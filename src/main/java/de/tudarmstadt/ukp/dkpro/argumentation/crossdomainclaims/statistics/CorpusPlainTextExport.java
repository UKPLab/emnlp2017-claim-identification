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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.statistics;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Extracts lemmas from all corpora and sorts them by frequency
 *
 * @author Ivan Habernal
 */
public class CorpusPlainTextExport
        extends JCasConsumer_ImplBase
{

    public static final String PARAM_OUTPUT_FILE = "outputFile";
    @ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
    File outputFile;

    Map<String, Integer> wordFrequencies = new HashMap<>();

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
        for (Token token : tokens) {
            String key = token.getLemma().getValue();
            if (!wordFrequencies.containsKey(key)) {
                wordFrequencies.put(key, 0);
            }
            wordFrequencies.put(key, wordFrequencies.get(key) + 1);
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map,
            final boolean reverse)
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
            {
                if (reverse) {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
                else {
                    return (o1.getValue()).compareTo(o2.getValue());
                }
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            // sort by values
            Map<String, Integer> sorted = sortByValue(wordFrequencies, true);

            PrintWriter printWriter;
            printWriter = new PrintWriter(outputFile, "utf-8");
            for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
                printWriter.println(entry.getKey() + "\t" + entry.getValue());
            }

            IOUtils.closeQuietly(printWriter);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        String origPath = "/home/user-ukp/data2/ACL2016-jointPub-data/full-preprocessing-fixed-paragraphs/";
        File[] dirs = new File(origPath).listFiles(
                new FileFilter()
                {
                    @Override public boolean accept(File pathname)
                    {
                        return pathname.isDirectory();
                    }
                });

        File outDir = new File("/tmp/out2");

        for (File dir : dirs) {
            File outputFile = new File(outDir, dir.getName() + "_lemmas.txt");

            SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                    XmiReader.class,
                    XmiReader.PARAM_SOURCE_LOCATION, dir,
                    XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"
                    ),
                    AnalysisEngineFactory.createEngineDescription(
                            CorpusPlainTextExport.class,
                            CorpusPlainTextExport.PARAM_OUTPUT_FILE, outputFile
                    )
            );
        }

    }
}
