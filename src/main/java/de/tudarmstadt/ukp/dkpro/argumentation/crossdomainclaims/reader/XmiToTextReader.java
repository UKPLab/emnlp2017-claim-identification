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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.reader;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

public class XmiToTextReader {

	public static String path = "/Users/zemes/Desktop/NLP/Papers/TACL2016/data/prep3/Stab201X";
	
	public static void main(String[] args) throws Exception {
		File dir = new File(path);
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".xmi")) {
				JCas cas = readCas(f);
				
				String id = ArgUtils.getID(f);
				
				Collection<Paragraph> paragraphs = JCasUtil.select(cas, Paragraph.class);
				
				System.out.println("paragraphs:");
				for (Paragraph p : paragraphs) {
					System.out.println(p.getCoveredText());
				}
				
				
				int numInstance = 0;
				for (Sentence s : JCasUtil.select(cas, Sentence.class)) {
					// unique instance ID (see fold files)
					String instanceID = id+"_"+numInstance;
					
					// label of the sentence
					String label = ArgUtils.isClaim(s, cas)?"Claim":"NoClaim";
					
					
					System.out.println(instanceID + " (Label:" + label + ") " + s.getCoveredText());
					
					Collection<Token> tokens = JCasUtil.selectCovered(Token.class, s);
					for (Token t : tokens) {
						System.out.println(t.getCoveredText());
					}
					
					
//					ArgumentComponent
//						Claim
//						Premise
//						MajorClaim
//					ArgumentRelation
//						Support
//						Attack
					
					numInstance++;
				}
			}
		}
	}

	
	public static JCas readCas(File f) throws ResourceInitializationException, SAXException, IOException, CASException {
		CAS loadedCas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(), null, null); 
		FileInputStream in = new FileInputStream(f); 
		XmiCasDeserializer.deserialize(in, loadedCas);
		IOUtils.closeQuietly(in);
		return loadedCas.getJCas();
	}
	
}
