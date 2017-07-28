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

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.utils.ExperimentUtils;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.reader.ClaimSentenceReader;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramUFE;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.WekaClassificationAdapter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.rules.ZeroR;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class CalculateInfoGain implements Constants {
	
	// 0 = full path to data
	// 1 = language code
	// 2 = DKPRO_HOME
    public static void main(String[] args) throws Exception {

	    System.setProperty("org.apache.uima.logger.class",
			    "org.apache.uima.util.impl.Log4jLogger_impl");
	    System.setProperty("DKPRO_HOME", args[2]);
		long startTime = System.currentTimeMillis();
    	
        ParameterSpace pSpace = getParameterSpace(args[0], args[1]);
        CalculateInfoGain experiment = new CalculateInfoGain();
        
        experiment.run(pSpace);
        ExperimentUtils.evaluate(args[2], 2);
        
        long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total time taken: " + TimeUnit.MILLISECONDS.toHours(totalTime) + " hours " + TimeUnit.MILLISECONDS.toMinutes(totalTime) + " minutes");
    }
	
	
	@SuppressWarnings("unchecked")
	public static ParameterSpace getParameterSpace(String corpusPath, String languageCode) throws IOException {
        
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		// TRAIN
        dimReaders.put(DIM_READER_TRAIN, ClaimSentenceReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(
    		ClaimSentenceReader.PARAM_SOURCE_LOCATION, corpusPath,
    		ClaimSentenceReader.PARAM_LANGUAGE, languageCode,
    		ClaimSentenceReader.PARAM_PATTERNS, Arrays.asList(ClaimSentenceReader.INCLUDE_PREFIX + "**/*.xmi")));
        // TEST
        dimReaders.put(DIM_READER_TEST, ClaimSentenceReader.class);
        dimReaders.put(DIM_READER_TEST_PARAMS, Arrays.asList(
    		ClaimSentenceReader.PARAM_SOURCE_LOCATION, "src/test/resources/dummy-data",
    		ClaimSentenceReader.PARAM_LANGUAGE, "EN",
    		ClaimSentenceReader.PARAM_PATTERNS, Arrays.asList(ClaimSentenceReader.INCLUDE_PREFIX + "*.xmi")));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS, Arrays.asList(new Object[] {
            		 LuceneNGramUFE.PARAM_NGRAM_USE_TOP_K, 4000,
                     LuceneNGramUFE.PARAM_NGRAM_MIN_N, 1,
                     LuceneNGramUFE.PARAM_NGRAM_MAX_N, 1,
                     LuceneNGramUFE.PARAM_NGRAM_LOWER_CASE, true
            }));
                
        List<String> discFeatures = new ArrayList<String>();
        // lexical
        discFeatures.add(LuceneNGramUFE.class.getName());
        
        // single-label feature selection
        Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
        dimFeatureSelection.put(DIM_FEATURE_SEARCHER_ARGS,
        		Arrays.asList(Ranker.class.getName(), "-T", "0.0001"));
        dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
        		Arrays.asList(InfoGainAttributeEval.class.getName()));
        dimFeatureSelection.put(DIM_APPLY_FEATURE_SELECTION, true);
        
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(ZeroR.class.getName()));
                
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
            Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), 
            Dimension.create(DIM_FEATURE_MODE, FM_UNIT), 
            Dimension.createBundle("featureSelection", dimFeatureSelection),
            dimPipelineParameters, dimClassificationArgs,
            Dimension.create(DIM_FEATURE_SET, discFeatures)        
        );
        return pSpace;
	}
	
	
	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
        return createEngineDescription();
    }
	
	
	protected void run(ParameterSpace pSpace) throws Exception {
		ExperimentTrainTest batch = new ExperimentTrainTest("ClaimDetectionCrossDomain", WekaClassificationAdapter.class, getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }
}
