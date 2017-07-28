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

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaClassificationReport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExperimentUtils
{

    public final static String MATRIX_PATH = "/matrices/";
    public final static String OUTCOME_PATH = "/outcome/";
    public final static String PREDICTIONS_PATH = "/predictions/";

    public final static String EVAL_FILE = "evaluation.txt";
    public final static String HTML_COMP_FILE = "comparison.html";
    private static final Pattern PATTERN = Pattern.compile("\\d{3}");

    public static File getConfusionMatrix(String resultPath, int mode)
    {
        File f = new File(resultPath + "/de.tudarmstadt.ukp.dkpro.lab/repository");

        for (File subFolder : f.listFiles()) {
            if (mode == 0) {
                // CV
                if (subFolder.getName().startsWith("ExperimentCrossValidation")) {
                    return new File(subFolder.getAbsolutePath() + "/"
                            + WekaClassificationReport.CONFUSIONMATRIX_KEY);
                }
            }
            if (mode == 1) {
                // TT
                if (subFolder.getName().contains("TestTask")) {
                    return new File(subFolder.getAbsolutePath() + "/"
                            + WekaClassificationReport.CONFUSIONMATRIX_KEY);
                }
            }

        }
        return null;
    }

    public static File getIDToOutcome(String resultPath, int mode)
    {
        File f = new File(resultPath + "/de.tudarmstadt.ukp.dkpro.lab/repository");

        for (File subFolder : f.listFiles()) {
            if (mode == 0) {
                if (subFolder.getName().startsWith("ExperimentCrossValidation")) {
                    return new File(subFolder.getAbsolutePath() + "/id2outcome.txt");
                }
            }
            if (mode == 1) {
                if (subFolder.getName().contains("TestTask")) {
                    return new File(subFolder.getAbsolutePath() + "/id2outcome.txt");
                }
            }
        }
        return null;
    }

    public static void evaluate(String resultFilePath, int mode)
    {
        if (mode != 2) {
            File confusionMatrix = ExperimentUtils.getConfusionMatrix(resultFilePath, mode);
            File idOutcomeFile = ExperimentUtils.getIDToOutcome(resultFilePath, mode);

            try {
                FileUtils.copyFile(confusionMatrix, new File(
                        resultFilePath + "/" + ExperimentUtils.MATRIX_PATH + "/" + confusionMatrix
                                .getName()));
                FileUtils.copyFile(idOutcomeFile, new File(
                        resultFilePath + "/" + ExperimentUtils.OUTCOME_PATH + "/" + idOutcomeFile
                                .getName()));
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            // write results file
            StringBuilder sb = new StringBuilder();
            ConfusionMatrix result = null;

            try {
                File path = new File(resultFilePath + "/" + ExperimentUtils.MATRIX_PATH);
                ConfusionMatrix m = null;
                for (File f : path.listFiles()) {
                    if (!f.getName().endsWith(".csv"))
                        continue;
                    ConfusionMatrix n = new ConfusionMatrix(f);
                    if (m == null) {
                        m = n;
                    }
                    else {
                        m.add(n);
                    }
                    if (result == null) {
                        result = m;
                    }
                    else {
                        result.add(m);
                    }

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            sb.append(result.print());
            sb.append(result.sum() + "\n");
            sb.append(result.printEvalAll());
            sb.append(result.printEval());
            try {
                BufferedWriter br = new BufferedWriter(
                        new FileWriter(new File(resultFilePath + "/" + ExperimentUtils.EVAL_FILE)));
                br.write(sb.toString());
                br.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {
            File f = new File(resultFilePath + "/de.tudarmstadt.ukp.dkpro.lab/repository");
            File[] files = f.listFiles();

            // sort by date to ensure correct order of folds
            // this sorting is required if you aim to do significance testing later
            // it ensures that you are comparing the same runs from two different systems
            Arrays.sort(files, new Comparator<File>()
            {
                public int compare(File f1, File f2)
                {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });

            try {
                //copy confusion matrix
                int cmCounter = 1;
                for (File subFolder : files) {
                    if (subFolder.getName().contains("TestTask")) {
                        FileUtils.copyFile(new File(subFolder.getAbsoluteFile() + "/"
                                + WekaClassificationReport.CONFUSIONMATRIX_KEY), new File(
                                resultFilePath + "/" + ExperimentUtils.MATRIX_PATH
                                        + "/confusionMatrix" + String.format("%03d", cmCounter)
                                        + ".csv"));
                        cmCounter++;
                    }
                }

                //copy outcome
                int oCounter = 1;
                for (File subFolder : files) {
                    if (subFolder.getName().contains("TestTask")) {
                        FileUtils
                                .copyFile(new File(subFolder.getAbsoluteFile() + "/id2outcome.txt"),
                                        new File(resultFilePath + "/" + ExperimentUtils.OUTCOME_PATH
                                                + "/id2outcome" + String.format("%03d", oCounter)
                                                + ".txt"));
                        oCounter++;
                    }
                }

                // get scores
                StringBuilder sb = new StringBuilder();
                double sumAcc = 0.0;
                double sumF1 = 0.0;
                File matrices = new File(resultFilePath + "/" + ExperimentUtils.MATRIX_PATH);
                double folds = 0.0;
                for (File cm : matrices.listFiles()) {
                    if (!cm.getName().endsWith(".csv"))
                        continue;
                    ConfusionMatrix c = new ConfusionMatrix(cm);
                    sb.append(c.Fmacro() + "\n");
                    sumF1 += c.Fmacro();
                    sumAcc += c.Acc();
                    folds++;
                }
                NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
                percentFormat.setMaximumFractionDigits(3);
                String results = "Accuracy = " + (percentFormat.format(sumAcc / folds)) + "\n";
                results += "Macro-F1 = " + (percentFormat.format(sumF1 / folds));
                System.out.println(results);
                ArgUtils.writeFile(resultFilePath + "/Results.txt", results);
                ArgUtils.writeFile(resultFilePath + "/MacroF1Scores.txt", sb.toString());

            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public static File createCombinedIdOutcomeInSequenceClassification(String experimentPath)
            throws IOException
    {
        // copy outcomes
        String source = experimentPath + "/de.tudarmstadt.ukp.dkpro.lab/repository";
        String outcomePath = experimentPath + "/outcomes/";

        File sourceDir = new File(source);

        int count = 0;
        for (File subfolder : sourceDir.listFiles()) {
            if (subfolder.isDirectory()) {
                File testee = new File(subfolder + "/" + "id2outcome.txt");
                if (testee.exists() && subfolder.getName().startsWith("CRFSuiteTestTask")) {
                    count++;
                    //String essayName = getEssayName(subfolder);
                    FileUtils.copyFile(testee,
                            new File(outcomePath + "/id2outcome_" + count + ".txt"));
                }
            }
        }
        System.out.println("Found " + count + " outcome files");

        // Create combined outcome file
        File combinedOutcome = new File(experimentPath + "/id2outcome-all.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(combinedOutcome));

        File outcomes = new File(outcomePath);
        boolean isFirstFile = true;
        for (File f : outcomes.listFiles()) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#"))
                    bw.write(line + "\n");
                if (line.startsWith("#") && isFirstFile)
                    bw.write(line + "\n");
            }
            isFirstFile = false;
            br.close();
        }
        bw.close();
        return combinedOutcome;
    }

    /**
     * For each experiment in the sub-folder, creates 100 confusion matrices from 100 id2outcome
     * files and saves to the output folder under the same structure.
     *
     * @param inputDir  input dir
     * @param outputDir output dir
     * @throws Exception exception
     */
    public static void evaluateInDomainExperimentsCluster(File inputDir, File outputDir)
            throws Exception
    {
        // create output dir
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IOException("Cannot create output dir " + outputDir);
            }
        }

        // list over all experiments
        for (String dirName : inputDir.list(DirectoryFileFilter.INSTANCE)) {
            File dir = new File(inputDir, dirName);

            // collect all id2outcome files
            Collection<File> id2outcomeFiles = FileUtils
                    .listFiles(dir, new RegexFileFilter("id2outcome.*\\.txt"), null);

            if (id2outcomeFiles.isEmpty()) {
                // ok, there might be just one file hidden in "outcome"

                id2outcomeFiles = FileUtils
                        .listFiles(new File(dir, "outcome"),
                                new RegexFileFilter("id2outcome.*\\.txt"), null);
            }
            // sanity check for in-domain cross-validation
            else if (id2outcomeFiles.size() != 10) {
                throw new IllegalArgumentException(
                        "Expected 10 files but " + id2outcomeFiles.size() + " found");
            }

            if (id2outcomeFiles.isEmpty()) {
                throw new IllegalArgumentException("No id2outcome.txt file found under " + dir);
            }

            // create corresponding output dir
            File experimentOutputDir = new File(outputDir, dirName);
            if (!experimentOutputDir.exists() && !experimentOutputDir.mkdir()) {
                throw new IOException("Cannot create dir " + experimentOutputDir);
            }

            // create corresponding output dir
            File experimentOutputDirMajority = new File(outputDir,
                    dirName.split("_")[0] + "_allNone");
            if (!experimentOutputDirMajority.exists() && !experimentOutputDirMajority.mkdir()) {
                throw new IOException("Cannot create dir " + experimentOutputDirMajority);
            }

            // read all predictions
            for (File id2outcomeFile : id2outcomeFiles) {

                // update confusion matrix
                double[][] matrix = new double[2][2];

                // for majority classifier
                double[][] matrixAllNone = new double[2][2];

                for (String line : FileUtils.readLines(id2outcomeFile)) {
                    if (!line.startsWith("#")) {
                        String[] split = line.split("=")[1].split(";");
                        int prediction = Integer.valueOf(split[0]);
                        int gold = Integer.valueOf(split[1]);
                        matrix[gold][prediction]++;

                        matrixAllNone[gold][1]++;
                    }
                }

                // create instance
                ConfusionMatrix cm = new ConfusionMatrix(matrix);
                cm.setHeaders(new String[] { "Claim", "None" });
                System.out.println(cm);

                // create majority vote baseline
                ConfusionMatrix cmAllNone = new ConfusionMatrix(matrixAllNone);
                cmAllNone.setHeaders(new String[] { "Claim", "None" });
                System.out.println(cmAllNone);

                // and save it to file
                Matcher matcher = PATTERN.matcher(id2outcomeFile.getName());
                if (!matcher.find()) {
                    throw new IllegalStateException("Cannot find fold number");
                }
                String foldNumber = matcher.group(0);

                File outputFile = new File(experimentOutputDir,
                        "confusionMatrix" + foldNumber + ".csv");
                FileUtils.writeStringToFile(outputFile, cm.toString());

                // and the majority dir
                File outputFileAllNone = new File(experimentOutputDirMajority,
                        "confusionMatrix" + foldNumber + ".csv");
                FileUtils.writeStringToFile(outputFileAllNone, cmAllNone.toString());
            }
        }

    }

    /**
     * Input dir: all experiment outputs repeated
     * <pre>
     *     Reed2008_lexical_01
     *     Reed2008_lexical_02
     *     Reed2008_lexical_03
     *     ...
     * </pre>
     * Output dir: Reed2008_lexical (containing a single confusion matrix)
     *
     * @param mainInDir
     * @param mainOutDir
     * @param crossDomain
     * @param randomPredictions
     * @param claimIsZero       in some experiments claim was labeled as 0, in other 1
     */
    private static void evaluateRepeatedResultsCluster(File mainInDir, File mainOutDir,
            boolean crossDomain, boolean randomPredictions, boolean claimIsZero)
            throws IOException
    {
        if (!mainInDir.exists() || !mainOutDir.exists()) {
            throw new IOException("In/our dirs must exist");
        }

        File[] allExperiments = mainInDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
        if (allExperiments == null) {
            throw new IllegalArgumentException(mainInDir.getName() + " is empty?");
        }

        // collect all 20 repeats for experiments
        SortedMap<String, SortedMap<Integer, File>> experimentsWithRepeatsMap = new TreeMap<>();

        for (File dir : allExperiments) {
            // split name
            String[] split = dir.getName().split("_");

            // experiment name, i.e. Reed2007_lexical
            String name;
            Integer repeat;

            // different for cross-domain
            if (crossDomain) {
                name = split[0] + "_" + split[1] + "_" + split[2];
                repeat = Integer.valueOf(split[3]);
            }
            else {
                name = split[0] + "_" + split[1];
                repeat = Integer.valueOf(split[2]);
            }

            // update the map
            if (!experimentsWithRepeatsMap.containsKey(name)) {
                experimentsWithRepeatsMap.put(name, new TreeMap<Integer, File>());
            }
            experimentsWithRepeatsMap.get(name).put(repeat, dir);

            //            System.out.println("Adding " + name);
        }
        System.out.println(experimentsWithRepeatsMap.size() + " experiments in total");

        // sanity check - each experiment has 20 repeats
        for (Map.Entry<String, SortedMap<Integer, File>> entry : experimentsWithRepeatsMap
                .entrySet()) {
            //            System.out.println("Checking " + entry.getValue().size());
            if (entry.getValue().size() != 20) {
                throw new IllegalArgumentException(
                        "Expected 20 folders for " + entry.getKey() + " but got only " + entry
                                .getValue().size());
            }
        }
        //        System.out.println("done");

        // now merge results
        for (Map.Entry<String, SortedMap<Integer, File>> entry : experimentsWithRepeatsMap
                .entrySet()) {
            // collect predictions into a table; row = line number/inst.ID (across all lines of 10-fold CV
            // column = experiment repeat (1-20), value = integer from id2outcome
            // (0 = Claim, 1 = None)
            Table<String, Integer, Integer> predictionsTable = TreeBasedTable.create();

            // gold standard (key = line number because we don't have instance IDs for CD;
            // or ID for CV)
            SortedMap<String, Integer> goldStandard = new TreeMap<>();

            for (Map.Entry<Integer, File> singleEntry : entry.getValue().entrySet()) {
                File singleExperimentDir = singleEntry.getValue();
                Integer experimentRepeat = singleEntry.getKey();

                // iterate over all id2outcome files
                File[] outcomes = new File(singleExperimentDir, "outcome")
                        .listFiles((FileFilter) FileFileFilter.FILE);
                if (outcomes == null) {
                    throw new IllegalArgumentException();
                }

                // sort them
                SortedSet<File> outcomeFiles = new TreeSet<>(Arrays.asList(outcomes));

                // read all lines
                List<String> lines = new ArrayList<>();
                for (File singleId2Outcome : outcomeFiles) {
                    LineIterator lineIterator = FileUtils.lineIterator(singleId2Outcome);
                    while (lineIterator.hasNext()) {
                        String line = lineIterator.nextLine();
                        if (!line.startsWith("#")) {
                            lines.add(line);
                        }
                    }
                }

                System.out.println("Read " + lines.size() + " lines from " + singleExperimentDir);

                // extract the predicted values and fill the columns
                for (int lineNo = 0; lineNo < lines.size(); lineNo++) {
                    // for cross-domain, we have no IDs, so each line number corresponds to one
                    String instId = String.valueOf(lineNo);

                    String line = lines.get(lineNo);
                    String[] split = line.split("=");
                    int prediction = Integer.valueOf(split[1].split(";")[0]);
                    int gold = Integer.valueOf(split[1].split(";")[1]);

                    // we have instance IDs for cross-validation
                    if (!crossDomain) {
                        instId = split[0];
                    }

                    // update and sanity check gold standard
                    if (goldStandard.containsKey(instId)) {
                        if (!goldStandard.get(instId).equals(gold)) {
                            throw new IllegalStateException("Gold standard for line no " + lineNo
                                    + " differs across experiment runs! " + entry.getKey() + ", "
                                    + singleEntry);
                        }
                    }
                    else {
                        goldStandard.put(instId, gold);
                    }

                    // add prediction
                    predictionsTable.put(instId, experimentRepeat, prediction);
                }

                System.out.println(predictionsTable);
            }

            // we have 20 predictions for each instance (row), so now do the majority vote
            SortedMap<String, Integer> predictions = new TreeMap<>();
            for (String rowKey : predictionsTable.rowKeySet()) {
                Collection<Integer> row = predictionsTable.row(rowKey).values();

                int prediction = majorityVote(row, randomPredictions);
                //                System.out.println("Got " + prediction);

                predictions.put(rowKey, prediction);
            }

            // ok, we're ready to go, just make sure the size of predictions

            // create corresponding output dir
            File experimentOutputDir = new File(mainOutDir, entry.getKey());
            if (!experimentOutputDir.exists() && !experimentOutputDir.mkdir()) {
                throw new IOException("Cannot create dir " + experimentOutputDir);
            }

            // create corresponding output dir
            File experimentOutputDirMajority = new File(mainOutDir,
                    entry.getKey().split("_")[0] + "_allNone");
            if (!experimentOutputDirMajority.exists() && !experimentOutputDirMajority.mkdir()) {
                throw new IOException("Cannot create dir " + experimentOutputDirMajority);
            }

            // create corresponding output dir
            File experimentOutputDirRand = new File(mainOutDir,
                    entry.getKey().split("_")[0] + "_rand");
            if (!experimentOutputDirRand.exists() && !experimentOutputDirRand.mkdir()) {
                throw new IOException("Cannot create dir " + experimentOutputDirRand);
            }

            // update confusion matrix
            double[][] matrix = new double[2][2];

            // for majority classifier
            double[][] matrixAllNone = new double[2][2];

            // for random classifier
            double[][] matrixRand = new double[2][2];

            // writing outcome2id
            StringWriter swOutcome2IdMajority = new StringWriter();
            StringWriter swOutcome2IdRandom = new StringWriter();
            StringWriter swOutcome2IdClassifier = new StringWriter();

            // write the header first
            for (StringWriter sw : Arrays
                    .asList(swOutcome2IdMajority, swOutcome2IdRandom, swOutcome2IdClassifier)) {
                sw.write("#ID=PREDICTION;GOLDSTANDARD\n");
            }

            for (String instanceKey : predictions.keySet()) {

                int prediction = predictions.get(instanceKey);
                int gold = goldStandard.get(instanceKey);

                matrix[gold][prediction]++;

                // majority baseline
                int majorityPrediction = claimIsZero ? 1 : 0;
                matrixAllNone[gold][majorityPrediction]++;

                // random prediction
                int randomPrediction = (int) Math.round(Math.random());
                matrixRand[gold][randomPrediction]++;

                // outcome2id for each of them
                swOutcome2IdClassifier.write(instanceKey + ";" + prediction + ";" + gold + "\n");
                swOutcome2IdRandom.write(instanceKey + ";" + randomPrediction + ";" + gold + "\n");
                swOutcome2IdMajority
                        .write(instanceKey + ";" + majorityPrediction + ";" + gold + "\n");
            }

            String[] headers = claimIsZero ?
                    new String[] { "Claim", "None" } :
                    new String[] { "None", "Claim" };

            // create instance
            ConfusionMatrix cm = new ConfusionMatrix(matrix);
            cm.setHeaders(headers);
            System.out.println(cm);

            // create majority vote baseline
            ConfusionMatrix cmAllNone = new ConfusionMatrix(matrixAllNone);
            cmAllNone.setHeaders(headers);
            System.out.println(cmAllNone);

            // create random baseline
            ConfusionMatrix cmRand = new ConfusionMatrix(matrixRand);
            cmRand.setHeaders(headers);
            System.out.println(cmRand);

            // and save it to file
            File outputFile = new File(experimentOutputDir, "confusionMatrix.csv");
            FileUtils.writeStringToFile(outputFile, cm.toString());
            File outputFileId = new File(experimentOutputDir, "id2outcome.txt");
            FileUtils.writeStringToFile(outputFileId, swOutcome2IdClassifier.toString());

            // and the majority dir
            File outputFileAllNone = new File(experimentOutputDirMajority, "confusionMatrix.csv");
            FileUtils.writeStringToFile(outputFileAllNone, cmAllNone.toString());
            File outputFileIdAllNone = new File(experimentOutputDirMajority, "id2outcome.txt");
            FileUtils.writeStringToFile(outputFileIdAllNone, swOutcome2IdMajority.toString());

            // and the majority dir
            File outputFileRand = new File(experimentOutputDirRand, "confusionMatrix.csv");
            FileUtils.writeStringToFile(outputFileRand, cmRand.toString());
            File outputFileIdRand = new File(experimentOutputDirRand, "id2outcome.txt");
            FileUtils.writeStringToFile(outputFileIdRand, swOutcome2IdRandom.toString());
        }

    }

    private static Integer majorityVote(Collection<Integer> votes, boolean randomPredictions)
    {
        //        System.out.println(votes);
        if (votes.size() != 20) {
            throw new IllegalArgumentException("Expected 20 votes but got " + votes.size());
        }

        int zeros = 0;
        int ones = 0;
        for (Integer i : votes) {
            Integer currentValue = i;

            // or we can replace it by a random int
            if (randomPredictions) {
                currentValue = RANDOM.nextInt(2);
            }

            if (currentValue.equals(1)) {
                ones++;
            }
            else if (currentValue.equals(0)) {
                zeros++;
            }
            else {
                throw new IllegalStateException();
            }
        }

        if (zeros > ones) {
            return 0;
        }

        if (ones > zeros) {
            return 1;
        }

        // it's a tie... fall-back: 1 (no-claim)
        return 1;
    }

    private static final Random RANDOM = new Random(1);

    public static void main(String[] args)
            throws Exception
    {
        //		evaluate(args[0], Integer.valueOf(args[1]));
        //        String inDir = args[0]; // "/mnt/hpc-project/output/"
        //        String outDir = args[1]; // "/tmp/out/"
        //        evaluateInDomainExperimentsCluster(new File(inDir), new File(outDir));

        //        evaluateRepeatedInDomainCluster(new File("/mnt/hpc-project/output-cv"), null);

//        evaluateRepeatedResultsCluster(new File("/mnt/hpc-project/output-cd"),
//                new File("/tmp/outcd"), true, false, true);
//        evaluateRepeatedResultsCluster(new File("/mnt/hpc-project/output-cv"),
//                new File("/tmp/outcv"), false, false, false);

        evaluateRepeatedResultsCluster(new File("/mnt/apu/argumentation-claims/output-lodo"),
                new File("/tmp/outlodo"), false, false, false);
    }

}
