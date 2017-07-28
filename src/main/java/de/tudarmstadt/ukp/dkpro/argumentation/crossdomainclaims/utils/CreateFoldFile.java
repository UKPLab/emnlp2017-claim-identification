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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.util.*;

public class CreateFoldFile {

	private static String corpusPath = "/Users/zemes/Desktop/NLP/Papers/TACL2016/data/prep4/WTP";

	private static String outputFile = "src/main/resources/folds-10CV/folds-WTP.tsv";
	
	/**
	 * number of folds k
	 */
	private static int folds = 10;
			
	/**
	 * number of instances
	 */
	private static int iterations = 1;
	
	/**
	 * this proportion of training set is used as development set
	 */
	private static double devSize = 0.1;
	
	/**
	 * seed for shuffeling
	 */
	private static long seed = 1234;
	
	
	public static void main(String[] args) throws Exception {
		
		// get instance ids and outcomes
		List<List<String>> instancesPerDocument = new ArrayList<List<String>>();
		HashMap<String,Boolean> labels = new HashMap<String, Boolean>();
		
		File path = new File(corpusPath);
		for (File f : path.listFiles()) {
			if (!f.getName().endsWith(".xmi")) continue;
			JCas cas = ArgUtils.readCas(f);
			String id = ArgUtils.getID(f);
			
			ArrayList<String> docInstances = new ArrayList<String>(); 
			
			int numInstance = 0;
			for (Sentence s : JCasUtil.select(cas, Sentence.class)) {
				String instanceID = id+"_"+numInstance;
				labels.put(instanceID, ArgUtils.isClaim(s, cas));
				docInstances.add(instanceID);
				numInstance++;
			}
			instancesPerDocument.add(docInstances); 
		}
		
		//System.out.println(instancesPerDocument.size());
		
		
		// Collect Documen folds
		Random rand = new Random(seed);
		HashMap<String, List<String>> documentFolds = new HashMap<String, List<String>>();
		
		for (int i=0; i<iterations; i++) {
			Collections.shuffle(instancesPerDocument, rand);
			addFold(folds,instancesPerDocument,documentFolds,rand);
		}
		
		
		// print distribution
		for (int i=0; i<folds*iterations; i++) {
			double numDoc = 0.0;
			double numTrain = 0.0;
			double numDev = 0.0;
			double numTest = 0.0;
			
			for (String key : documentFolds.keySet()) {
				if (documentFolds.get(key).get(i).equals("TEST")) numTest++;
				if (documentFolds.get(key).get(i).equals("TRAIN")) numTrain++;
				if (documentFolds.get(key).get(i).equals("DEV")) numDev++;
				numDoc++;
			}
			
			System.out.println("Split " + i + " (" + numDoc + ")");
			System.out.println("  Train: " + numTrain + "\t(" + (numTrain/numDoc) + ")");
			System.out.println("  Dev  : " + numDev + "\t(" + (numDev/numDoc) + ")");
			System.out.println("  Test : " + numTest + "\t(" + (numTest/numDoc) + ")");
		}
		
		
		// sysout write tsv-file
		StringBuilder sb = new StringBuilder();
		for (String key : documentFolds.keySet()) {
			sb.append(key);
			for (String set : documentFolds.get(key)) {
				sb.append("\t" + set);
			}
			sb.append("\n");
		}
		ArgUtils.writeFile(outputFile, sb.toString());
		
	}
	
	
	@SuppressWarnings("unchecked")
	public static void addFold(int k, List<List<String>> instancesPerDocument, HashMap<String, List<String>> folds, Random rand) {
		List<List<String>>[] buckets;
		buckets = new List[k];
		for(int bucket=0;bucket<buckets.length;bucket++){
			buckets[bucket] = new ArrayList<List<String>>();
		}
		
		int i = 0;
		for (List<String> doc : instancesPerDocument) {
			int bucket = i % k;
			
			buckets[bucket].add(doc);
			i++;
		}
		
		// split train test
		for (int j=0; j<k; j++) {
			for (int currentTest=0; currentTest<k;currentTest++) {
				
				if (j==currentTest) {
					// add doc in test set
					for (List<String> docInst : buckets[j]) {
						for (String instanceID : docInst) {
							if (!folds.containsKey(instanceID)) folds.put(instanceID, new ArrayList<String>()); 
							folds.get(instanceID).add("TEST");
						}
					}
					
				} else {
					// add doc in train set and randomly sample 10% for dev set
					for (List<String> docInst : buckets[j]) {
						String set = "DEV";
						if (rand.nextDouble()>devSize) {
							set = "TRAIN";
						}
						for (String instanceID : docInst) {
							if (!folds.containsKey(instanceID)) folds.put(instanceID, new ArrayList<String>()); 
							folds.get(instanceID).add(set);
						}
					}
				}
				
			}
			
		}
		
	}
	
}
