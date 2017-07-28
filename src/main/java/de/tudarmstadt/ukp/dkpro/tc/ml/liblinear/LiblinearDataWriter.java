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

import de.bwaldvogel.liblinear.FeatureNode;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Format is outcome TAB index:value TAB index:value TAB ...
 * 
 * Zeros are omitted. Indexes need to be sorted.
 * 
 * For example: 1 1:1 3:1 4:1 6:1 2 2:1 3:1 5:1 7:1 1 3:1 5:1
 */
public class LiblinearDataWriter
    implements DataWriter
{

    static final String INDEX2INSTANCEID = "index2InstanceId.txt";

    @Override
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode)
                throws Exception
    {
        FeatureNodeArrayEncoder encoder = new FeatureNodeArrayEncoder();
        FeatureNode[][] nodes = encoder.featueStore2FeatureNode(featureStore);

        String fileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(outputDirectory, fileName)), "utf-8"));

        Map<String, String> index2instanceId = new HashMap<>();

        for (int i = 0; i < nodes.length; i++) {
            Instance instance = featureStore.getInstance(i);

            recordInstanceId(instance, i, index2instanceId);

            List<String> elements = new ArrayList<String>();
            for (int j = 0; j < nodes[i].length; j++) {
                FeatureNode node = nodes[i][j];
                int index = node.getIndex();
                double value = node.getValue();

                // write sparse values, i.e. skip zero values
                if (Math.abs(value) > 0.00000000001) {
                    elements.add(index + ":" + value);
                }
            }
            bw.append(instance.getOutcome());
            bw.append("\t");
            bw.append(StringUtils.join(elements, "\t"));
            bw.append("\n");
        }
        bw.close();
        
        //write mapping
        writeMapping(outputDirectory, INDEX2INSTANCEID, index2instanceId);
    }

    private void writeMapping(File outputDirectory, String fileName, Map<String, String> index2instanceId) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("#Index\tDkProInstanceId\n");
        for(String k : index2instanceId.keySet()){
            sb.append(k + "\t" + index2instanceId.get(k)+"\n");
        }
        FileUtils.writeStringToFile(new File(outputDirectory, fileName), sb.toString(), "utf-8");
    }

    // build a map between the dkpro instance id and the index in the file
    private void recordInstanceId(Instance instance, int i, Map<String, String> index2instanceId)
    {
        Collection<Feature> features = instance.getFeatures();
        for (Feature f : features) {
            if (!f.getName().equals(Constants.ID_FEATURE_NAME)) {
                continue;
            }
            index2instanceId.put(i + "", f.getValue() + "");
            return;
        }
    }
}