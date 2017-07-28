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

package de.tudarmstadt.ukp.dkpro.argumentation.io;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

/**
 * Does nothing, if the JCas contains paragraph annotations already. Otherwise, it annotates
 * paragraphs.
 *
 * @author Ivan Habernal
 */
public class ParagraphReAnnotator
        extends ParagraphSplitter
{
    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {
        Collection<Paragraph> paragraphs = JCasUtil.select(jCas, Paragraph.class);

        // only if there are no paragraphs
        if (paragraphs.isEmpty()) {
            super.process(jCas);
        }

        // make sure there are paragraphs now
        paragraphs = JCasUtil.select(jCas, Paragraph.class);
        if (paragraphs.isEmpty()) {
            throw new IllegalStateException("Paragraphs should have been annotated but none found");
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        File mainDir = new File(args[0]);
        File[] dirs = mainDir.listFiles(new FileFilter()
        {
            @Override public boolean accept(File pathname)
            {
                return pathname.isDirectory();
            }
        });

        for (File dir : dirs) {

            SimplePipeline.runPipeline(
                    CollectionReaderFactory.createReaderDescription(
                            XmiReader.class,
                            XmiReader.PARAM_SOURCE_LOCATION, dir,
                            XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"
                    ),
                    AnalysisEngineFactory.createEngineDescription(
                            ParagraphReAnnotator.class,
                            ParagraphReAnnotator.PARAM_SPLIT_PATTERN,
                            ParagraphReAnnotator.SINGLE_LINE_BREAKS_PATTERN
                    ),
                    AnalysisEngineFactory.createEngineDescription(
                            XmiWriter.class,
                            XmiWriter.PARAM_TARGET_LOCATION, new File(args[1], dir.getName())
                    )
            );
        }
    }
}
