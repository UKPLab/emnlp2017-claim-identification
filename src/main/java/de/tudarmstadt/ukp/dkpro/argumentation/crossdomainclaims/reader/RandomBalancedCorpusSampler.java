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

import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Reads all XMI files where each XMI file contains only a single
 * {@linkplain TextClassificationOutcome} annotation with "Claim" or "None" (covering a single
 * sentence), produced by {@linkplain SentenceClaimOutcomeMultiplier}.
 * Randomly shuffles claims and non-claims documents and "reads" a balanced list.
 *
 * @author Ivan Habernal
 */
public class RandomBalancedCorpusSampler
        extends XmiReader
{
    public static final String PARAM_RANDOM_SEED = "randomSeed";
    @ConfigurationParameter(name = PARAM_RANDOM_SEED)
    protected int randomSeed;

    /**
     * Random number generator
     */
    protected Random random;

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException
    {
        random = new Random(randomSeed);

        super.initialize(aContext);
    }

    @Override
    protected Collection<Resource> scan(String aBase,
            Collection<String> aIncludes, Collection<String> aExcludes)
            throws IOException
    {	
        try {
            Collection<Resource> resourceCollection = super.scan(aBase, aIncludes, aExcludes);
            Map<String, List<Resource>> claimDocs = new HashMap<String, List<Resource>>();
            Map<String, List<Resource>> nonClaimDocs = new HashMap<String, List<Resource>>();
            JCas jCas = JCasFactory.createJCas();

            for (Resource res : resourceCollection) {
            	String dataset = res.getResource().getFile().getParentFile().getName();    	
            	jCas.reset();
                
                CAS aCAS = jCas.getCas();
                initCas(aCAS, res);
                
                InputStream is = null;

                try {
                	is = CompressionUtils
                        .getInputStream(res.getLocation(), res.getInputStream());
                	XmiCasDeserializer.deserialize(is, aCAS, false);
                }
                catch (SAXException e) {
                	throw new IOException(e);
                }
                finally {
                	IOUtils.closeQuietly(is);            
                }

                // find TextOutcome - there must be only one (for a single sentence)
                TextClassificationOutcome outcome = JCasUtil
                        .selectSingle(jCas, TextClassificationOutcome.class);

                if ("Claim".equals(outcome.getOutcome())) {
                	List<Resource> resList = claimDocs.containsKey(dataset) ? claimDocs.get(dataset) : new ArrayList<Resource>();
                	resList.add(res);
                    claimDocs.put(dataset, resList);
                }
                else if ("None".equals(outcome.getOutcome())) {
                	List<Resource> resList = nonClaimDocs.containsKey(dataset) ? nonClaimDocs.get(dataset) : new ArrayList<Resource>();
                	resList.add(res);
                    nonClaimDocs.put(dataset, resList);                    
                }
                else {
                    throw new IllegalArgumentException(outcome.getOutcome());
                }
            }

            List<Resource> allDocs = new ArrayList<>();
            // now shuffle, trim, and throw into final list of resources
            for (String dataset : claimDocs.keySet()) {
            	Collections.shuffle(claimDocs.get(dataset), random);
            	Collections.shuffle(nonClaimDocs.get(dataset), random);
            	int maxLength = Math.min(claimDocs.get(dataset).size(), nonClaimDocs.get(dataset).size());
            	List<Resource> c = claimDocs.get(dataset).subList(0, maxLength);
            	List<Resource> nc = nonClaimDocs.get(dataset).subList(0, maxLength);
            	allDocs.addAll(c);
            	allDocs.addAll(nc);
			}

            // add some extra randomization
            Collections.shuffle(allDocs, random);
            return allDocs;
        }
        catch (UIMAException e) {
            throw new IOException(e);
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                RandomBalancedCorpusSampler.class,
                RandomBalancedCorpusSampler.PARAM_SOURCE_LOCATION, "/tmp/out",
                RandomBalancedCorpusSampler.PARAM_PATTERNS,
                RandomBalancedCorpusSampler.INCLUDE_PREFIX + "*.xmi",
                RandomBalancedCorpusSampler.PARAM_RANDOM_SEED, 1),
                AnalysisEngineFactory.createEngineDescription(
                        NoOpAnnotator.class
                )
        );
    }
}
