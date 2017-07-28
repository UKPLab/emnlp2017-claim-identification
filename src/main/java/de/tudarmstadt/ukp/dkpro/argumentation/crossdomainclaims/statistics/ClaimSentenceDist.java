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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.statistics;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;

public class ClaimSentenceDist {
	
	private static final String path = "/Users/zemes/Desktop/NLP/Papers/TACL2016/data/prep3/VG";
	
	public static void main(String[] args) throws Exception {
		
		File dir = new File(path);
		
		double numDocs = 0.0;
		double numSentences = 0.0;
		double numTokens = 0.0;
		double sentClaims = 0.0;
		double sentPremises = 0.0;
		
		for (File f : dir.listFiles()) {
			if (!f.getName().endsWith(".xmi")) continue;
			System.out.println(f.getName());
			JCas cas = ArgUtils.readCas(f);
			
			numDocs++;
			numTokens += JCasUtil.select(cas, Token.class).size();
			Collection<Sentence> sentences = JCasUtil.select(cas, Sentence.class);
			
			for (Sentence s : sentences) {
				if (ArgUtils.isClaim(s, cas)) sentClaims++;
				if (ArgUtils.isPremise(s, cas)) sentPremises++;
				numSentences++;
			}
			
		}
		
		System.out.println("#Documents = " + numDocs);
		System.out.println("#Sentences = " + numSentences);
		System.out.println("#Tokens    = " + numTokens);
		System.out.println("#Claims    = " + sentClaims + " (" + MessageFormat.format("{0,number,#.##%}", sentClaims / numSentences) + ")");
		System.out.println("#Premises  = " + sentPremises + " (" + MessageFormat.format("{0,number,#.##%}", sentPremises / numSentences) + ")");
		
	}
	
	
	

}
