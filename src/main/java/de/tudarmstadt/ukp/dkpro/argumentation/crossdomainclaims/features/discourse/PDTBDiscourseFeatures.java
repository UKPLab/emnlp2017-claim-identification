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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.discourse;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.discourse.utils.PDTBDiscourseFeatureMetaCollector;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Christian Stab
 */
public class PDTBDiscourseFeatures extends FeatureExtractorResource_ImplBase implements MetaDependent, ClassificationUnitFeatureExtractor{

	
	public static final String FN_DISCOURSE_FEATURES = "Discourse_";
	
	
	public static final String PARAM_DISCOURSE_FEATURES_FILE = "dfFile";
    @ConfigurationParameter(name = PARAM_DISCOURSE_FEATURES_FILE, mandatory = true)
    private String dfFile;
    
    public FrequencyDistribution<String> df;
    
    
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
    	if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
    	
    	try {
            df = new FrequencyDistribution<String>();
            df.load(new File(dfFile));
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    	
    	getLogger().info("Loaded " + df.getKeys().size() + " discourse features for feature extraction");
    	
    	return true;
    }
    
    
	@Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses() {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(PDTBDiscourseFeatureMetaCollector.class);
        
        return metaCollectorClasses;
    }
	
	
	@Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
        List<Feature> featList = new ArrayList<Feature>();

        FrequencyDistribution<String> dfFeatures = new FrequencyDistribution<String>();
        
        Collection<DiscourseRelation> disRel = JCasUtil.select(jcas, DiscourseRelation.class);
		
		Collection<DiscourseRelation> rels = PDTBDiscourseFeatureMetaCollector.getDiscourseRelationArg1(jcas, classificationUnit, disRel);
		for (DiscourseRelation rel : rels) {
			dfFeatures.addSample(PDTBDiscourseFeatureMetaCollector.getType(rel) + "_Arg1", 1);
		}
		rels = PDTBDiscourseFeatureMetaCollector.getDiscourseRelationArg2(jcas, classificationUnit, disRel);
		for (DiscourseRelation rel : rels) {
			dfFeatures.addSample(PDTBDiscourseFeatureMetaCollector.getType(rel) + "_Arg2", 1);
		}
		
		for (String discourseFeature : df.getKeys()) {
			if (dfFeatures.contains(discourseFeature)) {
				featList.add(new Feature(FN_DISCOURSE_FEATURES + discourseFeature, 1));
			} else {
				featList.add(new Feature(FN_DISCOURSE_FEATURES + discourseFeature, 0));
			}
		}
        
        return featList;
	}
	
}
