/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.dictionary;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class LiwcUFE
        extends FeatureExtractorResource_ImplBase
        implements ClassificationUnitFeatureExtractor
{

    public static final String PARAM_DICTIONARY_FILE = "DictionaryFileLiwc";
    @ConfigurationParameter(name = PARAM_DICTIONARY_FILE, mandatory = true, defaultValue = "dictionary/LIWC_en.txt")
    private String dicFilePath;

    public static final String PARAM_ROW_DELIMITER = "DelimiterLiwc";
    @ConfigurationParameter(name = PARAM_ROW_DELIMITER, mandatory = true, defaultValue = ",")
    private String delimiter;

    public static final String PARAM_DICTIONARY_NAME = "DictionaryNameLiwc";
    @ConfigurationParameter(name = PARAM_DICTIONARY_NAME, mandatory = true, defaultValue = "LIWC_")
    private String prefix;
    private HashMap<String, List<Integer>> exactMap;
    private HashMap<String, List<Integer>> prefixMap;
    private HashMap<Integer, String> idToClassName;
    private HashMap<Integer, Integer> classOccurences;

    @Override
    public List<Feature> extract(JCas jCas, TextClassificationUnit unit)
            throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();
        List<String> tokens = JCasUtil.toText(JCasUtil.selectCovered(jCas, Token.class, unit));
        classOccurences = new HashMap<Integer, Integer>();

        if (dicFilePath == null || dicFilePath.isEmpty()) {
            System.out.println("LiwcUFE: Path to dictionary must be set!");
        }

        for (String token : tokens) {
            List<Integer> vals = new ArrayList<Integer>();
            if (exactMap.containsKey(token)) {
                vals = exactMap.get(token);
            }
            else {
                for (String prefix : prefixMap.keySet()) {
                    if (token.startsWith(prefix))
                        vals = prefixMap.get(prefix);
                }
            }
            for (int v : vals) {
                if (!classOccurences.containsKey(v))
                    classOccurences.put(v, 1);
                else
                    classOccurences.put(v, classOccurences.get(v) + 1);
            }
        }
        double numTokens = tokens.size();
        for (int id : idToClassName.keySet()) {
            int wordCount = classOccurences.get(id) == null ? 0 : classOccurences.get(id);
            String className = idToClassName.get(id);
            featList.add(new Feature(prefix + "_" + className,
                    numTokens > 0 ? ((double) wordCount / numTokens) : 0));
        }
        return featList;
    }

    @Override
    public boolean initialize(ResourceSpecifier resSpec, Map params)
            throws ResourceInitializationException
    {
        if (!super.initialize(resSpec, params)) {
            return false;
        }
        List<String> lines = new ArrayList<String>();
        List<String> classes = new ArrayList<String>();
        List<String> instances = new ArrayList<String>();
        exactMap = new HashMap<String, List<Integer>>();
        prefixMap = new HashMap<String, List<Integer>>();
        idToClassName = new HashMap<Integer, String>();
        if (dicFilePath != null && !dicFilePath.isEmpty()) {
            // Read the file and split into lines of classes and instances
            try {
                // try to resolve as File first
                File dicFile = new File(dicFilePath);
                if (dicFile.exists()) {
                    lines = FileUtils.readLines(dicFile);
                }
                else {
                    // try resource class loader
                    InputStream inputStream = this.getClass().getClassLoader()
                            .getResourceAsStream(dicFilePath);

                    if (inputStream != null) {
                    	lines = IOUtils.readLines(inputStream);
                    } else {
                        throw new IOException(
                                "Cannot load " + dicFilePath + " either as File or as stream");
                    }
                }

                Iterator<String> iter = lines.iterator();
                int percentage = 0;
                while (percentage < 2 && iter.hasNext()) {
                    String line = iter.next();
                    if (line.contains("%"))
                        percentage++;
                    else
                        classes.add(line.trim());
                    // TODO: Assert regex
                }
                while (iter.hasNext()) {
                    String line = iter.next();
                    if (!line.equals(""))
                        ;
                    instances.add(line.trim());
                    // TODO: Assert regex
                }
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
            // Map from number to corresponding class name
            for (String line : classes) {
                String[] split = line.split(delimiter);
                assert split.length == 2;
                idToClassName.put(Integer.parseInt(split[0]), split[1]);
            }
            // Map from instance name to classes
            for (String line : instances) {
                String[] split = line.split(delimiter);
                List<Integer> vals = new ArrayList<Integer>();
                // '*' denotes a prefix
                if (split[0].endsWith("*")) {
                    split[0] = split[0].substring(0, split[0].length() - 1);
                    if (prefixMap.containsKey(split[0]))
                        vals = prefixMap.get(split[0]);
                    for (int i = 1; i < split.length; i++) {
                        int value = Integer.parseInt(split[i]);
                        if (!vals.contains(value))
                            vals.add(Integer.parseInt(split[i]));
                    }
                    prefixMap.put(split[0], vals);
                }
                else {
                    //System.out.println(split[0]);
                    if (exactMap.containsKey(split[0]))
                        vals = exactMap.get(split[0]);
                    for (int i = 1; i < split.length; i++) {
                        int value = Integer.parseInt(split[i]);
                        if (!vals.contains(value))
                            vals.add(Integer.parseInt(split[i]));
                    }
                    exactMap.put(split[0], vals);
                }
            }
        }
        return true;
    }
}
