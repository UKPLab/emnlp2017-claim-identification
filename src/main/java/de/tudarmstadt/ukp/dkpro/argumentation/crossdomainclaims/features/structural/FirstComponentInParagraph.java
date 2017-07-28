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
public class FirstComponentInParagraph extends FeatureExtractorResource_ImplBase implements ClassificationUnitFeatureExtractor {
    
    public static final String FN_FIRST_COMPONENT_IN_PARAGRAPH = "FirstComponentInParagraph";


    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
        List<Feature> featList = new ArrayList<Feature>();
        
        Paragraph para = FeatureUtils.getCoveringParagraph(classificationUnit);
        
        Sentence firstComponent = null;
        if (para==null) {
        	featList.add(new Feature(FN_FIRST_COMPONENT_IN_PARAGRAPH, false));
        } else {
        	Collection<Sentence> sentences = JCasUtil.selectCovered(Sentence.class, para);
	        for (Sentence comp : sentences) {
	        	if (firstComponent==null) {
	        		firstComponent=comp;
	        		continue;
	        	}
	        	if (comp.getBegin()<firstComponent.getBegin()) firstComponent = comp;
	        }
	        
	        boolean isFirstComponentInParagraph = false;
	        if (firstComponent!=null) {
	        	isFirstComponentInParagraph = firstComponent.getBegin() == classificationUnit.getBegin();
	        }
	        
	        featList.add(new Feature(FN_FIRST_COMPONENT_IN_PARAGRAPH, isFirstComponentInParagraph));
        }
        
        return featList;
    }
}
