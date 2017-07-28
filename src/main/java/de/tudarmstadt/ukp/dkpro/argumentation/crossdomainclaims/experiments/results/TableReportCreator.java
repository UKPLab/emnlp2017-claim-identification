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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.results;

import com.github.habernal.confusionmatrix.ConfusionMatrix;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class TableReportCreator
{

    private static final Map<String, String> DOMAIN_MAP = new TreeMap<>();

    static public ConfusionMatrix loadMatrixFromCSVFile(InputStream stream)
            throws IOException
    {
        ConfusionMatrix result = new ConfusionMatrix();

        String content = StringUtils.join(IOUtils.readLines(stream, "utf-8"), "\n");

        String[] h;

        // detect the header
        if (content.contains("Claim (pred.)\",\"None (pred.)")) {
            h = new String[] { "ID", "Claim (pred.)", "None (pred.)" };
        }
        else if (content.contains("\"None (pred.)\",\"Claim (pred.)\"")) {
            h = new String[] { "ID", "None (pred.)", "Claim (pred.)" };
        }
        else {
            throw new IllegalArgumentException(content);
        }

        CSVParser csvParser = CSVFormat.DEFAULT.withHeader(h)
                .withSkipHeaderRecord(true)
                .parse(new StringReader(content));

        for (CSVRecord record : csvParser.getRecords()) {
            for (Map.Entry<String, Integer> header : csvParser.getHeaderMap().entrySet()) {
                String goldLabel = record.get(0).split(" ")[0];
                String predictedLabel = header.getKey().split(" ")[0];

                String countsString = record.get(header.getValue());

                try {
                    int count = (int) ((double) Double.valueOf(countsString));
                    result.increaseValue(goldLabel, predictedLabel, count);
                }
                catch (NumberFormatException ex) {
                    // ignore
                }
            }
        }

        return result;
    }

    static void processConsolidatedResultsCD(File mainIdDir)
            throws IOException
    {
        // all results: tables for each "system"
        Map<Category, Map<String, Table<String, String, ConfusionMatrix>>> allTables = new TreeMap<>();

        File[] dirs = mainIdDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
        if (dirs == null) {
            throw new IOException("No sub dirs found");
        }

        for (File experimentDir : dirs) {
            String[] split = experimentDir.getName().split("_");
            //            System.out.println(Arrays.asList(split));

            // there must be three parts
            if (split.length == 3) {
                String sourceDomain = mapDomain(split[0]);
                String targetDomain = mapDomain(split[1]);
                FeatureNameAndCategory featureNameAndCategory = mapSystem(split[2]);

                if (featureNameAndCategory == null) {
                    throw new IllegalStateException(
                            "Cannot find proper category and domain: " + Arrays.toString(split));
                }

                String features = featureNameAndCategory.featureName;
                Category category = featureNameAndCategory.category;

                // load confusion matrix
                File[] csvFiles = experimentDir
                        .listFiles((FileFilter) new SuffixFileFilter(".csv"));
                if (csvFiles == null) {
                    throw new IllegalStateException("No files found in " + experimentDir);
                }

                if (csvFiles.length != 1) {
                    throw new IllegalStateException(
                            "Expected one csv file but found " + csvFiles.length + " in "
                                    + experimentDir);
                }

                FileInputStream stream = new FileInputStream(csvFiles[0]);
                ConfusionMatrix cm = loadMatrixFromCSVFile(stream);
                IOUtils.closeQuietly(stream);

                // add to the result tables
                if (!allTables.containsKey(category)) {
                    allTables.put(category,
                            new TreeMap<String, Table<String, String, ConfusionMatrix>>());
                }

                if (!allTables.get(category).containsKey(features)) {
                    allTables.get(category).put(features,
                            TreeBasedTable.<String, String, ConfusionMatrix>create());
                }

                Table<String, String, ConfusionMatrix> table = allTables.get(category)
                        .get(features);
                // update
                table.put(sourceDomain, targetDomain, cm);
            }
            else {
                System.err.println("Required Cross-Domain folder, got " + experimentDir);
            }
        }

        printTables(allTables);

    }

    static void processConsolidatedResultsCV(File mainIdDir)
            throws IOException
    {
        // all results: tables for each "system"
        // table: row (features), column (domain)
        Map<Category, Table<String, String, ConfusionMatrix>> allTables = new TreeMap<>();

        File[] dirs = mainIdDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
        if (dirs == null) {
            throw new IOException("No sub dirs found");
        }

        for (File experimentDir : dirs) {
            String[] split = experimentDir.getName().split("_");
            //            System.out.println(Arrays.asList(split));

            // there must be three parts
            if (split.length == 2) {
                String domain = mapDomain(split[0]);
                FeatureNameAndCategory featureNameAndCategory = mapSystem(split[1]);
                String features = featureNameAndCategory.featureName;
                Category category = featureNameAndCategory.category;

                // load confusion matrix
                File[] csvFiles = experimentDir.listFiles(new FilenameFilter()
                {
                    @Override public boolean accept(File dir, String name)
                    {
                        return "confusionMatrix.csv".equals(name) || "confMatrix.csv".equals(name)
                                || "confMatrix001.csv".equals(name);
                    }
                });

                if (csvFiles == null) {
                    throw new IllegalStateException("No files found in " + experimentDir);
                }

                if (csvFiles.length != 1) {
                    throw new IllegalStateException(
                            "Expected one csv file but found " + csvFiles.length + " in "
                                    + experimentDir);
                }

                FileInputStream stream = new FileInputStream(csvFiles[0]);
                ConfusionMatrix cm = loadMatrixFromCSVFile(stream);
                IOUtils.closeQuietly(stream);

                // add to the result tables
                if (!allTables.containsKey(category)) {
                    allTables.put(category,
                            TreeBasedTable.<String, String, ConfusionMatrix>create());
                }

                Table<String, String, ConfusionMatrix> table = allTables.get(category);
                // update
                table.put(features, domain, cm);
            }

            else {
                throw new IllegalStateException("Required CV folder, got " + experimentDir);
            }
        }

        printTablesCV(allTables);

    }

    private static void printTablesCV(
            Map<Category, Table<String, String, ConfusionMatrix>> allTables)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);

        // get domains
        SortedSet<String> domains = new TreeSet<>(DOMAIN_MAP.values());

        // full header first
        String header = "\\begin{table*}[ht]\n"
                + "\\begin{small}\n"
                + "\\begin{center}\n"
                + "\\begin{tabular}{p{2cm}|rr|rr|rr|rr|rr|rr|rr}\n"
                + "Domain $\\rightarrow$ & " +
                StringUtils.join(CollectionUtils.collect(domains, new Transformer()
                {
                    @Override public Object transform(Object input)
                    {
                        return "\\multicolumn{2}{c}{\\textbf{" + input + "}}";
                    }
                }), " & ")
                + " & \\multicolumn{2}{c}{\\textbf{Avg}} \\\\ \\hline";

        pw.println(header);

        for (Map.Entry<Category, Table<String, String, ConfusionMatrix>> e : allTables
                .entrySet()) {
            String category = e.getKey().toString();
            Table<String, String, ConfusionMatrix> table = e.getValue();

            // iterate over domains

            List<List<String>> rows = new ArrayList<>();
            // iterate over rows
            for (String rowName : table.rowKeySet()) {
                Map<String, ConfusionMatrix> row = table.row(rowName);

                List<String> rowCells = new ArrayList<>(Collections.singletonList(rowName));

                DescriptiveStatistics rowClaimF1 = new DescriptiveStatistics();
                DescriptiveStatistics rowMacroF1 = new DescriptiveStatistics();

                for (String domain : domains) {
                    ConfusionMatrix cell = row.get(domain);

                    if (cell == null) {
                        throw new IllegalArgumentException(
                                "No results for " + rowName + "/" + domain);
                    }

                    // source and target domain are the same here
                    double macroF = getMacroFMeasureFromMacroPR(cell);
                    double claimF = cell.getFMeasureForLabels().get("Claim");

                    //                    if ("WD".equals(domain) && "Discourse".equals(rowName)) {
                    //                        System.out.println(rowName);
                    //                        System.out.println(cell);
                    //                        System.out.println(macroF);
                    //                        System.out.println(claimF);
                    //                        System.out.println(cell.printNiceResults());
                    //                        System.out.println(cell.printLabelPrecRecFm());
                    //                        System.out.println(cell.getFMeasureForLabels());
                    //                        System.out.println(cell.getPrecisionForLabels());
                    //                        System.out.println(cell.getRecallForLabels());
                    //                    }

                    // update total stats
                    rowClaimF1.addValue(claimF);
                    rowMacroF1.addValue(macroF);

                    rowCells.add(format(macroF));
                    rowCells.add(format(claimF));
                }

                // add row averages
                rowCells.add(format(rowMacroF1.getMean()));
                rowCells.add(format(rowClaimF1.getMean()));

                rows.add(rowCells);
            }

            // print the header for the category
            pw.println("\\multicolumn{15}{c}{\\centering{\\emph{" + category + "}}} \\\\");

            for (List<String> row : rows) {
                // print the collected row
                pw.print(StringUtils.join(row, " & "));
                pw.println(" \\\\");
            }

            pw.println("\\hline");
        }

        pw.println("\\end{tabular}\n"
                + "\\end{center}\n"
                + "\\end{small}\n"
                + "\\caption{Cross-validation experiments. "
                + " For each test dataset (column head) we show two scores: \\emph{Macro $F_1$} score (left-hand column) and $F_1$ score for claims (right-hand column).}\n"
                + "\\label{tab:results-cross-validation}\n"
                + "\\end{table*}");

        pw.close();

        System.out
                .println("\n\n\n% ================== Cross-validation results ===================");

        System.out.println(sw.toString());
    }

    private static void printTables(
            Map<Category, Map<String, Table<String, String, ConfusionMatrix>>> allTables)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);

        // get domains
        SortedSet<String> domains = new TreeSet<>(
                allTables.values().iterator().next().entrySet().iterator().next().getValue()
                        .columnKeySet());

        for (Map.Entry<Category, Map<String, Table<String, String, ConfusionMatrix>>> e : allTables
                .entrySet()) {
            String category = e.getKey().toString();
            Map<String, Table<String, String, ConfusionMatrix>> tables = e.getValue();

            // full header first
            String header = "\\begin{table*}[ht]\n"
                    + "\\begin{small}\n"
                    + "\\begin{center}\n"
                    + "\\begin{tabular}{p{2cm}|rr|rr|rr|rr|rr|rr|rr}\n"
                    + "Train $\\downarrow$ Test $\\rightarrow$ & " +
                    StringUtils.join(CollectionUtils.collect(domains, new Transformer()
                    {
                        @Override public Object transform(Object input)
                        {
                            return "\\multicolumn{2}{c}{\\textbf{" + input + "}}";
                        }
                    }), " & ")
                    + " & \\multicolumn{2}{c}{\\textbf{Avg}} \\\\ \\hline";

            pw.println(header);

            // iterate over methods (features)
            for (Map.Entry<String, Table<String, String, ConfusionMatrix>> entry : tables
                    .entrySet()) {

                DescriptiveStatistics featAvgMacro = new DescriptiveStatistics();
                DescriptiveStatistics featAvgCF1 = new DescriptiveStatistics();

                Table<String, String, ConfusionMatrix> t = entry.getValue();

                List<List<String>> rows = new ArrayList<>();
                // iterate over rows
                for (String rowName : t.rowKeySet()) {
                    Map<String, ConfusionMatrix> row = t.row(rowName);

                    List<String> rowCells = new ArrayList<>(Collections.singletonList(rowName));

                    DescriptiveStatistics rowClaimF1 = new DescriptiveStatistics();
                    DescriptiveStatistics rowMacroF1 = new DescriptiveStatistics();

                    for (String domain : domains) {
                        ConfusionMatrix cell = row.get(domain);

                        // source and target domain are the same here
                        if (cell == null) {
                            rowCells.add("--");
                            rowCells.add("--");
                        }
                        else {
                            double macroF = getMacroFMeasureFromMacroPR(cell);
                            double claimF = cell.getFMeasureForLabels().get("Claim");

                            // update total stats
                            featAvgCF1.addValue(claimF);
                            featAvgMacro.addValue(macroF);
                            rowClaimF1.addValue(claimF);
                            rowMacroF1.addValue(macroF);

                            rowCells.add(format(macroF));
                            rowCells.add(format(claimF));
                        }
                    }

                    // add row averages
                    rowCells.add(format(rowMacroF1.getMean()));
                    rowCells.add(format(rowClaimF1.getMean()));

                    rows.add(rowCells);
                }

                // print the header
                pw.println("\\multicolumn{13}{c|}{\\centering{\\emph{" + entry.getKey() + "}}} & "
                        + format(featAvgMacro.getMean()) + " & " + format(featAvgCF1.getMean())
                        + " \\\\");

                for (List<String> row : rows) {
                    // print the collected row
                    pw.print(StringUtils.join(row, " & "));
                    pw.println(" \\\\");
                }

                pw.println("\\hline");
            }

            pw.println("\\end{tabular}\n"
                    + "\\end{center}\n"
                    + "\\end{small}\n"
                    + "\\caption{Cross-domain experiments, " + category
                    + ". For each test dataset (column head) we show two scores: \\emph{Macro $F_1$} score (left-hand column) and $F_1$ score for claims (right-hand column).}\n"
                    + "\\label{tab:results-cross-domain-" + e.getKey().name() + "}\n"
                    + "\\end{table*}"
                    + "\n\n\n");

        }

        pw.flush();

        System.out.println("% ================== Cross-domain results ===================");

        System.out.println(sw.toString());

    }

    private static String format(double value)
    {
        return String.format(Locale.ENGLISH, "%.1f", value * 100);
    }

    static double getMacroFMeasureFromMacroPR(ConfusionMatrix cm)
    {
        double macroP = getMean(cm.getPrecisionForLabels().values());
        double macroR = getMean(cm.getRecallForLabels().values());

        return (2 * macroP * macroR) / (macroP + macroR);
    }

    static double getMean(Collection<? extends Number> numbers)
    {
        double sum = 0;
        for (Number d : numbers) {
            sum += d.doubleValue();
        }
        return sum / (double) numbers.size();
    }

    enum Category
    {
        NN {
            @Override public String toString()
            {
                return "neural network models";
            }
        },
        ALL {
            @Override public String toString()
            {
                return "feature combination and ablation";
            }
        },
        SINGLE {
            @Override public String toString()
            {
                return "single features";
            }
        },
        BASELINE {
            @Override public String toString()
            {
                return "baselines";
            }
        }

    }

    static class FeatureNameAndCategory
    {
        final String featureName;
        final Category category;

        FeatureNameAndCategory(String featureName, Category category)
        {
            this.featureName = featureName;
            this.category = category;
        }

    }

    private static FeatureNameAndCategory mapSystem(String system)
    {
        Map<String, FeatureNameAndCategory> map = new TreeMap<>();
        map.put("embeddings-syntax-lexical-structure-discourse",
                new FeatureNameAndCategory("All features", Category.ALL));
        map.put("discourse-embeddings-lexical-syntax-structure",
                new FeatureNameAndCategory("All features", Category.ALL));
        map.put("embeddings", new FeatureNameAndCategory("Embedd", Category.SINGLE));
        map.put("lexical", new FeatureNameAndCategory("Lexical", Category.SINGLE));
        map.put("syntax", new FeatureNameAndCategory("Syntax", Category.SINGLE));
        map.put("discourse", new FeatureNameAndCategory("Discourse", Category.SINGLE));
        map.put("structure", new FeatureNameAndCategory("Structure", Category.SINGLE));
        map.put("embeddings-syntax-lexical-discourse",
                new FeatureNameAndCategory("All - struct", Category.ALL));
        map.put("embeddings-syntax-lexical-structure",
                new FeatureNameAndCategory("All - disc", Category.ALL));
        map.put("embeddings-syntax-structure-discourse",
                new FeatureNameAndCategory("All - lex", Category.ALL));
        map.put("syntax-lexical-structure-discourse",
                new FeatureNameAndCategory("All - embedd", Category.ALL));
        map.put("embeddings-lexical-structure-discourse",
                new FeatureNameAndCategory("All - syntax", Category.ALL));
        map.put("allnone", new FeatureNameAndCategory("Majority bsl", Category.BASELINE));
        map.put("rand", new FeatureNameAndCategory("Random bsl", Category.BASELINE));
        map.put("lstm", new FeatureNameAndCategory("LSTM", Category.NN));
        map.put("bilstm", new FeatureNameAndCategory("BiLSTM", Category.NN));
        map.put("cnn-rand", new FeatureNameAndCategory("CNN (rand.)", Category.NN));
        map.put("cnn-w2vec", new FeatureNameAndCategory("CNN (w2v)", Category.NN));

        String s = system.toLowerCase();

        if (!map.containsKey(s)) {
            throw new IllegalArgumentException("Unknown system: " + s);
        }

        return map.get(s);
    }

    private static String mapDomain(String domain)
    {
        DOMAIN_MAP.put("Peldszus2015en", "MT");
        DOMAIN_MAP.put("Reed2008", "VG");
        DOMAIN_MAP.put("Stab201X", "PE");
        DOMAIN_MAP.put("OrBiran2011-wd", "WTP");
        DOMAIN_MAP.put("OrBiran2011-lj", "OC");
        DOMAIN_MAP.put("Habernal2015", "WD");

        if (!DOMAIN_MAP.containsKey(domain)) {
            // some methods use the proper domain names already
            if (DOMAIN_MAP.containsValue(domain)) {
                return domain;
            }

            throw new IllegalArgumentException("Unknown domain " + domain);
        }

        return DOMAIN_MAP.get(domain);
    }

    public static void main(String[] args)
            throws Exception
    {
        processConsolidatedResultsCD(
                new File("/home/user-ukp/data2/ACL2016-jointPub-data/outputs/outcd"));
        processConsolidatedResultsCV(
                new File("/home/user-ukp/data2/ACL2016-jointPub-data/outputs/outcv"));

        processConsolidatedResultsCV(
                new File("/home/user-ukp/data2/ACL2016-jointPub-data/outputs/outlodo"));

    }

}
