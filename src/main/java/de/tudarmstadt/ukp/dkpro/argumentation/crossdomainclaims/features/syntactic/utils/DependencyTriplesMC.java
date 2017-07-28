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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.utils;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.DependencyTriples;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.FreqDistBasedMetaCollector;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Christian Stab
 */
public class DependencyTriplesMC extends FreqDistBasedMetaCollector {

//	public static final String DEPENDENCIES_FD_KEY = "depTripleFdFile.ser";
//    @ConfigurationParameter(name = DependencyTriples.PARAM_DEPENDENCY_TRIPLE_FD_FILE, mandatory = true)
//    private File depTripleFdFile;
    
    
    public static final String PARAM_DEPENDENCY_TRIPLE_FD_FILE = "depTripleFdFile";
    @ConfigurationParameter(name = PARAM_DEPENDENCY_TRIPLE_FD_FILE, mandatory = true)
    private File depTripleFdFile;
    
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

    }
    
    
    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
        FrequencyDistribution<String> foundDependencies = new FrequencyDistribution<String>();
        	
    	Collection<Dependency> dependencies = JCasUtil.select(jcas, Dependency.class);  
    	
    	for (Dependency dep : dependencies) {
			
    		// lemma with type
			String dependency = dep.getDependencyType()+ ":" + dep.getGovernor().getLemma().getValue()+"_"+dep.getDependent().getLemma().getValue();
    		
			foundDependencies.addSample(dependency, foundDependencies.getCount(dependency)+1);
		}

        for (String ngram : foundDependencies.getKeys()) {
            fd.addSample(ngram, foundDependencies.getCount(ngram));
        }
    }

    @Override
    public Map<String, String> getParameterKeyPairs() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(DependencyTriples.PARAM_DEPENDENCY_TRIPLE_FD_FILE, PARAM_DEPENDENCY_TRIPLE_FD_FILE);
        return mapping;
    }

    @Override
    protected File getFreqDistFile() {
        return depTripleFdFile;
    }
}
