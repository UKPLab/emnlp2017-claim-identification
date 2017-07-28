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

public class RelativePositionInParagraph extends FeatureExtractorResource_ImplBase implements ClassificationUnitFeatureExtractor {
    
    public static final String RELATIVE_POSITION_OF_SENTENCE_IN_PARAGRAPH = "RelativePositionOfSentenceInParagraph";
    
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
        List<Feature> featList = new ArrayList<Feature>();

		Collection<Paragraph> paragraphs = JCasUtil.select(jcas, Paragraph.class);
		Collection<Sentence> relevantSentences = JCasUtil.select(jcas, Sentence.class);
		if(paragraphs.size()>0){
			for (Paragraph paragraph : paragraphs) {
				int begin = paragraph.getBegin();
				int end = paragraph.getEnd();
				if(classificationUnit.getBegin()>= begin && classificationUnit.getEnd() <= end){
					// This is our paragraph
					relevantSentences = JCasUtil.selectCovered(jcas, Sentence.class, paragraph);
				}
			}
		}
        
        if (relevantSentences.size()==0) {
        	featList.add(new Feature(RELATIVE_POSITION_OF_SENTENCE_IN_PARAGRAPH, false));
        } else {
        	double before = 0;
        	double after = 0;
	        for (Sentence sent : relevantSentences) {
	        	if(sent.getBegin()<classificationUnit.getBegin()){
	        		before ++;
	        	}
	        	if(sent.getBegin()>classificationUnit.getEnd()){
	        		after++;
	        	}
	        }
	        featList.add(new Feature(RELATIVE_POSITION_OF_SENTENCE_IN_PARAGRAPH, before/(after+before)));
        }        
        return featList;
    }
}
