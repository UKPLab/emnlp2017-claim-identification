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

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Creates id 2 outcome report
 */
public class LiblinearOutcomeIdReport
    extends ReportBase
    implements Constants
{

    // constant dummy value for setting as threshold which is an expected field in the evaluation
    // module but is not needed/provided by liblinear
    private static final String THRESHOLD_CONSTANT = "-1";
    
    private static int numKeys;

    @Override
    public void execute()
        throws Exception
    {
        boolean isRegression = getDiscriminators()
                .get(LiblinearTestTask.class.getName() + "|" + Constants.DIM_LEARNING_MODE)
                .equals(Constants.LM_REGRESSION);
        
        Map<Integer, String> id2label = getId2LabelMapping(isRegression);
        String header = buildHeader(id2label, isRegression);

        List<String> predictions = readPredictions();
        Map<String, String> index2instanceIdMap = getIndex2InstanceIdMap();

        Properties prop = new SortedKeyProperties();
        int lineCounter = 0;
        
        double[][] cm = new double[numKeys][numKeys];
        
        for (String line : predictions) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] split = line.split(LiblinearTestTask.SEPARATOR_CHAR);
            int pred = Integer.valueOf(split[0]);
            int gold = Integer.valueOf(split[1]);
            cm[gold][pred]++;
            
            String key = index2instanceIdMap.get(lineCounter+"");
            prop.setProperty(key, pred + LiblinearTestTask.SEPARATOR_CHAR + gold
                    + LiblinearTestTask.SEPARATOR_CHAR + THRESHOLD_CONSTANT);
            lineCounter++;
        }

        
        writeConfusionMatrix(cm, id2label, getContext());
        File targetFile = getId2OutcomeFileLocation();
        FileWriterWithEncoding fw = new FileWriterWithEncoding(targetFile, "utf-8");
        prop.store(fw, header);
        fw.close();

    }

    private Map<String, String> getIndex2InstanceIdMap() throws IOException
    {
        File f = new File(getContext().getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY), LiblinearDataWriter.INDEX2INSTANCEID);
        
        Map<String,String> m = new HashMap<>();
        
        for(String l : FileUtils.readLines(f, "utf-8")){
            if(l.startsWith("#")){
                continue;
            }
            if(l.trim().isEmpty()){
                continue;
            }
            String[] split = l.split("\t");
            m.put(split[0], split[1]);
        }
        return m;
    }

    private File getId2OutcomeFileLocation()
    {
        File evaluationFolder = getContext().getStorageLocation("", AccessMode.READWRITE);
        return new File(evaluationFolder, ID_OUTCOME_KEY);
    }

    private List<String> readPredictions()
        throws IOException
    {
        File predFolder = getContext().getStorageLocation("", AccessMode.READWRITE);
        String predFileName = LiblinearAdapter.getInstance().getFrameworkFilename(
                AdapterNameEntries.predictionsFile);
        return FileUtils.readLines(new File(predFolder, predFileName));
    }

    private String buildHeader(Map<Integer, String> id2label, boolean isRegression)
        throws UnsupportedEncodingException
    {
        StringBuilder header = new StringBuilder();
        header.append("ID=PREDICTION;GOLDSTANDARD;THRESHOLD" + "\n" + "labels" + " ");
        numKeys = id2label.keySet().size();

        if(isRegression){
            return header.toString();
        }
        
        int numKeys = id2label.keySet().size();
        List<Integer> keys = new ArrayList<Integer>(id2label.keySet());
        for (int i = 0; i < numKeys; i++) {
            Integer key = keys.get(i);
            header.append(key + "=" + URLEncoder.encode(id2label.get(key), "UTF-8"));
            if (i + 1 < numKeys) {
                header.append(" ");
            }
        }
        return header.toString();
    }

    private Map<Integer, String> getId2LabelMapping(boolean isRegression)
        throws Exception
    {
        if(isRegression){
            return new HashMap<>();
        }
        
        File mappingFolder = getContext().getStorageLocation("",
                StorageService.AccessMode.READONLY);
        String fileName = LiblinearAdapter.getOutcomeMappingFilename();
        File file = new File(mappingFolder, fileName);
        Map<Integer, String> map = new HashMap<Integer, String>();

        List<String> lines = FileUtils.readLines(file);
        for (String line : lines) {
            String[] split = line.split("\t");
            map.put(Integer.valueOf(split[1]), split[0]);
        }

        return map;
    }
    
    // quick&dirty!!
    private static void writeConfusionMatrix(double[][] cm, Map<Integer, String> id2label, TaskContext context) throws IOException{
        // build cm string
        StringBuilder sb = new StringBuilder();
        sb.append("\"ID\",\"");
        for(int key : id2label.keySet()){
        	sb.append(id2label.get(key) + "(pred.)\",");
        }
        String head = sb.toString().substring(0, sb.length()-1);
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < cm.length; i++) {
        	rows.append("\"" + id2label.get(i)+"(act.)\",");
        	for(int j = 0; j < cm[0].length; j++){
        		if(j!=cm[0].length-1){
        		rows.append("\"" + cm[i][j]+"\",");
        		}
        		else{
        			rows.append("\"" + cm[i][j]+"\"");
        		}
        	}
        	rows.append("\n");
		}
        
        String cmS = head + "\n" + rows;
        File evaluationFolder = context.getStorageLocation("", AccessMode.READWRITE);
        FileUtils.write(new File(evaluationFolder, Constants.CONFUSIONMATRIX_KEY), cmS); 
    }
}

/**
 * Provides sorted keys for properties file to support human readability i.e. sustain items in th
 * sequence they were classified as additional debug support
 */
@SuppressWarnings("serial")
class SortedKeyProperties
    extends Properties
{

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Enumeration keys()
    {

        final Object[] keys = super.keySet().toArray();
        Arrays.sort(keys);

        return new Enumeration()
        {
            int idx;

            @Override
            public boolean hasMoreElements()
            {
                return idx < keys.length;
            }

            @Override
            public Object nextElement()
            {
                return keys[idx++];
            }
        };
    }
}