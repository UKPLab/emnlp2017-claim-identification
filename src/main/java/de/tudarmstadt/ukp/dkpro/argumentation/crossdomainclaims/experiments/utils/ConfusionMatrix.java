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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ConfusionMatrix {

	private String[] headers;
	
	private int size = -1;
	
	private double[][] matrix;
	
	
	public ConfusionMatrix(double[][] matrix) {
		this.size = matrix.length;
		this.matrix = matrix;
	}
	
	
	public ConfusionMatrix(int size) {
		this.size = size;
		matrix = new double[size][size];
	}
	
	
	public ConfusionMatrix(File f) throws IOException {
		FileReader in = new FileReader(f);
		BufferedReader br = new BufferedReader(in);
		String line = br.readLine();
		
		// get headers
		 
		String[] tmp = line.split(",");
		ArrayList<String> h = new ArrayList<String>();
		for (int i = 1; i<tmp.length; i++) {
			h.add(tmp[i].replace(" (pred.)", "").replaceAll("\"", "").trim());
		}
		
		headers = h.toArray(new String[h.size()]);
		
		size = headers.length;
		matrix = new double[size][size];
		
		int linecounter = 0;
		while ((line=br.readLine())!=null && !line.equals("")) {
			//System.out.println(line);
			String[] split = line.split(",");
			
			for (int i=1; i<size+1;i++) {
				double value = Double.parseDouble(split[i].replace("\"", ""));
				//System.out.println(value);
				matrix[linecounter][i-1] = value;
			}
				
			linecounter++;
		}
		br.close();
	}
	
	
	public String print() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				sb.append(matrix[i][j] + "\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	public void add(ConfusionMatrix m) {
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				matrix[i][j] += m.getValue(i, j); 
			}
		}
	}

	
	public double sum() {
		double sum = 0.0;
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				sum += matrix[i][j]; 
			}
		}
		return sum;
	}
	
	public String[] getHeaders() {
		return headers;
	}
	
	public void setHeaders(String[] headers) {
		this.headers = headers;
	}
	
	public int getIndexOf(String category) {
		for (int i=0; i<headers.length; i++) {
			if (headers[i].equals(category)) return i;
		}
		return -1;
	}
	
	public double getValue(int i, int j) {
		return matrix[i][j];
	}
	
	public double TP(String category) {
		int catIndex=getIndexOf(category);
		return matrix[catIndex][catIndex];
	}
	
	public double TP() {
		double tp = 0.0;
		for (int i=0; i<size; i++) {
			tp = tp + matrix[i][i];
		}
		
		return tp;
	}
	
	public double FP(String category) {
		int catIndex=getIndexOf(category);
		double fp = 0.0; 
		for (int i=0; i<size; i++) {
			if (i!=catIndex) fp = fp + matrix[i][catIndex];
		}
		return fp;
	}
	
	public double FP() {
		double fp = 0.0; 
		for (String s : headers) {
			fp = fp + FP(s);
		}
//		for (int i=0; i<size; i++) {
//			for (int j=i+1; j<size; j++) {
//				fp = fp + matrix[i][j];
//			}
//		}
		return fp;
	}
	
	public double FN(String category) {
		int catIndex=getIndexOf(category);
		double fn = 0.0; 
		for (int i=0; i<size; i++) {
			if (i!=catIndex) fn = fn + matrix[catIndex][i];
		}
		
		return fn;
	}
	
	public double FN() {
		double fn = 0.0; 
		for (String s : headers) {
			fn = fn + FN(s);
		}
//		for (int i=0; i<size; i++) {
//			for (int j=0; j<i; j++) {
//				fn = fn + matrix[i][j];
//			}
//		}
		
		return fn;
	}
	
	public double TN() {
		double tn = 0.0; 
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				tn = tn + matrix[i][j];
			}
		}
		
		return tn;
	}
	
	public double F(String category) {
		//double tp, fp, fn = 0.0;
		double tp = TP(category);
		double fp = FP(category);
		double fn = FN(category);
		return (2*tp)/(2*tp+fp+fn);
	}
	
	public double F() {
		double tp, fp, fn = 0.0;
		tp = TP();
		fp = FP();
		fn = FN();
		return (2*tp)/(2*tp+fp+fn);
	}
	
	public double P(String category) {
		if ((TP(category) + FP(category))==0) return 0.0;
		return TP(category) / (TP(category) + FP(category));
	}
	
	public double P() {
		return TP() / (TP() + FP());
	}
	
	public double Pmacro() {
		double psum = 0.0;
		for (String s : headers) {
			psum = psum + P(s);
		}
		return psum/headers.length;
	}
	
	public double Rmacro() {
		double rsum = 0.0;
		for (String s : headers) {
			rsum = rsum + R(s);
		}
		return rsum/headers.length;
	}
	
	public double Fmacro() {
		double rmacro = Rmacro();
		double pmacro = Pmacro();
		return 2.0*(rmacro*pmacro)/(rmacro+pmacro);
	}
	
	public double Fmacro_impl2() {
		double sumF1 = 0.0;
		for (String h : headers) {
			sumF1 += F(h);
		}
		return sumF1 / (double)headers.length;
	}
	
	public double R(String category) {
		return TP(category) / (TP(category) + FN(category));
	}
	
	public double R() {
		return TP() / (TP() + FN());
	}
	
	public double Acc() {
		return TP() / sum();
	}
	
	public String printEval(String category) {
		StringBuilder sb = new StringBuilder();
		sb.append("Category: " + category + "\n");
		sb.append("  F         = " + F(category) + "\n");
		sb.append("  precision = " + P(category) + "\n");
		sb.append("  recall    = " + R(category) + "\n");
		sb.append("  TP        = " + TP(category) + "\n");
		sb.append("  FP        = " + FP(category) + "\n");
		sb.append("  FN        = " + FN(category) + "\n");
//		double test = 2.0*(P(category)*R(category)) / (P(category)+R(category));
//		System.out.println("  TEST F    = " + test);
		return sb.toString();
	}
	
	public String printEvalAll() {
		StringBuilder sb = new StringBuilder();
		for (String category : headers) { 
			sb.append("Category: " + category + "\n");
			sb.append("  F         = " + F(category) + "\n");
			sb.append("  precision = " + P(category) + "\n");
			sb.append("  recall    = " + R(category) + "\n");
			sb.append("  TP        = " + TP(category) + "\n");
			sb.append("  FP        = " + FP(category) + "\n");
			sb.append("  FN        = " + FN(category) + "\n");
	//		double test = 2.0*(P(category)*R(category)) / (P(category)+R(category));
	//		System.out.println("  TEST F    = " + test);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public String printEval() {
		StringBuilder sb = new StringBuilder();
		sb.append("Overall Results (micro-avg)" + "\n");
		sb.append("  F         = " + F() + "\n");
//		System.out.println("  precision = " + P());
//		System.out.println("  recall    = " + R());
//		System.out.println("  TP        = " + TP());
//		System.out.println("  FP        = " + FP());
//		System.out.println("  FN        = " + FN());
		sb.append("  acc       = " + TP() / sum() + "\n\n");
//		double test = 2.0*(P()*R()) / (P()+R());
//		System.out.println("  TEST F    = " + test);
		sb.append("Overall Results (macro-avg)" + "\n");
		sb.append("  acc       = " + TP() / sum() + "\n");
		sb.append("  F[macro]  = " + Fmacro() + "\n");
		sb.append("  P[macro]  = " + Pmacro() + "\n");
		sb.append("  R[macro]  = " + Rmacro() + "\n\n");
		return sb.toString();
	}
	
	public static void main (String[] args) throws IOException {
		
		
		File path = new File("/Users/zemes/DEVELOPMENT/Experiments/Sufficiency/Results-MLP/matrices/confusionMatrix.csv");
		
		ConfusionMatrix result = new ConfusionMatrix(path);
		
//		ConfusionMatrix result = null;
//		
//		for (File f : path.listFiles()) {
//			if (!f.getName().endsWith(".csv")) continue;
//			ConfusionMatrix m = new ConfusionMatrix(f);
//			if (result==null) {
//				result = m;
//			} else {
//				result.add(m);
//			}
//			
//		}
		
		
		System.out.print(result.print());
		System.out.println(result.sum());
		System.out.println();
		System.out.println(result.printEvalAll());

		System.out.println(result.printEval());
	}
	
	
	public String getEvaluationReport() {
		StringBuilder sb = new StringBuilder();
		sb.append(print());
		sb.append(sum() + "\n\n");
		sb.append(printEvalAll());
		sb.append(printEval());
		return sb.toString();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\"ID\"");
		for (String h : headers) {
			sb.append(",\"" + h+ " (pred.)\"");
		}
		sb.append("\n");
		for (int i=0; i<size; i++) {
			String h = headers[i];
			sb.append("\"" + h + " (act.)\"");
			for (int j=0; j<size; j++) {
				sb.append(",\"" + matrix[i][j] + "\"");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
