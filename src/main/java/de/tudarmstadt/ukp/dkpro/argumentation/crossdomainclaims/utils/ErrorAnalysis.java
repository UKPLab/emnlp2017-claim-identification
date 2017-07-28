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

import org.apache.commons.collections.SortedBidiMap;
import org.apache.commons.collections.bidimap.DualTreeBidiMap;
import org.apache.commons.io.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Ivan Habernal
 */
public class ErrorAnalysis
{

    static SortedMap<String, Integer> loadId2Claim(InputStream stream, boolean extractGoldLabels)
            throws IOException
    {
        SortedMap<String, Integer> result = new TreeMap<>();
        for (String line : IOUtils.readLines(stream)) {
            if (!line.startsWith("#")) {
                String[] split = line.split(";");

                String id = split[0];

                int claim = Integer.valueOf(split[1]);

                if (extractGoldLabels) {
                    claim = Integer.valueOf(split[2]);
                }

                result.put(id, claim);
            }
        }

        return result;
    }

    public static void main(String[] args)
            throws Exception
    {
        // mapping from instances (TC) to documents (sentences)
        SortedBidiMap index2InstanceMap = new DualTreeBidiMap();

        ClassLoader classLoader = ErrorAnalysis.class.getClassLoader();

        for (String line : IOUtils.readLines(
                classLoader.getResourceAsStream("error-analysis/index2InstanceId.txt"))) {
            if (!line.startsWith("#")) {
                String[] split = line.split("\t");
                index2InstanceMap.put(split[0], split[1]);
            }
        }

        //        System.out.println(index2InstanceMap);

        SortedMap<String, String> id2Sentence = new TreeMap<>();

        for (String line : IOUtils
                .readLines(classLoader.getResourceAsStream("error-analysis/id2context.txt"))) {
            String id = line.split("\t")[0];
            String sentence = line.split("\\[\\[")[1].split("\\]\\]")[0];

            //            System.out.println(sentence);
            String tcId = index2InstanceMap.getKey(id).toString();
            //            System.out.println(tcId);
            id2Sentence.put(tcId, sentence);
        }

        System.out.println(id2Sentence);

        SortedMap<String, Integer> ovMt = loadId2Claim(
                classLoader.getResourceAsStream("error-analysis/OV-MT-id2outcome.txt"), false);
        SortedMap<String, Integer> peMt = loadId2Claim(
                classLoader.getResourceAsStream("error-analysis/PE-MT-id2outcome.txt"), false);
        SortedMap<String, Integer> gold = loadId2Claim(
                classLoader.getResourceAsStream("error-analysis/OV-MT-id2outcome.txt"), true);

        PrintWriter pw = new PrintWriter(new FileWriter("/tmp/error-analysis.tsv"));
        pw.println("Sentence\tOV-MT\tPE-MT\tGold");

        for (String id : gold.keySet()) {
            pw.println(id2Sentence.get(id) + "\t" + ovMt.get(id) + "\t" + peMt.get(id) + "\t" + gold
                    .get(id));
        }
        IOUtils.closeQuietly(pw);
    }
}
