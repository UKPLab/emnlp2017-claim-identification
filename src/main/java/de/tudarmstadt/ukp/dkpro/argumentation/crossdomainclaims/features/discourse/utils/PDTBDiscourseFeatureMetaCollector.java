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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.discourse.utils;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.FeatureUtils;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.discourse.PDTBDiscourseFeatures;
import de.tudarmstadt.ukp.dkpro.argumentation.types.ArgumentComponent;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument;
import de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation;
import de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation;
import de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ImplicitDiscourseRelation;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.FreqDistBasedMetaCollector;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Christian Stab
 */
public class PDTBDiscourseFeatureMetaCollector extends FreqDistBasedMetaCollector{

	public static final String DISCOURSE_FEATURES_FILE = "dfFile.ser";
    @ConfigurationParameter(name = PDTBDiscourseFeatures.PARAM_DISCOURSE_FEATURES_FILE, mandatory = true)
    private File dfFile;
    
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

    }
    
    
    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
        FrequencyDistribution<String> dfFeatures = new FrequencyDistribution<String>();
        
        Collection<DiscourseRelation> disRel = JCasUtil.select(jcas, DiscourseRelation.class);
		
		Collection<ArgumentComponent> comps = JCasUtil.select(jcas, ArgumentComponent.class);
		for (ArgumentComponent comp : comps) {
			Collection<DiscourseRelation> rels = getDiscourseRelationArg1(jcas, comp, disRel);
			for (DiscourseRelation rel : rels) {
				dfFeatures.addSample(getType(rel) + "_Arg1", 1);
			}
			rels = getDiscourseRelationArg2(jcas, comp, disRel);
			for (DiscourseRelation rel : rels) {
				dfFeatures.addSample(getType(rel) + "_Arg2", 1);
			}
		}

        for (String ngram : dfFeatures.getKeys()) {
            fd.addSample(ngram, dfFeatures.getCount(ngram));
        }
    }
    

    @Override
    public Map<String, String> getParameterKeyPairs() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(PDTBDiscourseFeatures.PARAM_DISCOURSE_FEATURES_FILE, DISCOURSE_FEATURES_FILE);
        return mapping;
    }

    
    @Override
    protected File getFreqDistFile() {
        return dfFile;
    }
    
    
	public static Collection<DiscourseRelation> getDiscourseRelationArg1(JCas jcas, Annotation comp, Collection<DiscourseRelation> disRel) {
		Collection<DiscourseArgument> args = FeatureUtils.selectOverlapping(DiscourseArgument.class, comp, jcas);
		
		ArrayList<DiscourseRelation> result = new ArrayList<DiscourseRelation>();
		
		for (DiscourseArgument arg : args) {
			result.addAll(getRelationArg1(arg, disRel));
		}
		
		return result;
	}
	
	
	public static Collection<DiscourseRelation> getRelationArg1(DiscourseArgument arg, Collection<DiscourseRelation> disRel) {
		ArrayList<DiscourseRelation> result = new ArrayList<DiscourseRelation>();
		for (DiscourseRelation rel : disRel) {
			if (rel.getArg1()==arg) result.add(rel);
		}
		
		return result;
	}
	
	
	public static Collection<DiscourseRelation> getDiscourseRelationArg2(JCas jcas, Annotation comp, Collection<DiscourseRelation> disRel) {
		
		Collection<DiscourseArgument> args = FeatureUtils.selectOverlapping(DiscourseArgument.class, comp, jcas);
		
		ArrayList<DiscourseRelation> result = new ArrayList<DiscourseRelation>();
		
		for (DiscourseArgument arg : args) {
			result.addAll(getRelationArg2(arg, disRel));
		}
		
		return result;
	}
	
	public static Collection<DiscourseRelation> getRelationArg2(DiscourseArgument arg, Collection<DiscourseRelation> disRel) {
		ArrayList<DiscourseRelation> result = new ArrayList<DiscourseRelation>();
		for (DiscourseRelation rel : disRel) {
			//if (rel.getArg1()==arg) result.add(rel);
			if (rel.getArg2()==arg) result.add(rel);
		}
		
		return result;
	}
	
	
	
	public static String getType(DiscourseRelation rel) {
		String type = "";
		if (rel instanceof ImplicitDiscourseRelation) {
			ImplicitDiscourseRelation imp = (ImplicitDiscourseRelation) rel;
			type = imp.getArg2().getArgumentType() + "_imp";
		}
		
		if (rel instanceof ExplicitDiscourseRelation) {
			ExplicitDiscourseRelation exp = (ExplicitDiscourseRelation) rel;
			if (exp.getDiscourseConnective1()!=null) {
				type = exp.getDiscourseConnective1().getConnectiveType() + "_exp";
			}
		}
		
		return type;
	}
}
