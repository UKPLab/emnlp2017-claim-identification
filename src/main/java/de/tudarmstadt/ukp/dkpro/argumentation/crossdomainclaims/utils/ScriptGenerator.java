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

import edu.stanford.nlp.util.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class ScriptGenerator
{

    private static final SortedSet<String> FEATURES = new TreeSet<>();

    private static final String MAIN_INPUT_DIR = "/work/projects/Project00138";

    private static final String MAIN_OUTPUT_DIR = "/work/scratch/ih68sexe";

    static {
        //        we decided to go without dictionary and sentiment - these were pretty bad though
        //        SortedSet<String> featureTypes = new TreeSet<>(
        //                Arrays.asList("lexical", "syntax", "structure", "embeddings", "dictionary",
        //                        "discourse", "sentiment"));
        SortedSet<String> featureTypes = new TreeSet<>(
                Arrays.asList("lexical", "syntax", "structure", "embeddings", "discourse"));
        for (Set<String> set : Sets.powerSet(featureTypes)) {
            // we accept only one type or all minus one
            if ((set.size() == (featureTypes.size() - 1) || set.size() == 1)) {
                String featureSet = StringUtils.join(set, "-");
                FEATURES.add(featureSet);
            }

            // also generate "all features"
            if (set.size() == featureTypes.size()) {
                String featureSet = StringUtils.join(set, "-");
                FEATURES.add(featureSet);
            }
        }
    }

    public static void generateInDomain(String[] args)
            throws IOException
    {
        File outputDir = new File(args[0]);

        String dataDirBalanced = MAIN_INPUT_DIR + "/corpora";
        String jarFile = MAIN_INPUT_DIR
                + "/arg-cross/de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims-0.0.1-SNAPSHOT-standalone.jar";

        String mainOutputDir = MAIN_OUTPUT_DIR + "/output-cv/";

        Map<String, String> dataAndFolds = new HashMap<>();
        dataAndFolds.put("Habernal2015", "folds-WD.tsv"); // WD
        dataAndFolds.put("Peldszus2015en", "folds-MT.tsv"); // MT
        dataAndFolds.put("OrBiran2011-lj", "folds-OC.tsv"); // OC
        dataAndFolds.put("OrBiran2011-wd", "folds-WTP.tsv"); // WTP
        dataAndFolds.put("Reed2008", "folds-VG.tsv"); // VG
        dataAndFolds.put("Stab201X", "folds-PE.tsv"); // PE

        for (String featureSet : FEATURES) {
            for (Map.Entry<String, String> entry : dataAndFolds.entrySet()) {
                String data = entry.getKey();
                for (int randomSeed = 1; randomSeed <= 20; randomSeed++) {
                    String randomSeedString = String.format("%02d", randomSeed);

                    String jobName = data + "_" + featureSet + "_" + randomSeedString;

                    String header = "#!/bin/bash\n"
                            + "#SBATCH -J " + jobName + "\n"
                            + "#SBATCH --mail-user=<habernal@ukp.informatik.tu-darmstadt.de>\n"
                            //                        + "#SBATCH --mail-type=ALL\n"  (not a good idea :)
                            + "#SBATCH --mail-type=FAIL\n"
                            + "#SBATCH -e " + mainOutputDir + jobName + ".err.%j\n"
                            + "#SBATCH -o " + mainOutputDir + jobName + ".out.%j\n"
                            + "#SBATCH -n 1      # Number of cores\n"
                            + "#SBATCH --mem-per-cpu=32768  # 32GB Main memory in MByte per task\n"
                            + "#SBATCH -t 6:00:00     # 6 hours\n"
                            + "\n"
                            + "# -------------------------------\n"
                            + "module load java\n"
                            + "cd " + MAIN_INPUT_DIR + "\n";


                    /*
                        /path/to/dataset
                        random seed (int)
                        /path/to/DKPRO_HOME
                        features (as before)
                        language (as before)
                     */

                    String currentOutputDir =
                            mainOutputDir + data + "_" + featureSet + "_" + randomSeedString;
                    StringBuilder command = new StringBuilder(String
                            .format(Locale.ENGLISH,
                                    "java -Djava.io.tmpdir=/work/scratch/ih68sexe/tmp -cp  %s "
                                            + "de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.InDomainSampledCVLibSvm "
                                            + "%s %d %s %s EN",
                                    jarFile,
                                    dataDirBalanced + "/" + data,
                                    randomSeed,
                                    currentOutputDir,
                                    featureSet
                            ));

                    // now delete the dkpro-lab directory
                    String removeCommand =
                            "rm -rf " + currentOutputDir + "/de.tudarmstadt.ukp.dkpro.lab/";

                    command.append("\n").append(removeCommand).append("\n");

                    // generate output files
                    File outputFile = new File(outputDir,
                            data + "_" + featureSet + "_" + randomSeedString + ".sh");
                    FileUtils.writeStringToFile(outputFile, header + command.toString());
                }
            }
        }
    }

    public static void generateCrossDomain(String[] args)
            throws IOException
    {
        File outputDir = new File(args[0]);

        String dataDirOrig = MAIN_INPUT_DIR + "/corpora";
        String dataDirBalanced = MAIN_INPUT_DIR + "/corpora-balanced";
        String jarFile = MAIN_INPUT_DIR
                + "/arg-cross/de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims-0.0.1-SNAPSHOT-standalone.jar";

        String mainOutputDir = MAIN_OUTPUT_DIR + "/output-cd/";

        Set<String> dataAndFolds = new TreeSet<>();
        dataAndFolds.add("Habernal2015");
        dataAndFolds.add("Peldszus2015en");
        dataAndFolds.add("OrBiran2011-lj");
        dataAndFolds.add("OrBiran2011-wd");
        dataAndFolds.add("Reed2008");
        dataAndFolds.add("Stab201X");

        // list of [train, test]
        List<List<String>> trainTestTuples = new ArrayList<>();
        for (String d1 : dataAndFolds) {
            for (String d2 : dataAndFolds) {
                if (!d1.equals(d2)) {
                    trainTestTuples.add(Arrays.asList(d1, d2));
                }
            }
        }

        for (String featureSet : FEATURES) {
            for (List<String> entry : trainTestTuples) {
                // we'll repeat random training data sub-sampling 20 times, iterate over seed
                for (int randomSeed = 1; randomSeed <= 20; randomSeed++) {
                    String randomSeedString = String.format("%02d", randomSeed);

                    String training = entry.get(0);
                    String test = entry.get(1);

                    String data = training + "_" + test;

                    String jobName = data + "_" + featureSet + "_" + randomSeedString;

                    String header = "#!/bin/bash\n"
                            + "#SBATCH -J " + jobName + "\n"
                            + "#SBATCH --mail-user=<habernal@ukp.informatik.tu-darmstadt.de>\n"
                            //                        + "#SBATCH --mail-type=ALL\n"  (not a good idea :)
                            + "#SBATCH --mail-type=FAIL\n"
                            + "#SBATCH -e " + mainOutputDir + jobName + ".err.%j\n"
                            + "#SBATCH -o " + mainOutputDir + jobName + ".out.%j\n"
                            + "#SBATCH -n 1      # Number of cores\n"
                            + "#SBATCH --mem-per-cpu=32768  # 32GB Main memory in MByte per task\n"
                            + "#SBATCH -t 4:00:00     # Hours, minutes and seconds, or '#SBATCH -t 10' - only minutes\n"
                            + "\n"
                            + "# -------------------------------\n"
                            + "module load java\n"
                            + "cd " + MAIN_INPUT_DIR + "\n";

                    //                StringBuilder command = new StringBuilder();

                    String currentOutputDir =
                            mainOutputDir + data + "_" + featureSet + "_" + randomSeedString;
                    String mainClass = "de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.TrainTestSampled";
                    StringBuilder command = new StringBuilder(String
                            .format(Locale.ENGLISH,
                                    "java -Djava.io.tmpdir=/work/scratch/ih68sexe/tmp -cp %s %s %s %s %d %s %s EN EN",
                                    jarFile,
                                    mainClass,
                                    dataDirBalanced + "/" + training,
                                    dataDirOrig + "/" + test,
                                    randomSeed,
                                    currentOutputDir,
                                    featureSet
                            ));
                    command.append("\n# clean the cache\n");
                    command.append(String.format(Locale.ENGLISH,
                            "rm -rf %s/de.tudarmstadt.ukp.dkpro.lab\n",
                            currentOutputDir
                    ));

                    // generate output files

                    File outputFile = new File(outputDir, jobName + ".sh");
                    FileUtils.writeStringToFile(outputFile, header + command);
                }
            }
        }
    }

    public static void generateLODO(String[] args)
            throws IOException
    {
        File outputDir = new File(args[0]);

        // String dataDirSentences = MAIN_INPUT_DIR
        // must be on scratch due to slow access on the /work/project folder!!!

//        String dataDirSentences = "/work/scratch/ih68sexe"
//                + "/corpora-last-experiment/data-full-preprocessing-fixed-paragraphs-sentences";

        // APU
        String dataDirSentences = "/home/habernal/argumentation-claims"
                + "/corpora-last-experiment/data-full-preprocessing-fixed-paragraphs-sentences/";

        // must be on scratch due to slow access on the /work/project folder!!!
//        String dataDirOrig = "/work/scratch/ih68sexe" +
//                "/corpora-last-experiment/full-preprocessing-fixed-paragraphs";

        // APU
        String dataDirOrig = "/home/habernal/argumentation-claims" +
                "/corpora-last-experiment/full-preprocessing-fixed-paragraphs/";

        String mainInputDir = "/home/habernal/argumentation-claims";

        String jarFile = mainInputDir
                + "/arg-cross/de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims-0.0.1-SNAPSHOT-standalone.jar";

        String mainOutputDir = "/home/habernal/argumentation-claims/output-lodo/";

        Set<String> allDataSets = new TreeSet<>();
        allDataSets.add("Habernal2015");
        allDataSets.add("Peldszus2015en");
        allDataSets.add("OrBiran2011-lj");
        allDataSets.add("OrBiran2011-wd");
        allDataSets.add("Reed2008");
        allDataSets.add("Stab201X");

//        for (String featureSet : FEATURES) {
//        all features only
        String featureSet = "discourse-embeddings-lexical-syntax-structure";
            for (String dataSet : allDataSets) {
                // we'll repeat random training data sub-sampling 20 times, iterate over seed
                for (int randomSeed = 1; randomSeed <= 20; randomSeed++) {
                    String randomSeedString = String.format("%02d", randomSeed);

                    String jobName = dataSet + "_" + featureSet + "_" + randomSeedString;

                    String header = "#!/bin/bash\n"
//                            + "#SBATCH -J " + jobName + "\n"
//                            + "#SBATCH --mail-user=<habernal@ukp.informatik.tu-darmstadt.de>\n"
//                            + "#SBATCH --mail-type=FAIL\n"
//                            + "#SBATCH -e " + mainOutputDir + jobName + ".err.%j\n"
//                            + "#SBATCH -o " + mainOutputDir + jobName + ".out.%j\n"
//                            + "#SBATCH -n 1      # Number of cores\n"
//                            + "#SBATCH --mem-per-cpu=24000  # 24GB Main memory in MByte per task required for this setup\n"
//                            + "#SBATCH -t 4:00:00     # Hours, minutes and seconds\n"
//                            + "\n"
//                            + "# -------------------------------\n"
//                            + "module load java\n"
                            + "cd " + mainInputDir + "\n";

                    //                StringBuilder command = new StringBuilder();

                    String currentOutputDir =
                            mainOutputDir + dataSet + "_" + featureSet + "_" + randomSeedString;
                    String mainClass = "de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.TrainTestSampledLODO";
                    StringBuilder command = new StringBuilder(String
                            .format(Locale.ENGLISH,
                                    "java -cp %s %s %s %s %s %d %s %s EN EN",
                                    jarFile,
                                    mainClass,
                                    dataDirSentences,
                                    dataDirOrig,
                                    dataSet,
                                    randomSeed,
                                    currentOutputDir,
                                    featureSet
                            ));
                    command.append("\n# clean the cache\n");
                    command.append(String.format(Locale.ENGLISH,
                            "rm -rf %s/de.tudarmstadt.ukp.dkpro.lab\n",
                            currentOutputDir
                    ));

                    // generate output files

                    File outputFile = new File(outputDir, jobName + ".sh");
                    FileUtils.writeStringToFile(outputFile, header + command);
                }
            }
    }

    public static void main(String[] args)
            throws IOException
    {
//        generateInDomain(args);
        //        generateCrossDomain(args);
        generateLODO(args);
    }

}
