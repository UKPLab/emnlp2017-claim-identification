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

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.argumentation.io.writer.ArgumentDumpWriter;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import org.apache.commons.io.FileUtils;
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
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class ClaimExamplesExplorer
        extends JCasConsumer_ImplBase
{
    public static final String PARAM_RANDOM_CLAIMS_COUNT = "randomClaimsCount";
    @ConfigurationParameter(name = PARAM_RANDOM_CLAIMS_COUNT, defaultValue = "50")
    int randomClaimsCount;

    public static final String PARAM_OUTPUT_FILE = "outputFile";
    @ConfigurationParameter(name = PARAM_OUTPUT_FILE)
    File outputFile;

    /**
     * Random for reproducibility
     */
    Random random = new Random(1234);

    SortedSet<String> allClaims = new TreeSet<>();

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
            if (ArgUtils.isClaim(sentence, aJCas)) {
                allClaims.add(sentence.getCoveredText());
            }
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            ArrayList<String> claimsList = new ArrayList<>(allClaims);
            // shuffle
            Collections.shuffle(claimsList, random);

            List<String> randomClaims = claimsList.subList(0, randomClaimsCount);
            FileUtils.writeLines(outputFile, "utf-8", randomClaims);
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

        File outDir = new File("/tmp/claims");
        outDir.mkdir();

        for (File dir : dirs) {
            File outputFile = new File(outDir, dir.getName() + "_claims.txt");

            SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                    XmiReader.class,
                    XmiReader.PARAM_SOURCE_LOCATION, dir,
                    XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"
                    ),
                    AnalysisEngineFactory.createEngineDescription(
                            ClaimExamplesExplorer.class,
                            ClaimExamplesExplorer.PARAM_OUTPUT_FILE, outputFile
                    ),
                    AnalysisEngineFactory.createEngineDescription(
                            ArgumentDumpWriter.class
                    )
            );
        }

    }
}

