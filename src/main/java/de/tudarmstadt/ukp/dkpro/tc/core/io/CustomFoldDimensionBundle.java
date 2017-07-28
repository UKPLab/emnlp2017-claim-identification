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

package de.tudarmstadt.ukp.dkpro.tc.core.io;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DynamicDimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class CustomFoldDimensionBundle<T> extends FoldDimensionBundle<T> {

	private Dimension<T> foldedDimension;
	private List<T>[] test;
	private List<T>[] train;
	private int validationBucket = -1;
	
	private String foldFile;
	
	private boolean useDevSet = false;
    
	public CustomFoldDimensionBundle(String aName, Dimension<T> aFoldedDimension, String aFoldFile, boolean useDevSet) {
		super(aName, aFoldedDimension, 5);
		foldedDimension = aFoldedDimension;
		foldFile = aFoldFile;
		this.useDevSet = useDevSet;
	}
	
	@SuppressWarnings("unchecked")
	private void init() {
		
		// get mapping from file names to ids
		HashMap<String, T> idToPath = new HashMap<String, T>();
		while (foldedDimension.hasNext()) {
			T path = foldedDimension.next();
			//String currentID = getEssayIDFromPath((String)path).replace("_0","");
			String currentID = ArgUtils.getID((String)path);
			System.out.println(currentID);
			idToPath.put(currentID, path);
		}
		
		// read fold file in the following format <instance id, list of folds>
		HashMap<String, String[]> lists = new HashMap<String, String[]>();
		int folds = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(foldFile));
			
			String line ="";
			while ((line=br.readLine())!=null) {
				String[] split = line.split("\t");
				folds = split.length-1;
				lists.put(split[0], split);
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		test = new List[folds];
		train = new List[folds];
		for (int i=0; i<folds; i++) {
			ArrayList<T> currentTest = new ArrayList<T>();
			ArrayList<T> currentTrain = new ArrayList<T>();
			
			for (String id : lists.keySet()) {
				String[] list = lists.get(id);
				if (list[i+1].equals("TRAIN")) {
					currentTrain.add(idToPath.get(id));
				}
				if (useDevSet) {
					if (list[i+1].equals("DEV")) {
						currentTest.add(idToPath.get(id));
					}
				} else {
					if (list[i+1].equals("TEST")) {
						currentTest.add(idToPath.get(id));
					}
				}
			}
			
			test[i] = currentTest;
			train[i] = currentTrain;
		}
		
	}
	
	
	@Override
	public Map<String, Collection<T>> current() {
		Map<String, Collection<T>> data = new HashMap<String, Collection<T>>();
		data.put(getName()+"_training", train[validationBucket]);
		data.put(getName()+"_validation", test[validationBucket]);
		
		return data;
	}
	

	@Override
	public boolean hasNext() {
		return validationBucket < train.length-1;
	}

	
	@Override
	public void rewind() {
		init();
		validationBucket = -1;
	}

	
	@Override
	public Map<String, Collection<T>> next() {
		validationBucket++;
		return current();
	}


	@Override
	public void setConfiguration(Map<String, Object> aConfig) {
		if (foldedDimension instanceof DynamicDimension) {
			((DynamicDimension) foldedDimension).setConfiguration(aConfig);
		}
	}
	
//	private String getEssayIDFromPath(String path) {
//		String[] split = path.split("/");
//		String fileName = split[split.length-1];
//		String[] split2 = fileName.split("\\.");
//		
//		return split2[0];
//	}
}
