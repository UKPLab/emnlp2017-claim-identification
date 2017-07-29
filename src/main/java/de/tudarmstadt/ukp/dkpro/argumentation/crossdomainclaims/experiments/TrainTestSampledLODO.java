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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.utils.ExperimentUtils;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.discourse.PDTBDiscourseFeatures;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.embeddings.EmbeddingFeatures;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.sentiment.StanfordSentimentUFE;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.structural.FirstComponentInParagraph;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.structural.LastComponentInParagraph;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.structural.NumTokensComponent;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.*;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.reader.ClaimSentenceReader;
import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.reader.RandomBalancedCorpusSampler;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramUFE;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class TrainTestSampledLODO implements Constants {
	
	// 0 = full path to parent train data dir
	// 1 = full path to parent test data dir
	// 2 = name of test dataset (one of: Habernal2015, OrBiran2011-lj, OrBiran2011-wd, Peldszus2015en, Reed2008, Stab201X)
	// 3 = random seed training data
	// 4 = full path to result folder (= DKPRO_HOME)
	// 5 = feature type (lexical, syntax, structure, embeddings, dictionary, discourse, sentiment) - combine with "-", e.g. lexical-syntax
	// 6 = language code train data
	// 7 = language code test data
    public static void main(String[] args) throws Exception {

	    System.setProperty("org.apache.uima.logger.class",
			    "org.apache.uima.util.impl.Log4jLogger_impl");

	    System.setProperty("DKPRO_HOME", args[4]);
		long startTime = System.currentTimeMillis();
    	
		List<String> features = new ArrayList<String>(Arrays.asList(args[5].split("-")));
        ParameterSpace pSpace = getParameterSpace(features, args[0], args[1], args[2], args[6], args[7], args[3]);
        TrainTestSampledLODO experiment = new TrainTestSampledLODO();
        
        experiment.run(pSpace);
        ExperimentUtils.evaluate(args[4], 2);
        
        long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total time taken: " + TimeUnit.MILLISECONDS.toHours(totalTime) + " hours " + TimeUnit.MILLISECONDS.toMinutes(totalTime) + " minutes");
    }
	
	
	@SuppressWarnings("unchecked")
	public static ParameterSpace getParameterSpace(List<String> featureType, String corpusPathTrainBaseDir, String corpusPathTestBaseDir, String nameTestDataset, 
			String languageCodeTrain, String languageCodeTest, String randomSeedTrain) throws IOException {
        
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		// TRAIN
        dimReaders.put(DIM_READER_TRAIN, RandomBalancedCorpusSampler.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(
        		RandomBalancedCorpusSampler.PARAM_SOURCE_LOCATION, corpusPathTrainBaseDir,        		
        		RandomBalancedCorpusSampler.PARAM_RANDOM_SEED, Integer.parseInt(randomSeedTrain),
        		RandomBalancedCorpusSampler.PARAM_LANGUAGE, languageCodeTrain,
        		RandomBalancedCorpusSampler.PARAM_PATTERNS, new String[] {
        			RandomBalancedCorpusSampler.INCLUDE_PREFIX + "**/*.xmi",
        			RandomBalancedCorpusSampler.EXCLUDE_PREFIX + nameTestDataset + "/*.xmi"
        		}));
        // TEST
        dimReaders.put(DIM_READER_TEST, ClaimSentenceReader.class);
        dimReaders.put(DIM_READER_TEST_PARAMS, Arrays.asList(
    		ClaimSentenceReader.PARAM_SOURCE_LOCATION, corpusPathTestBaseDir + "/" + nameTestDataset,
    		ClaimSentenceReader.PARAM_LANGUAGE, languageCodeTest,
    		ClaimSentenceReader.PARAM_PATTERNS, Arrays.asList(ClaimSentenceReader.INCLUDE_PREFIX + "*.xmi")));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS, Arrays.asList(new Object[] {
            		 LuceneNGramUFE.PARAM_NGRAM_USE_TOP_K, 4000,
                     LuceneNGramUFE.PARAM_NGRAM_MIN_N, 1,
                     LuceneNGramUFE.PARAM_NGRAM_MAX_N, 1,
                     LuceneNGramUFE.PARAM_NGRAM_LOWER_CASE, true,
                     
                     LucenePOSNGramUFE.PARAM_POS_NGRAM_USE_TOP_K, 2000,
                     LucenePOSNGramUFE.PARAM_POS_NGRAM_MIN_N, 2,
                     LucenePOSNGramUFE.PARAM_POS_NGRAM_MAX_N, 4,
                     
                     DependencyTriples.PARAM_NUMBER_OF_DEPENDENCY_TRIPLES, 4000,
                     
                     ProductionRules.PARAM_THRESHOLD, 5,
                     ProductionRules.PARAM_NUMBER_OF_PRODRULES, 4000,
            }));
                
		List<String> discFeatures = new ArrayList<String>();

		if (featureType.contains("lexical")) {
			System.out.println("using lexical features");
			discFeatures.add(LuceneNGramUFE.class.getName());
		}

		if (featureType.contains("syntax")) {
			System.out.println("using syntax features");
			discFeatures.add(LucenePOSNGramUFE.class.getName());
			discFeatures.add(PosDistribution.class.getName());			
			discFeatures.add(ProductionRules.class.getName());
		}

		if (featureType.contains("structure")) {
			System.out.println("using structure features");
			discFeatures.add(FirstComponentInParagraph.class.getName());
			discFeatures.add(LastComponentInParagraph.class.getName());
			discFeatures.add(NumTokensComponent.class.getName());
			discFeatures.add(PunctuationSequenceUFE.class.getName());
		}

		if (featureType.contains("embeddings")) {
			System.out.println("using embeddings features");
			discFeatures.add(EmbeddingFeatures.class.getName());
		}

		if (featureType.contains("discourse")) {
			System.out.println("using discourse features");
			discFeatures.add(PDTBDiscourseFeatures.class.getName());
		}

		if (featureType.contains("sentiment")) {
			System.out.println("using sentiment features");
			discFeatures.add(StanfordSentimentUFE.class.getName());
		}

		if(discFeatures.size()==0) { // take all
			System.out.println("using all features");

			// lexical
			discFeatures.add(LuceneNGramUFE.class.getName());
			// syntax
			discFeatures.add(LucenePOSNGramUFE.class.getName());
			discFeatures.add(PosDistribution.class.getName());
			discFeatures.add(ProductionRules.class.getName());
			// structure
			discFeatures.add(PunctuationSequenceUFE.class.getName());
			discFeatures.add(FirstComponentInParagraph.class.getName());
			discFeatures.add(LastComponentInParagraph.class.getName());
			discFeatures.add(NumTokensComponent.class.getName());
			// embeddings
			discFeatures.add(EmbeddingFeatures.class.getName());
			// discourse
			discFeatures.add(PDTBDiscourseFeatures.class.getName());
			// sentiment
			discFeatures.add(StanfordSentimentUFE.class.getName());
		}
                
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
            Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), 
            Dimension.create(DIM_FEATURE_MODE, FM_UNIT), 
            dimPipelineParameters, 
            Dimension.create(DIM_FEATURE_SET, discFeatures)        
        );
        return pSpace;
	}
	
	
	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
        return createEngineDescription();
    }
	
	
	protected void run(ParameterSpace pSpace) throws Exception {
		ExperimentTrainTest batch = new ExperimentTrainTest("ClaimDetectionCrossDomain", LiblinearAdapter.class, getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }
}
