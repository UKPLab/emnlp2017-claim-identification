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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils;

import de.tudarmstadt.ukp.dkpro.argumentation.types.ArgumentComponent;
import de.tudarmstadt.ukp.dkpro.argumentation.types.Claim;
import de.tudarmstadt.ukp.dkpro.argumentation.types.MajorClaim;
import de.tudarmstadt.ukp.dkpro.argumentation.types.Premise;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClaimSentenceDist {
	
	private static final String path = "/Users/zemes/Desktop/NLP/Papers/TACL2016/data/gold.data.toulmin";
	
	public static void main(String[] args) throws Exception {
		
		File dir = new File(path);
		
		double numDocs = 0.0;
		double numSentences = 0.0;
		double numTokens = 0.0;
		double sentClaims = 0.0;
		double claims = 0.0;
		double sentPremises = 0.0;
		double premises = 0.0;
		double argComp = 0.0;
		
		for (File f : dir.listFiles()) {
			if (!f.getName().endsWith(".xmi")) continue;
			System.out.println(f.getName());
			JCas cas = readCas(f);
			
			numDocs++;
			numTokens += JCasUtil.select(cas, Token.class).size();
			Collection<Sentence> sentences = JCasUtil.select(cas, Sentence.class);
			
			for (Sentence s : sentences) {
				if (isClaim(s, cas)) sentClaims++;
				if (isPremise(s, cas)) sentPremises++;
				numSentences++;
			}
			
			claims += JCasUtil.select(cas, Claim.class).size();
			premises += JCasUtil.select(cas, Premise.class).size();
			argComp += JCasUtil.select(cas, ArgumentComponent.class).size();
		}
		
		System.out.println("#Documents = " + numDocs);
		System.out.println("#Sentences = " + numSentences);
		System.out.println("#Tokens    = " + numTokens);
		System.out.println("#Claims    = " + sentClaims + " (" + MessageFormat.format("{0,number,#.##%}", sentClaims / numSentences) + ")");
		System.out.println("#Premises  = " + sentPremises + " (" + MessageFormat.format("{0,number,#.##%}", sentPremises / numSentences) + ")");
		
		
		System.out.println("Clause-level claims   = " + claims);
		System.out.println("Clause-level premises = " + premises);
		System.out.println("Argument Components   = " + argComp);
	}
	
	
	public static boolean isClaim(Sentence s, JCas cas) {
		return selectOverlapping(Claim.class, s, cas).size()>0 || selectOverlapping(MajorClaim.class, s, cas).size()>0;
	}
	
	
	public static boolean isPremise(Sentence s, JCas cas) {
		return selectOverlapping(Premise.class, s, cas).size()>0;
	}
	
	
	public static <T extends TOP> List<T> selectOverlapping(Class<T> type,Annotation annotation, JCas jCas) {
        Collection<T> allAnnotations = JCasUtil.select(jCas, type);

        List<T> result = new ArrayList<T>();

        for (T a : allAnnotations) {
            if ((a instanceof Annotation) && (doOverlap(annotation, (Annotation) a))) {
                result.add(a);
            }
        }

        return result;
    }
	
	 
	public static <T extends Annotation> boolean doOverlap(final T anno1, final T anno2) {
		return anno1.getEnd() > anno2.getBegin() && anno1.getBegin() < anno2.getEnd();
	}
	
	
	public static JCas readCas(File f) throws ResourceInitializationException, SAXException, IOException, CASException {
		CAS loadedCas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(), null, null); 
		FileInputStream in = new FileInputStream(f); 
		XmiCasDeserializer.deserialize(in, loadedCas);
		IOUtils.closeQuietly(in);
		return loadedCas.getJCas();
	}

}
