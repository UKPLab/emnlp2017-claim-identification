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

package de.tudarmstadt.ukp.dkpro.argumentation.io;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;

public class SentenceOutput {
	
	private static final String path = "/Users/zemes/Desktop/NLP/Papers/TACL2016/data/prep3/WTP";
	private static final String output = "/Users/zemes/Desktop/NLP/Papers/TACL2016/data/prep3/WTP.tsv";
	
	public static void main(String[] args) throws Exception {
		
		File dir = new File(path);
		
		double numSentences = 0.0;
		StringBuilder sb = new StringBuilder();
		for (File f : dir.listFiles()) {
			if (!f.getName().endsWith(".xmi")) continue;
			//System.out.println(f.getName());
			JCas cas = ArgUtils.readCas(f);
			
			DocumentMetaData meta = DocumentMetaData.get(cas);
			String id = meta.getDocumentId();
			
			
			Collection<Sentence> sentences = JCasUtil.select(cas, Sentence.class);
			int docSentence = 0;
			for (Sentence s : sentences) {
				boolean isClaim = false;
				if (ArgUtils.isClaim(s, cas)) isClaim = true;
				sb.append(id + "_" +docSentence + "\t");
				Collection<Token> tokens = JCasUtil.selectCovered(Token.class, s);
				for (Token t : tokens) {
					sb.append(t.getCoveredText().replace("\n", "") + " ");
				}
				
				if (isClaim) {
					sb.append("\t1\n");
				}
				else sb.append("\t0\n");
				docSentence++;
				numSentences++;
			}
		}
		
		writeFile(output, sb.toString());
		System.out.println("Sentences: " + numSentences);
	
	}
	
	public static void writeFile(String fileName, String content) throws Exception {
		BufferedWriter br = new BufferedWriter(new FileWriter(new File(fileName)));
		br.write(content);
		br.close();
	}	
	

}
