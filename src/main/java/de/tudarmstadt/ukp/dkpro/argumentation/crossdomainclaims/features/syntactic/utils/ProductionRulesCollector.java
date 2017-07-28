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

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.FeatureUtils;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.ProductionRules;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.FreqDistBasedMetaCollector;
import edu.stanford.nlp.trees.Tree;
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
public class ProductionRulesCollector extends FreqDistBasedMetaCollector{

	public static final String PRODRULES_FD_KEY = "prodRules.ser";
    @ConfigurationParameter(name = ProductionRules.PARAM_PRODRULES_FD_FILE, mandatory = true)
    private File prodRulesFdFile;
    
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

    }
    
    
    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
    	
        FrequencyDistribution<String> rules = new FrequencyDistribution<String>();
        
        Collection<TextClassificationUnit> units = JCasUtil.select(jcas, TextClassificationUnit.class);
        for (TextClassificationUnit u : units) {
        	Sentence sentence = FeatureUtils.getCoveringSentence(u);
        	Collection<ROOT> root =	JCasUtil.selectCovered(ROOT.class,sentence);
    		if (!root.isEmpty()) {
    			Tree tree = TreeUtils.createStanfordTree(root.iterator().next());
    			getProductionRules(tree, rules);
    		} 
        }

        for (String ngram : rules.getKeys()) {
            fd.addSample(ngram, rules.getCount(ngram));
        }
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
    

    @Override
    public Map<String, String> getParameterKeyPairs() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(ProductionRules.PARAM_PRODRULES_FD_FILE, PRODRULES_FD_KEY);
        return mapping;
    }

    @Override
    protected File getFreqDistFile() {
        return prodRulesFdFile;
    }
}
