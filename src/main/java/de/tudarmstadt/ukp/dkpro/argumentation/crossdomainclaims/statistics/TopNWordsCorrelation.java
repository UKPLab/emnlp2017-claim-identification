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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.statistics;

import com.google.common.collect.TreeBasedTable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class TopNWordsCorrelation
{
    /**
     * Computes Spearman correlation by comparing order of two corpora vocabularies
     *
     * @param goldCorpus  gold corpus
     * @param otherCorpus other corpus
     * @param topN        how many entries from the gold corpus should be taken
     * @throws IOException I/O exception
     */
    public static double spearmanCorrelation(File goldCorpus, File otherCorpus,
            int topN)
            throws IOException
    {
        LinkedHashMap<String, Integer> gold = loadCorpusToRankedVocabulary(
                new FileInputStream(goldCorpus));
        LinkedHashMap<String, Integer> other = loadCorpusToRankedVocabulary(
                new FileInputStream(otherCorpus));

        double[][] matrix = new double[topN][];

        if (gold.size() < topN) {
            //            throw new IllegalArgumentException(
            System.err.println(
                    "topN (" + topN + ") cannot be greater than vocabulary size (" + gold.size()
                            + ")");
            return Double.NaN;
        }

        Iterator<Map.Entry<String, Integer>> iterator = gold.entrySet().iterator();
        int counter = 0;
        while (counter < topN) {
            Map.Entry<String, Integer> next = iterator.next();
            String goldWord = next.getKey();
            Integer goldValue = next.getValue();

            // look-up position in other corpus
            Integer otherValue = other.get(goldWord);
            if (otherValue == null) {
                //                System.err.println("Word " + goldWord + " not found in the other corpus");
                otherValue = Integer.MAX_VALUE;
            }

            matrix[counter] = new double[2];
            matrix[counter][0] = goldValue;
            matrix[counter][1] = otherValue;

            counter++;
        }

        RealMatrix realMatrix = new Array2DRowRealMatrix(matrix);

        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation(realMatrix);
        double pValue = spearmansCorrelation.getRankCorrelation().getCorrelationPValues()
                .getEntry(0, 1);
        double correlation = spearmansCorrelation.getRankCorrelation().getCorrelationMatrix()
                .getEntry(0, 1);

        //        System.out.println("Gold: " + goldCorpus.getName());
        //        System.out.println("Other: " + otherCorpus.getName());
        //        System.out.printf(Locale.ENGLISH, "Top N:\n%d\nCorrelation\n%.3f\np-value\n%.3f\n", topN,
        //                correlation, pValue);

        return correlation;
    }

    public static LinkedHashMap<String, Integer> loadCorpusToRankedVocabulary(InputStream corpus)
            throws IOException
    {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();

        LineIterator lineIterator = IOUtils.lineIterator(corpus, "utf-8");
        int counter = 0;
        while (lineIterator.hasNext()) {
            String line = lineIterator.next();

            String word = line.split("\\s+")[0];

            result.put(word, counter);
            counter++;
        }

        return result;
    }

    public static double atanh(double x)
    {
        return Math.log((1 + x) / (1 - x)) / 2;

    }

    public static double[] spearmanConfidenceInterval(double rho, int num)
    {
        double stderr = 1.0 / Math.sqrt(num - 3);
        double delta = 1.96 * stderr;
        double lower = Math.tanh(atanh(rho) - delta);
        double upper = Math.tanh(atanh(rho) + delta);

        return new double[] { lower, upper };
    }

    public static String nameToShortcut(String fileName)
    {
        Map<String, String> map = new HashMap<>();
        map.put("Peldszus2015en", "MT");
        map.put("Reed2008", "VG");
        map.put("Stab201X", "PE");
        map.put("OrBiran2011-wd", "WTP");
        map.put("OrBiran2011-lj", "OC");
        map.put("Habernal2015", "WD");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (fileName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        throw new IllegalStateException();
    }

    public static void main(String[] args)
            throws IOException
    {
        File inputDir = new File(
                "/home/user-ukp/data2/ACL2016-jointPub-data/exported-plain-text-lemmas");

        File[] files = inputDir.listFiles();
        if (files == null) {
            throw new IOException("No files found in " + inputDir);
        }

        Map<String, Double> jointCorrelations = new TreeMap<>();

        TreeBasedTable<String, String, Double> table = TreeBasedTable.create();

        for (File f1 : files) {
            for (File f2 : files) {
                if (f1 != f2) {
                    Map<Integer, Double> correlations = new TreeMap<>();
                    for (Integer size : Arrays
                            .asList(500)) {
                        double rho = spearmanCorrelation(f1, f2, size);

                        correlations.put(size, rho);
                        System.out
                                .println(rho + ", " + Arrays
                                        .toString(spearmanConfidenceInterval(rho, size)));

                        System.out.println("Orig: " + f1 + ", target: " + f2);
                        System.out.println(correlations);

                        // convert names to shortcuts
                        String n1 = nameToShortcut(f1.getName());
                        String n2 = nameToShortcut(f2.getName());

                        String jointName = n1 + "\t" + n2 + "\t" + size;

                        jointCorrelations.put(jointName, rho);

                        table.put(n1, n2, rho);
                    }
                }
            }
        }

        for (Map.Entry<String, Double> entry : jointCorrelations.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }

        System.out.println(table);

        System.out.println("\\begin{table}\n"
                + "\\begin{small}\n"
                + "\\begin{tabular}{l|llllll}");
        System.out.println(" & " + StringUtils.join(table.columnKeySet(), " & ") + " \\\\ \\hline");
        for (String rowKey : table.rowKeySet()) {
            System.out.print(rowKey);
            for (String columnKey : table.columnKeySet()) {
                Double value = table.get(rowKey, columnKey);
                String s = value == null ? "--" : String.format(Locale.ENGLISH, "%.2f", value);
                System.out.print(" & " + s);
            }
            System.out.println(" \\\\");
        }
        System.out.println("\\end{tabular}\n"
                + "\\end{small}\n"
                + "\\caption{\\label{tab:correlations} XXX}\n"
                + "\\end{table}");
    }

}
