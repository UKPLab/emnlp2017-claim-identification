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

package de.tudarmstadt.ukp.dkpro.core.api.discourse;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.*;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;
import java.util.Arrays;

/**
 * For debugging purposes; prints the discourse annotations in cas
 *
 * @author Ivan Habernal
 */
public class DiscourseDumpWriter
        extends JCasAnnotator_ImplBase
{
    /**
     * Output file. If multiple CASes as processed, their contents are concatenated into this file.
     * When this file is set to "-", the dump does to{@link System#out} (default).
     */
    public static final String PARAM_OUTPUT_FILE = "outputFile";

    @ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true, defaultValue = "-")
    private File outputFile;

    private PrintWriter out;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            if (out == null) {
                if ("-".equals(outputFile.getName())) {
                    out = new PrintWriter(new CloseShieldOutputStream(System.out));
                }
                else {
                    if (outputFile.getParentFile() != null) {
                        outputFile.getParentFile().mkdirs();
                    }
                    out = new PrintWriter(
                            new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
                }
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {
        out.println("++ Document: " + DocumentMetaData.get(jCas).getDocumentId());

        for (DiscourseRelation relation : JCasUtil.select(jCas, DiscourseRelation.class)) {
            out.println(debugRelation(relation));
        }

        out.flush();
    }

    /**
     * Debugs discourse relation to String
     *
     * @param relation relation
     */
    public static String debugRelation(DiscourseRelation relation)
    {

        //        StringWriter out = new StringWriter();
        StringWriter stringWriter = new StringWriter();
        PrintWriter out = new PrintWriter(stringWriter);

        out.println("---- discourse relation " + relation.getRelationId());
        out.println(" relation class: " + relation.getClass().getSimpleName());
        out.println(" coveredText: " + relation.getCoveredText());
        for (DiscourseArgument argument : Arrays
                .asList(relation.getArg1(), relation.getArg2())) {
            out.println("   -- arg" + argument.getArgumentNumber() + ": ");
            out.println("      argumentType: " + (argument.getArgumentType() != null ?
                    argument.getArgumentType() : "-"));
            out.println("      coveredText: " + argument.getCoveredText());

        }

        if (relation instanceof ExplicitDiscourseRelation) {
            ExplicitDiscourseRelation explicitRelation = (ExplicitDiscourseRelation) relation;

            for (DiscourseConnective connective : Arrays
                    .asList(explicitRelation.getDiscourseConnective1(),
                            explicitRelation.getDiscourseConnective2())) {
                if (connective != null) {
                    out.println("   -- connectiveType: " + connective.getConnectiveType());
                    out.println("      coveredText: " + connective.getCoveredText());
                }
            }
        }

        for (DiscourseAttribution attribution : JCasUtil
                .selectCovered(DiscourseAttribution.class, relation)) {
            out.println("   -- attribution " + attribution.getAttributeId());
            out.println("      coveredText: " + attribution.getCoveredText());
        }

        out.println("---- end of relation " + relation.getRelationId());
        out.flush();

        return stringWriter.toString();
    }
}
