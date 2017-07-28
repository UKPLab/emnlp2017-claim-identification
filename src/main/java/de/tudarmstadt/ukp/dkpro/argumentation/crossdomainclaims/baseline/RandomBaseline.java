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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.baseline;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.utils.ConfusionMatrix;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.util.Collection;
import java.util.Random;

public class RandomBaseline {

	public static String corpusPath = "/Users/zemes/Desktop/NLP/Papers/TACL2016/data/prep4/WD";
	
	public static double p = 0.5;
	
	public static void main (String[] args) throws Exception {
		
		File dir = new File(corpusPath); 
		
		Random rand = new Random(12345);

		double[][] matrix = new double[2][2];
		
		for (File f : dir.listFiles()) {
			if (!f.getName().endsWith(".xmi")) continue;
			//System.out.println(f.getName());
			JCas cas = ArgUtils.readCas(f);
			
			Collection<Sentence> sentences = JCasUtil.select(cas, Sentence.class);
			
			int numInstance = 0;
		    for (Sentence s : sentences) {
		    	String id = ArgUtils.getID(f);
		    	String instanceID = id+"_"+numInstance;
				
				// get actual label
				String actualLabel = null;
		    	if (ArgUtils.isClaim(s, cas)) {
	    			actualLabel = "Claim";
		    	} else {
		    		actualLabel = "None";
		    	}
		    	
		    	String randLabel = null;
		    	if (rand.nextDouble()>p) {
		    		randLabel = "Claim";
		    	} else {
		    		randLabel ="None";
		    	}
		    	
		    	System.out.println(instanceID + "="+actualLabel+";"+randLabel);
		    	
		    	if (actualLabel.equals("Claim") && randLabel.equals("Claim")) matrix[0][0]++;
		    	if (actualLabel.equals("Claim") && randLabel.equals("None")) matrix[0][1]++;
		    	if (actualLabel.equals("None") && randLabel.equals("Claim")) matrix[1][0]++;
		    	if (actualLabel.equals("None") && randLabel.equals("None")) matrix[1][1]++;
		    	
		    	numInstance++;
		    }
		    
		}
		
		ConfusionMatrix confMartrix = new ConfusionMatrix(matrix);
	    confMartrix.setHeaders(new String[]{"Claim","Premise"});
	    
	    System.out.println(confMartrix.printEvalAll());
	    System.out.println(confMartrix.printEval());
	    
	}
	
}
