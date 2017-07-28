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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.structural;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.FeatureUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Christian Stab
 */
public class LastComponentInParagraph extends FeatureExtractorResource_ImplBase implements ClassificationUnitFeatureExtractor {
    
    public static final String FN_LAST_COMPONENT_IN_PARAGRAPH = "LastComponentInParagraph";


    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
        List<Feature> featList = new ArrayList<Feature>();

        Paragraph para = FeatureUtils.getCoveringParagraph(classificationUnit);
        
        if (para==null) {
        	 featList.add(new Feature(FN_LAST_COMPONENT_IN_PARAGRAPH, false));
        } else {
        	Collection<Sentence> sentences = JCasUtil.selectCovered(Sentence.class, para);
	        Sentence lastComponent = null;
	        for (Sentence comp : sentences) {
	        	if (lastComponent==null) {
	        		lastComponent=comp;
	        		continue;
	        	}
	        	if (comp.getBegin()>lastComponent.getBegin()) lastComponent = comp;
	        }
	        
	        boolean isLastComponentInParagraph = false;
	        if (lastComponent!=null) {
	        	isLastComponentInParagraph = lastComponent.getBegin() == classificationUnit.getBegin();
	        }
	        
	        featList.add(new Feature(FN_LAST_COMPONENT_IN_PARAGRAPH, isLastComponentInParagraph));
        }
        
        return featList;
    }
}
