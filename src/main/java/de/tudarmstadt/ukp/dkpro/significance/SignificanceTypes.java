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

package de.tudarmstadt.ukp.dkpro.significance;

import de.tudarmstadt.ukp.dkpro.argumentation.types.ArgumentComponent;
import de.tudarmstadt.ukp.dkpro.argumentation.types.Claim;
import de.tudarmstadt.ukp.dkpro.argumentation.types.MajorClaim;
import de.tudarmstadt.ukp.dkpro.argumentation.types.Premise;
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
import java.util.ArrayList;
import java.util.Collection;

public class SignificanceTypes {
	
	public static final String gsPath = "<Path to Gold Standard-XMI>"; 
	public static final String s1Path = "<Path to prediction of System 1>";
	public static final String s2Path = "<Path to prediction of System 2>";
	
	
	private static int correctCorrect = 0;
	private static int correctWrong = 0;
	private static int wrongCorrect = 0;
	private static int wrongWrong=0;
	
	
	public static void main(String[] args) throws Exception {
		System.out.println(evaluateTypes(gsPath, s1Path, s2Path));
	}
	
	public static String evaluateTypes(String gsPath, String s1Path, String s2Path) throws Exception {
		System.out.print("  Evaluating Significance Types ...");
		File gsDir = new File(gsPath);
		for (File gsEssay : gsDir.listFiles()) {
			if (!gsEssay.getName().endsWith(".xmi")) continue;
			//System.out.println(gsEssay.getName());
			JCas gsCas   = readCas(new File(gsEssay.getAbsolutePath()));
			JCas s1Cas = readCas(new File(s1Path + "/" + gsEssay.getName()));
			JCas s2Cas = readCas(new File(s2Path + "/" + gsEssay.getName()));
			updateScores(gsCas, s1Cas, s2Cas);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Significane Report\n");
		sb.append(" a=" + correctCorrect + "\n");
		sb.append(" b=" + correctWrong + "\n");
		sb.append(" c=" + wrongCorrect + "\n");
		sb.append(" d=" + wrongWrong + "\n");
		
		double chiSquare = McNemar.getChiSquare(correctWrong, wrongCorrect);
		double yates = McNemar.getYatesCorrected(correctWrong, wrongCorrect);
		double edwards = McNemar.getEdwardsCorrected(correctWrong, wrongCorrect);
		
		sb.append("\np = 0.005\n");
		double chi = 7.879;
		sb.append("ChiSquare          = " + chiSquare  + "\t");
		sb.append((chiSquare>=chi)?"Significant (p=0.005)\n":"NotSignificant (p=0.005)\n");
		sb.append("Yates-Correction   = " + yates  + "\t");
		sb.append((yates>=chi)?"Significant (p=0.005)\n":"NotSignificant (p=0.005)\n");
		sb.append("Edwards-Correction = " + edwards  + "\t");
		sb.append((edwards>=chi)?"Significant (p=0.005)\n":"NotSignificant (p=0.005)\n");
		
		sb.append("\np = 0.05\n");
		chi = 3.841;
		sb.append("ChiSquare          = " + chiSquare  + "\t");
		sb.append((chiSquare>=chi)?"Significant (p=0.05)\n":"NotSignificant (p=0.05)\n");
		sb.append("Yates-Correction   = " + yates  + "\t");
		sb.append((yates>=chi)?"Significant (p=0.05)\n":"NotSignificant (p=0.05)\n");
		sb.append("Edwards-Correction = " + edwards  + "\t");
		sb.append((edwards>=chi)?"Significant (p=0.05)\n":"NotSignificant (p=0.05)\n");
		
		return sb.toString();
	}
	
	
	public static void updateScores(JCas gsCas, JCas s1Cas, JCas s2Cas) {
		
		Collection<ArgumentComponent> gsComps   = JCasUtil.select(gsCas, ArgumentComponent.class);
		Collection<ArgumentComponent> s1Comps = JCasUtil.select(s1Cas, ArgumentComponent.class);
		Collection<ArgumentComponent> s2Comps = JCasUtil.select(s2Cas, ArgumentComponent.class);
		
		
		ArrayList<String> gsLabels = getLabels(gsComps);
		ArrayList<String> s1Labels = getLabels(s1Comps);
		ArrayList<String> s2Labels = getLabels(s2Comps);
		
		for (int i=0; i<gsComps.size(); i++) {
			String gsLabel = gsLabels.get(i);
			String s1Label = s1Labels.get(i);
			String s2Label = s2Labels.get(i);
			
			if (s1Label.equals(gsLabel) && s2Label.equals(gsLabel)) correctCorrect++;
			if (s1Label.equals(gsLabel) && !s2Label.equals(gsLabel)) correctWrong++;
			if (!s1Label.equals(gsLabel) && s2Label.equals(gsLabel)) wrongCorrect++;
			if (!s1Label.equals(gsLabel) && !s2Label.equals(gsLabel)) wrongWrong++;
		}
	}
	
	
	public static ArrayList<String> getLabels(Collection<ArgumentComponent> comps) {
		ArrayList<String> result = new ArrayList<String>();
		
		for (ArgumentComponent comp : comps) { 
			String label = null; 
			if (comp instanceof MajorClaim) label = "MajorClaim";
			if (comp instanceof Claim) label = "Claim";
			if (comp instanceof Premise) label = "Premise";
			
			
			result.add(label);
		}
		
		return result;
	}
	
	public static JCas readCas(File f) throws ResourceInitializationException, SAXException, IOException, CASException {
		CAS loadedCas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(), null, null); 
		FileInputStream in = new FileInputStream(f); 
		XmiCasDeserializer.deserialize(in, loadedCas);
		IOUtils.closeQuietly(in);
		return loadedCas.getJCas();
	}
	

	
}
