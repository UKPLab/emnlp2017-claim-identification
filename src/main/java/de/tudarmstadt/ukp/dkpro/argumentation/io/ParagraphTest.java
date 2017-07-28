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
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;

public class ParagraphTest {
	
	private static final String path = "/Users/zemes/Desktop/NLP/Papers/TACL2016/data/prep4/WTP";
	
	public static void main(String[] args) throws Exception {
		
		File dir = new File(path);
		
		boolean containsParagraph = false;
		for (File f : dir.listFiles()) {
			if (!f.getName().endsWith(".xmi")) continue;
			JCas cas = ArgUtils.readCas(f);
			
			Collection<Paragraph> paras = JCasUtil.select(cas,Paragraph.class);
			if (paras.size()>0) {
				containsParagraph = true;
			}
			
			Collection<Sentence> sentences = JCasUtil.select(cas, Sentence.class);
			for (Sentence s : sentences) {
				Collection<Paragraph> para = JCasUtil.selectCovering(Paragraph.class, s);
				if (para.size()>1) System.out.println("Sentence has more than one paragraph");
				if (para.size()==0) System.out.println("Sentence has no paragraph");
			}
			
		}
		
		System.out.println(containsParagraph);
	
	}
	
	public static void writeFile(String fileName, String content) throws Exception {
		BufferedWriter br = new BufferedWriter(new FileWriter(new File(fileName)));
		br.write(content);
		br.close();
	}	
	

}
