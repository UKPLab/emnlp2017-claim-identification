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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.utils.DependencyTriplesMC;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import java.io.File;
import java.util.*;

/**
 * 
 * @author Christian Stab
 */
public class DependencyTriples extends FeatureExtractorResource_ImplBase implements MetaDependent, ClassificationUnitFeatureExtractor {

	
	public static final String FN_DEPENDENCY_TRIPLE_PREFIX = "DepTriple_";
	
	
	public static final String PARAM_DEPENDENCY_TRIPLE_FD_FILE = "depTripleFdFile";
    @ConfigurationParameter(name = PARAM_DEPENDENCY_TRIPLE_FD_FILE, mandatory = true)
    private String depTripleFdFile;
    
    public static final String PARAM_NUMBER_OF_DEPENDENCY_TRIPLES = "numberOfDependencyTriples";
    @ConfigurationParameter(name = PARAM_NUMBER_OF_DEPENDENCY_TRIPLES, mandatory = true, defaultValue="-1")
    private int numberOfDependencyTriples;
    
    public FrequencyDistribution<String> topKDeps;
    
    
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
    	if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
    	
    	FrequencyDistribution<String> trainingFD;
    	try {
            trainingFD = new FrequencyDistribution<String>();
            trainingFD.load(new File(depTripleFdFile));
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
    	topKDeps = new FrequencyDistribution<String>();
    	
    	if (numberOfDependencyTriples==-1) {
    		// consider all samples
    		for (String sample : trainingFD.getKeys()) {
    			topKDeps.addSample(sample, trainingFD.getCount(sample));
    		}
    	}
    	else {
    		// consider a given number of samples
	    	List<String> topK = trainingFD.getMostFrequentSamples(numberOfDependencyTriples);
	    	for (String sample : topK) {
	    		topKDeps.addSample(sample, trainingFD.getCount(sample));
	    	}
    	} 	
    	
    	getLogger().info("Loaded " + topKDeps.getKeys().size() + " dependencies for feature extraction");
    	
    	return true;
    }
    
	
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses() {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(DependencyTriplesMC.class);
        
        return metaCollectorClasses;
    }
	
	
    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
        List<Feature> featList = new ArrayList<Feature>();
        
        HashSet<String> foundDependencies = new HashSet<String>();	
    	Collection<Dependency> dependencies = JCasUtil.selectCovered(Dependency.class, classificationUnit);  
        
    	for (Dependency dep : dependencies) {
    		// lemma with type
			String dependency = dep.getDependencyType()+ ":" + dep.getGovernor().getLemma().getValue()+"_"+dep.getDependent().getLemma().getValue();
			foundDependencies.add(dependency);
		}
        
        // extract features
        for (String dep : topKDeps.getKeys()) {
        	if (foundDependencies.contains(dep)) featList.add(new Feature(FN_DEPENDENCY_TRIPLE_PREFIX+"_source_" + dep, true));
        	else featList.add(new Feature(FN_DEPENDENCY_TRIPLE_PREFIX+"_source_" + dep, false));
        }
        
        return featList;
	}
}
