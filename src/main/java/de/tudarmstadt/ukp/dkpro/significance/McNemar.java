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

public class McNemar {
	
	public static double getChiSquare(double correctWrong, double wrongCorrect) {
		return Math.pow(correctWrong-wrongCorrect, 2.0) / (correctWrong+wrongCorrect);
	}
	
	public static double getYatesCorrected(double correctWrong, double wrongCorrect) {
		return Math.pow(Math.abs(correctWrong-wrongCorrect)-0.5, 2.0) / (correctWrong+wrongCorrect);
	}
	
	public static double getEdwardsCorrected(double correctWrong, double wrongCorrect) {
		return Math.pow(Math.abs(correctWrong-wrongCorrect)-1.0, 2.0) / (correctWrong+wrongCorrect);
	}
}
