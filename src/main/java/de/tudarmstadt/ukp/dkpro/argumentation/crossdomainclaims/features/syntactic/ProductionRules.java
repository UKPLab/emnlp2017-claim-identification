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

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.FeatureUtils;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.utils.ProductionRulesCollector;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import edu.stanford.nlp.trees.Tree;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Christian Stab
 */
public class ProductionRules extends FeatureExtractorResource_ImplBase implements MetaDependent, ClassificationUnitFeatureExtractor{

	
	public static final String FN_PRODUCTION_RULE = "ProductionRule_";
	
	public static final String PARAM_PRODRULES_FD_FILE = "prodRulesFdFile";
    @ConfigurationParameter(name = PARAM_PRODRULES_FD_FILE, mandatory = true)
    private String prodRulesFdFile;
    
    public static final String PARAM_NUMBER_OF_PRODRULES = "numberOfProdRules";
    @ConfigurationParameter(name = PARAM_NUMBER_OF_PRODRULES, mandatory = true, defaultValue="-1")
    private int numberOfProdRules;
    
    public static final String PARAM_THRESHOLD = "ruleThreshold";
    @ConfigurationParameter(name = PARAM_THRESHOLD, mandatory = true, defaultValue="5") //10
    private int ruleThreshold;
    
    public FrequencyDistribution<String> topKProdRules;
    
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
    	if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
    	
    	FrequencyDistribution<String> trainingFD;
    	try {
            trainingFD = new FrequencyDistribution<String>();
            trainingFD.load(new File(prodRulesFdFile));
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
    	topKProdRules = new FrequencyDistribution<String>();
    	
    	if (numberOfProdRules==-1) {
    		// consider samples up to a given frequency threshold
    		for (String sample : trainingFD.getKeys()) {
    			if (trainingFD.getCount(sample)>=ruleThreshold) {
    				topKProdRules.addSample(sample, trainingFD.getCount(sample));
    			}
    		}
    	} else {
    		// consider a given number of verbs
	    	List<String> topK = trainingFD.getMostFrequentSamples(numberOfProdRules);
	    	for (String sample : topK) {
	    		topKProdRules.addSample(sample, trainingFD.getCount(sample));
	    	}
    	}
    	
    	getLogger().info("Loaded " + topKProdRules.getKeys().size() + " production rules for feature extraction");
    	
    	return true;
    }
    
    
	@Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses() {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(ProductionRulesCollector.class);
        
        return metaCollectorClasses;
    }
	
	
	@Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
        List<Feature> featList = new ArrayList<Feature>();

        Sentence sentence = FeatureUtils.getCoveringSentence(classificationUnit);
        
        Collection<ROOT> root =	JCasUtil.selectCovered(ROOT.class,sentence);
        FrequencyDistribution<String> rules = new FrequencyDistribution<String>();
		if (!root.isEmpty()) {
			Tree tree = TreeUtils.createStanfordTree(root.iterator().next());
			getProductionRules(tree, rules);
		}
		
		for (String rule : topKProdRules.getKeys()) {
			if (rules.contains(rule)) {
				featList.add(new Feature(FN_PRODUCTION_RULE + rule, rules.getCount(rule)));
			} else {
				featList.add(new Feature(FN_PRODUCTION_RULE + rule, 0));
			}
		}
        
        return featList;
	}
	
	
	public static FrequencyDistribution<String> getProductionRules(Tree tree, FrequencyDistribution<String> rules) {
		if (tree.getChildrenAsList().size()>1) {
			String rule = tree.value() + "->";
			for (Tree t : tree.getChildrenAsList()) {
				rule = rule + t.value() + ",";
			}
			//System.out.println(rule);
			rules.addSample(rule, 1);
		}
		
		for (Tree t : tree.getChildrenAsList()) {
			getProductionRules(t,rules);
		}
		
		return rules;
	}
	
}
