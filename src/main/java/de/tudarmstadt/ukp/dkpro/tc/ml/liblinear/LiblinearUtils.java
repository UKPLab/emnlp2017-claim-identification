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
package de.tudarmstadt.ukp.dkpro.tc.ml.liblinear;

import de.bwaldvogel.liblinear.SolverType;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import static de.tudarmstadt.ukp.dkpro.tc.ml.liblinear.LiblinearTestTask.EPISILON_DEFAULT;
import static de.tudarmstadt.ukp.dkpro.tc.ml.liblinear.LiblinearTestTask.PARAM_C_DEFAULT;

public class LiblinearUtils
{
    public static SolverType getSolver(List<String> classificationArguments)
    {
        if (classificationArguments == null) {
            return SolverType.L2R_LR;
        }

        SolverType type = null;
        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);
            if (e.equals("-s")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-s] but no solver type was specified");
                }

                String algo = classificationArguments.get(i + 1);
                switch (algo) {
                case "0":
                    type = SolverType.L2R_LR;
                    break;
                case "1":
                    type = SolverType.L2R_L2LOSS_SVC_DUAL;
                    break;
                case "2":
                    type = SolverType.L2R_L2LOSS_SVC;
                    break;
                case "3":
                    type = SolverType.L2R_L1LOSS_SVC_DUAL;
                    break;
                case "4":
                    type = SolverType.MCSVM_CS;
                    break;
                case "5":
                    type = SolverType.L1R_L2LOSS_SVC;
                    break;
                case "6":
                    type = SolverType.L1R_LR;
                    break;
                case "7":
                    type = SolverType.L2R_LR_DUAL;
                    break;
                case "11":
                    type = SolverType.L2R_L2LOSS_SVR;
                    break;
                case "12":
                    type = SolverType.L2R_L2LOSS_SVR_DUAL;
                    break;
                case "13":
                    type = SolverType.L2R_L1LOSS_SVR_DUAL;
                    break;
                default:
                    throw new IllegalArgumentException("An unknown solver was specified [" + algo
                            + "] which is unknown i.e. check parameter [-s] in your configuration");
                }

            }
        }

        if (type == null) {
            // parameter -s was not specified in the parameters so we set a default value
            type = SolverType.L2R_LR;
        }

        LogFactory.getLog(LiblinearUtils.class).info("Will use solver " + type.toString() + ")");
        return type;
    }

    public static double getParameterC(List<String> classificationArguments)
    {
        if (classificationArguments == null) {
            return PARAM_C_DEFAULT;
        }

        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);
            if (e.equals("-c")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-c] but no value was specified");
                }

                Double value;
                try {
                    value = Double.valueOf(classificationArguments.get(i + 1));
                }
                catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "The value of parameter -c has to be a floating point value but was ["
                                    + classificationArguments.get(i + 1) + "]",
                            ex);
                }
                return value;
            }
        }

        LogFactory.getLog(LiblinearUtils.class)
                .info("Parameter c is set to default value [" + PARAM_C_DEFAULT + "]");
        return PARAM_C_DEFAULT;
    }

    public static double getParameterEpsilon(List<String> classificationArguments)
    {
        if (classificationArguments == null) {
            return EPISILON_DEFAULT;
        }

        for (int i = 0; i < classificationArguments.size(); i++) {
            String e = classificationArguments.get(i);
            if (e.equals("-e")) {
                if (i + 1 >= classificationArguments.size()) {
                    throw new IllegalArgumentException(
                            "Found parameter [-e] but no value was specified");
                }

                Double value;
                try {
                    value = Double.valueOf(classificationArguments.get(i + 1));
                }
                catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "The value of parameter -e has to be a floating point value but was ["
                                    + classificationArguments.get(i + 1) + "]",
                            ex);
                }
                return value;
            }
        }

        LogFactory.getLog(LiblinearUtils.class).info("Parameter epsilon is set to [0.01]");
        return EPISILON_DEFAULT;
    }

    public static String outcomeMap2String(Map<String, Integer> map)
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Integer> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("\t");
            sb.append(entry.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }

    public static Map<String, Integer> createMapping(boolean isRegression, File... files)
        throws IOException
    {
        if (isRegression) {
            return new HashMap<>();
        }
        Set<String> uniqueOutcomes = new HashSet<>();
        for (File f : files) {
            uniqueOutcomes.addAll(pickOutcomes(f));
        }

        Map<String, Integer> mapping = new HashMap<>();
        int id = 0;
        for (String o : uniqueOutcomes) {
            mapping.put(o, id++);
        }

        return mapping;
    }

    private static Collection<? extends String> pickOutcomes(File file)
        throws IOException
    {
        Set<String> outcomes = new HashSet<>();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "utf-8"));

        String line = null;
        while (((line = br.readLine()) != null)) {
            if (line.isEmpty()) {
                continue;
            }
            int firstTabIdx = line.indexOf("\t");
            outcomes.add(line.substring(0, firstTabIdx));
        }
        br.close();
        return outcomes;
    }

    public static String replaceOutcome(File file, Map<String, Integer> outcomeMapping)
        throws IOException
    {	
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "utf-8"));
        
        StringBuffer bw = new StringBuffer();

        String line = null;
        while (((line = br.readLine()) != null)) {
            if (line.isEmpty()) {
                continue;
            }
            int firstTabIdx = line.indexOf("\t");
            String val = map(outcomeMapping, line, firstTabIdx);
            bw.append(val + line.substring(firstTabIdx) + "\n");
        }
        br.close();
        return bw.toString();
    }

    private static String map(Map<String, Integer> outcomeMapping, String line, int firstTabIdx)
    {
        String s = line.substring(0, firstTabIdx);
        Integer integer = outcomeMapping.get(s);
        if (integer == null) {
            // regression mode
            return s;
        }

        return integer.toString();
    }

	public static File balanceAndConvert(String idMappedString, String balanceRandomSeed) throws IOException {
		// iff not requested, just write and return File
    	if(balanceRandomSeed == null || Integer.parseInt(balanceRandomSeed) == -1){
            File idMappedTestFile = File.createTempFile("liblinear" + System.nanoTime(), ".tmp");
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(idMappedTestFile), "utf-8"));
            bw.write(idMappedString);
            bw.close();
    		return idMappedTestFile;
    	}
    	// apply down-sampling on majority class
    	else {
    		Random random = new Random(Integer.parseInt(balanceRandomSeed));
            File idMappedTestFile = File.createTempFile("liblinear" + System.nanoTime(), ".tmp");
            
            List<String> claimDocs = new ArrayList<>();
            List<String> nonClaimDocs = new ArrayList<>();
            
            for (String line : idMappedString.split("\n")) {
                if (line.isEmpty()) {
                    continue;
                }
                int firstTabIdx = line.indexOf("\t");
                String val = line.substring(0, firstTabIdx);
                if(val.equals("0")){
                	claimDocs.add(val + line.substring(firstTabIdx));
                }
                else if(val.equals("1")){
                	nonClaimDocs.add(val + line.substring(firstTabIdx));
                }
                else {
                	throw new IOException("can't be applied to non-binary classification problems");
                }
            }
            
            // shuffle and balance
            Collections.shuffle(claimDocs, random);
            Collections.shuffle(nonClaimDocs, random);
            int maxLength = Math.min(claimDocs.size(), nonClaimDocs.size());

            // trim
            List<String> trimClaim = claimDocs.subList(0, maxLength);
            List<String> trimNonClaim = nonClaimDocs.subList(0, maxLength);
            
            // and make a final list of resources
            List<String> allDocs = new ArrayList<>();
            allDocs.addAll(trimClaim);
            allDocs.addAll(trimNonClaim);
            Collections.shuffle(allDocs, random);
            
            org.apache.commons.io.FileUtils.writeLines(idMappedTestFile, allDocs);
     		return idMappedTestFile;
    	}
	}
}