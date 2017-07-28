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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.reader;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCopier;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * For each sentence creates a new output CAS and annotates with {@linkplain TextClassificationOutcome}
 * with either "Claim" or "None"
 *
 * @author Ivan Habernal
 */
public class SentenceClaimOutcomeMultiplier
        extends JCasMultiplier_ImplBase
{

    // For each TextClassificationUnit stored in this collection one corresponding JCas is created.
    private Collection<? extends AnnotationFS> annotations;
    private Iterator<? extends AnnotationFS> iterator;

    private JCas jCas;

    private int subCASCounter;
    private Integer unitCounter;

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        this.jCas = aJCas;
        this.subCASCounter = 0;
        this.unitCounter = 0;

        // we will iterate over sentences
        this.annotations = JCasUtil.select(aJCas, Sentence.class);
        this.iterator = annotations.iterator();
    }

    @Override
    public boolean hasNext()
            throws AnalysisEngineProcessException
    {
        if (!iterator.hasNext()) {
            this.jCas = null;
            this.iterator = null;
            this.annotations = null;
            return false;
        }

        return true;
    }

    @Override
    public AbstractCas next()
            throws AnalysisEngineProcessException
    {
        // Create an empty CAS as a destination for a copy.
        JCas emptyJCas = this.getEmptyJCas();
        DocumentMetaData.create(emptyJCas);
        emptyJCas.setDocumentText(this.jCas.getDocumentText());
        CAS emptyCas = emptyJCas.getCas();

        // Copy current CAS to the empty CAS.
        CasCopier.copyCas(this.jCas.getCas(), emptyCas, false);
        JCas copyJCas;
        try {
            copyJCas = emptyCas.getJCas();
        }
        catch (CASException e) {
            throw new AnalysisEngineProcessException("Exception while creating JCas", null, e);
        }

        // Set new ids and URIs for copied cases.
        // The counting variable keeps track of how many new CAS objects are created from the
        // original CAS, a CAS relative counter.
        // NOTE: As it may cause confusion: If in sequence classification several or all CAS
        // contains only a single sequence this counter would be zero in all cases - this is not a
        // bug, but a cosmetic flaw
        DocumentMetaData.get(copyJCas).setDocumentId(
                DocumentMetaData.get(jCas).getDocumentId() + "_" + subCASCounter);
        DocumentMetaData.get(copyJCas).setDocumentUri(
                DocumentMetaData.get(jCas).getDocumentUri() + "_" + subCASCounter);

        // focus annotation
        AnnotationFS focusUnit = this.iterator.next();
        Sentence s = (Sentence) focusUnit;

        // set unit
        TextClassificationUnit unit = new TextClassificationUnit(copyJCas, s.getBegin(),
                s.getEnd());
        unit.setId(unitCounter);
        unitCounter++;
        subCASCounter++;

        // and outcome
        TextClassificationOutcome outcome = new TextClassificationOutcome(copyJCas, s.getBegin(),
                s.getEnd());

        if (ArgUtils.isClaim(s, copyJCas)) {
            outcome.setOutcome("Claim");
        }
        else {
            outcome.setOutcome("None");
        }
        outcome.addToIndexes();
        unit.addToIndexes();

        getLogger().debug("Creating CAS " + subCASCounter + " of " + annotations.size());

        return copyJCas;
    }

    public static void multiplyCorpus(File inputDir, File mainOutputDir)
            throws Exception
    {
        File outputDir = mainOutputDir;

        SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, inputDir,
                XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"),
                AnalysisEngineFactory.createEngineDescription(
                        SentenceClaimOutcomeMultiplier.class
                ),
                AnalysisEngineFactory.createEngineDescription(
                        XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, outputDir
                )
        );
    }

    public static void main(String[] args)
            throws Exception
    {
        File mainInputDir = new File(
                "PATH/TO/data-full-preprocessing-fixed-paragraphs");
        File mainOutputDir = new File(
                "PATH/TO/data-full-preprocessing-fixed-paragraphs-sentences");
        mainOutputDir.mkdirs();

        for (File dir : mainInputDir.listFiles()) {
            File outputDir = new File(mainOutputDir, dir.getName());
            multiplyCorpus(dir, outputDir);
        }
    }

}
