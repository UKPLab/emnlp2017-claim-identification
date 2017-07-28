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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.utils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class TokenIDTokenContentPrinter
        extends JCasAnnotator_ImplBase
{
    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        List<Token> tokens = new ArrayList<>(JCasUtil.select(aJCas, Token.class));

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            System.out.println(DocumentMetaData.get(aJCas).getDocumentId() + "_X_" + i + "\t" + t
                    .getCoveredText());
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        String inputDir = args[0];

        SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                XmiReader.class,
                XmiReader.PARAM_LENIENT, true,
                XmiReader.PARAM_SOURCE_LOCATION, inputDir,
                XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"
                ),
                AnalysisEngineFactory.createEngineDescription(
                        TokenIDTokenContentPrinter.class
                )
        );

    }
}
