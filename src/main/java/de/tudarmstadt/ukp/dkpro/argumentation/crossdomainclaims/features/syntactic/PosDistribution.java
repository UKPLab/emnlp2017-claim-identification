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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Christian Stab
 */
public class PosDistribution extends FeatureExtractorResource_ImplBase implements ClassificationUnitFeatureExtractor {

	public static final String FN_POS_DIST = "POS_DIST_";
	

    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
        List<Feature> featureList = new ArrayList<Feature>();
        Collection<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, classificationUnit);
//        Collection<Token> tokens = FeatureUtils.getComponentAndPrecedingTokens(jcas, classificationUnit);
        
        HashMap<String, Integer> map = createHashMap();
        
        // get source pos
        for (Token t : tokens) {
        	addPosToMap(t, map);
        }
       
        
        // add target features
        for (String pos : map.keySet()) {
        	featureList.add(new Feature(FN_POS_DIST+pos, map.get(pos).intValue()));
        }
        
        return featureList;
    }
    
    
    private void addPosToMap(Token t, HashMap<String,Integer> map) {
    	if (t.getPos()!=null) {
	    	String pos = t.getPos().getPosValue();
	    	if (map.containsKey(pos)) {
	    		map.put(pos, map.get(pos).intValue()+1);
	    	} else {
	    		if (pos.equals("``") || pos.equals("''") || pos.equals("\"") || pos.equals("'") || pos.equals("´")|| pos.equals("`")) {
	    			map.put("APOS", map.get("APOS").intValue()+1);
	    		} else 
	    		if (pos.equals(".")) {
	    			map.put("FUST", map.get("FUST").intValue()+1);
	    		} else 
	    		if (pos.equals(",")) {
	    			map.put("COM", map.get("COM").intValue()+1);
	    		} else 
	    		if (pos.equals("(") || pos.equals(")") || pos.equals("[") || pos.equals("]") || pos.equals("{") || pos.equals("}")) {
	    			map.put("BRACKET", map.get("BRACKET").intValue()+1);
	    		} else {
	    			map.put("OTHER", map.get("OTHER").intValue()+1);
	    		}
	    	}
    	} else {
    		map.put("OTHER", map.get("OTHER").intValue()+1);
    	}
    }
    
    
    private HashMap<String,Integer> createHashMap() {
    	HashMap<String,Integer> map = new HashMap<String, Integer>();
		map.put("JJ",0);  
		map.put("RB",0);
		map.put("TO",0);
		map.put("DT",0);
		map.put("RP",0);
		map.put("RBR",0);
		map.put("RBS",0);
		map.put("JJS",0);
		map.put("FW",0);
		map.put("JJR",0);
		map.put("NN",0);
		map.put("NNPS",0);
		map.put("VBN",0);
		map.put("VB",0);
		map.put("PDT",0);
		map.put("VBP",0);
		map.put("PRP",0);
		map.put("SYM",0);
		map.put("MD",0);
		map.put("WDT",0);
		map.put("VBZ",0);
		map.put("WP",0);
		map.put("IN",0);
		map.put("POS",0);
		map.put("EX",0);
		map.put("VBG",0);
		map.put("VBD",0);
		map.put("PRP$",0);
		map.put("NNS",0);
		map.put("CC",0);		
		map.put("NNP",0);
		map.put("CD",0);
		map.put("WRB",0);
		map.put("LS",0);
		map.put("UH",0);
		map.put("WDT",0);
		map.put("WP",0);
		map.put("WP$",0);
		
		map.put("APOS", 0);  // apostroph
		map.put("FUST", 0);  // full stop
		map.put("COM", 0);   // comma
		map.put("BRACKET", 0); // all kinds of brackets
		map.put("OTHER", 0); 
		return map;
    }
}
