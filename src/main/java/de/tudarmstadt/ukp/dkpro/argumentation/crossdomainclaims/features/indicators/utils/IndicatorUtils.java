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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.indicators.utils;

import de.tudarmstadt.ukp.dkpro.argumentation.types.ArgumentComponent;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.Collection;
import java.util.HashMap;

public class IndicatorUtils {
	
	public static final String BWD_INDICATOR = "Bwd";
	public static final String FWD_INDICATOR = "Fwd";
	public static final String CONT_INDICATOR = "Cont";
	public static final String THESIS_INDICATOR = "Thesis";

	private static IndicatorUtils instance;

	private boolean caseSensitive = true;
	private PhraseMap fwdMap = null;
	private PhraseMap bwdMap = null;
	private PhraseMap contrastMap = null;
	private PhraseMap thesisMap = null;
	
	private IndicatorUtils (String fwdIndicatorsList, String bwdIndicatorsList, String contrastIndicatorsList, String thesisIndicatorsList) {
		fwdMap = new PhraseMap(fwdIndicatorsList, caseSensitive);
		bwdMap = new PhraseMap(bwdIndicatorsList, caseSensitive);
		contrastMap = new PhraseMap(contrastIndicatorsList, caseSensitive);
		thesisMap = new PhraseMap(thesisIndicatorsList, caseSensitive);
	}
	
	public static IndicatorUtils getInstance (String fwdIndicatorsList, String bwdIndicatorsList, String contrastIndicatorsList, String thesisIndicatorsList) {
	    if (IndicatorUtils.instance == null) {
	    	IndicatorUtils.instance = new IndicatorUtils (fwdIndicatorsList, bwdIndicatorsList, contrastIndicatorsList, thesisIndicatorsList);
	    }
	    return IndicatorUtils.instance;
	}
	
	
	public String getPhraseType(JCas jcas, Annotation anno) {
		Collection<Token> tokens = JCasUtil.selectCovered(Token.class, anno);
		
		HashMap<String,Integer> fwdPhrases = fwdMap.getPhrases(tokens);
		HashMap<String,Integer> bwdPhrases = bwdMap.getPhrases(tokens);
		HashMap<String,Integer> contrastPhrases = contrastMap.getPhrases(tokens);
		HashMap<String,Integer> thesisPhrases = thesisMap.getPhrases(tokens);
		
		String result = ""; 
		
		if (fwdPhrases.size()>0) result = result + FWD_INDICATOR;
		if (bwdPhrases.size()>0) result = result + BWD_INDICATOR;
		if (contrastPhrases.size()>0) result = result + CONT_INDICATOR;
		if (thesisPhrases.size()>0) result = result + THESIS_INDICATOR;
		
		return (result.length()==0)?"None":result;
	}
	
	public String getParagraphSequence(JCas jcas, Paragraph para) {
		Collection<ArgumentComponent> components = JCasUtil.selectCovered(ArgumentComponent.class, para);
		return getPhraseTypeSequence(jcas, components);
	}
	
	
	public String getPhraseTypeSequence(JCas jcas, Collection<ArgumentComponent> components) {
		String sequence = "";
		
		for (ArgumentComponent comp : components) {
			String phraseType = getPhraseType(jcas, comp);
			sequence = sequence+phraseType+"_";
		}
		return sequence;
	}

	
	public boolean isThesisIndicatorPresent(JCas jcas, Collection<ArgumentComponent> components) {
		for (ArgumentComponent comp : components) {
			if (getPhraseType(jcas, comp).contains(THESIS_INDICATOR)) return true;
		}
		return false; 
	}
	
	
	public boolean isBwdIndicatorPresent(JCas jcas, Collection<ArgumentComponent> components) {
		for (ArgumentComponent comp : components) {
			if (getPhraseType(jcas, comp).contains(BWD_INDICATOR)) return true;
		}
		return false; 
	}
	
	
	public boolean isFwdIndicatorPresent(JCas jcas, Collection<ArgumentComponent> components) {
		for (ArgumentComponent comp : components) {
			if (getPhraseType(jcas, comp).contains(FWD_INDICATOR)) return true;
		}
		return false; 
	}
	
	
	public boolean isContIndicatorPresent(JCas jcas, Collection<ArgumentComponent> components) {
		for (ArgumentComponent comp : components) {
			if (getPhraseType(jcas, comp).contains(CONT_INDICATOR)) return true;
		}
		return false; 
	}
	
}
