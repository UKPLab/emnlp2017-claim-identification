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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.indicators;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.indicators.utils.AbstractIndicatorFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.argumentation.types.ArgumentComponent;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

public class IndicatorFeatures extends AbstractIndicatorFeatureExtractor implements ClassificationUnitFeatureExtractor {
	
	public static final String FN_THESIS_IND_CURRENT = "CurrentThesisInd";
	public static final String FN_FWD_IND_CURRENT = "CurrentFwdInd";
	public static final String FN_BWD_IND_CURRENT = "CurrentBwdInd";
	public static final String FN_CONT_IND_CURRENT = "CurrentContInd";
	
	
	public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
		List<Feature> featList = new ArrayList<Feature>();
		
		ArrayList<ArgumentComponent> comp = new ArrayList<ArgumentComponent>();
		
		boolean bwdInd = false;
		boolean fwdInd = false;
		boolean thesisInd = false;
		boolean contInd = false;
		
		if (JCasUtil.selectCovered(ArgumentComponent.class, classificationUnit).size()>0) {
			comp.add(JCasUtil.selectCovered(ArgumentComponent.class, classificationUnit).iterator().next());
		
			bwdInd = indicatorUtils.isBwdIndicatorPresent(jcas, comp);
			fwdInd = indicatorUtils.isFwdIndicatorPresent(jcas, comp);
			thesisInd = indicatorUtils.isThesisIndicatorPresent(jcas, comp);
			contInd = indicatorUtils.isContIndicatorPresent(jcas, comp);
		}
       
        featList.add(new Feature(FN_THESIS_IND_CURRENT, thesisInd));
        featList.add(new Feature(FN_FWD_IND_CURRENT, fwdInd));
        featList.add(new Feature(FN_BWD_IND_CURRENT, bwdInd));
        featList.add(new Feature(FN_CONT_IND_CURRENT, contInd));
        
        return featList;
	}
	
}
