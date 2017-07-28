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

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.utils.PunctuationSequenceMetaCollector;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PunctuationSequenceUFE extends FeatureExtractorResource_ImplBase implements ClassificationUnitFeatureExtractor, MetaDependent {
	
	public static final String FN_PUNCTUATION_SEQUENCE_PREFIX = "PunctSeq_";
	
    public static final String PARAM_PUNCTUATION_SEQUENCE_FD_FILE = "punctSequenceFdFile";
    @ConfigurationParameter(name = PARAM_PUNCTUATION_SEQUENCE_FD_FILE, mandatory = true)
    private String punctSequenceFdFile;
    
    public FrequencyDistribution<String> topKPunctuationSequences;
	
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
    	if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
    	
    	FrequencyDistribution<String> trainingFD;
    	try {
            trainingFD = new FrequencyDistribution<String>();
            trainingFD.load(new File(punctSequenceFdFile));
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        
    	topKPunctuationSequences = new FrequencyDistribution<String>();
   
		for (String sample : trainingFD.getKeys()) {
			topKPunctuationSequences.addSample(sample, trainingFD.getCount(sample));
		}
    	
    	getLogger().info("Loaded " + topKPunctuationSequences.getKeys().size() + " punctuation sequences.");
    	return true;
    }

	@Override
	public List<Feature> extract(JCas view, TextClassificationUnit classificationUnit)
			throws TextClassificationException {
	    
		String text = classificationUnit.getCoveredText();
		List<Feature> features = new ArrayList<Feature>();
		String unitSequence = PunctuationSequenceMetaCollector.createPunctuationSequence(text);
		
		for (String seq : topKPunctuationSequences.getKeys()) {
        	if (unitSequence.equals(seq)) features.add(new Feature(FN_PUNCTUATION_SEQUENCE_PREFIX + seq, true));
        	else features.add(new Feature(FN_PUNCTUATION_SEQUENCE_PREFIX + seq, false));
		}
		return features;
	}

	@Override
	public List<Class<? extends MetaCollector>> getMetaCollectorClasses() {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(PunctuationSequenceMetaCollector.class);
        return metaCollectorClasses;
	}
}
