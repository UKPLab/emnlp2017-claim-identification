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
package de.tudarmstadt.ukp.dkpro.tc.ml;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.TaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.CustomFoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;
import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ExperimentFolds extends BatchTask {
    
    protected String experimentName;
    protected AnalysisEngineDescription preprocessing;
    protected List<String> operativeViews;
    protected List<Class<? extends Report>> innerReports;
    protected TCMachineLearningAdapter mlAdapter;

    protected ValidityCheckTask checkTask;
    protected PreprocessTask preprocessTask;
    protected MetaInfoTask metaTask;
    protected ExtractFeaturesTask extractFeaturesTrainTask;
    protected ExtractFeaturesTask extractFeaturesTestTask;
    protected TaskBase testTask;
    
    private String foldFile;

    private boolean useDevSet = false;
    
    public ExperimentFolds()
    {/* needed for Groovy */
    }

    /**
     * Preconfigured crossvalidation setup.
     * 
     * @param aExperimentName
     *            name of the experiment
     * @param preprocessing
     *            preprocessing analysis engine aggregate
     * @param aNumFolds
     *            the number of folds for crossvalidation (default 10)
     */
	public ExperimentFolds(String aExperimentName, Class<? extends TCMachineLearningAdapter> mlAdapter, AnalysisEngineDescription preprocessing, String aFoldFile, boolean useDevSet) throws TextClassificationException {
		foldFile = aFoldFile;
		this.useDevSet = useDevSet;
		setExperimentName(aExperimentName);
		setMachineLearningAdapter(mlAdapter);
		setPreprocessing(preprocessing);
		// set name of overall batch task
		setType("Evaluation-" + experimentName);
	}

    /**
     * Initializes the experiment. This is called automatically before execution. It's not done
     * directly in the constructor, because we want to be able to use setters instead of the
     * three-argument constructor.
     * 
     * @throws IllegalStateException
     *             if not all necessary arguments have been set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected void init()
        throws IllegalStateException, InstantiationException, IllegalAccessException,
        ClassNotFoundException
    {

        if (experimentName == null || preprocessing == null) {
            throw new IllegalStateException(
                    "You must set experiment name, datawriter and preprocessing aggregate.");
        }

//        if (numFolds < 2) {
//            throw new IllegalStateException(
//                    "Number of folds is not configured correctly. Number of folds needs to be at " +
//                            "least 2 (but was " + numFolds + ")");
//        }

        // check the validity of the experiment setup first
        checkTask = new ValidityCheckTask();
        checkTask.setMlAdapter(mlAdapter);
        checkTask.setType(checkTask.getType() + "-" + experimentName);

        // preprocessing on the entire data set and only once
        preprocessTask = new PreprocessTask();
        preprocessTask.setPreprocessing(preprocessing);
        preprocessTask.setOperativeViews(operativeViews);
        preprocessTask.setType(preprocessTask.getType() + "-" + experimentName);

        // inner batch task (carried out numFolds times)
        BatchTask crossValidationTask = new BatchTask()
        {
            @Override
            public void execute(TaskContext aContext)
                throws Exception
            {
                File xmiPathRoot = aContext.getStorageLocation(PreprocessTask.OUTPUT_KEY_TRAIN,
                        AccessMode.READONLY);
                Collection<File> files = FileUtils.listFiles(xmiPathRoot, new String[] { "bin" },true);
                
                String[] fileNames = new String[files.size()];
                int i = 0;
                for (File f : files) {
                    // adding file paths, not names
                    fileNames[i] = f.getAbsolutePath();
                    i++;
                }
                Arrays.sort(fileNames);
                
                // don't change any names!!
                FoldDimensionBundle<String> foldDim = getFoldDim(fileNames);
                        
                Dimension<File> filesRootDim = Dimension.create("filesRoot", xmiPathRoot);

                ParameterSpace pSpace = new ParameterSpace(foldDim, filesRootDim);
                setParameterSpace(pSpace);

                super.execute(aContext);
            }
        };

        // ================== SUBTASKS OF THE INNER BATCH TASK =======================

        // collecting meta features only on the training data (numFolds times)
        metaTask = new MetaInfoTask();
        metaTask.setOperativeViews(operativeViews);
        metaTask.setType(metaTask.getType() + "-" + experimentName);

        // extracting features from training data (numFolds times)
        extractFeaturesTrainTask = new ExtractFeaturesTask();
        extractFeaturesTrainTask.setTesting(false);
        extractFeaturesTrainTask.setType(extractFeaturesTrainTask.getType() + "-Train-"
                + experimentName);
        extractFeaturesTrainTask.setMlAdapter(mlAdapter);
        extractFeaturesTrainTask.addImport(metaTask, MetaInfoTask.META_KEY);

        // extracting features from test data (numFolds times)
        extractFeaturesTestTask = new ExtractFeaturesTask();
        extractFeaturesTestTask.setTesting(true);
        extractFeaturesTestTask.setType(extractFeaturesTestTask.getType() + "-Test-"
                + experimentName);
        extractFeaturesTestTask.setMlAdapter(mlAdapter);
        extractFeaturesTestTask.addImport(metaTask, MetaInfoTask.META_KEY);
        extractFeaturesTestTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY);
        
        // classification (numFolds times)
        testTask = mlAdapter.getTestTask();
        testTask.setType(testTask.getType() + "-" + experimentName);

        if (innerReports != null) {
            for (Class<? extends Report> report : innerReports) {
                testTask.addReport(report);
            }
        }

        // always add default report
        testTask.addReport(mlAdapter.getClassificationReportClass());
        // always add OutcomeIdReport
        testTask.addReport(mlAdapter.getOutcomeIdReportClass());

        testTask.addImport(extractFeaturesTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
        testTask.addImport(extractFeaturesTestTask, ExtractFeaturesTask.OUTPUT_KEY,
                Constants.TEST_TASK_INPUT_KEY_TEST_DATA);

        // ================== CONFIG OF THE INNER BATCH TASK =======================

        crossValidationTask.addImport(preprocessTask, PreprocessTask.OUTPUT_KEY_TRAIN);
        crossValidationTask.setType(crossValidationTask.getType() + experimentName);
        crossValidationTask.addTask(metaTask);
        crossValidationTask.addTask(extractFeaturesTrainTask);
        crossValidationTask.addTask(extractFeaturesTestTask);
        crossValidationTask.addTask(testTask);
        // report of the inner batch task (sums up results for the folds)
        // we want to re-use the old CV report, we need to collect the evaluation.bin files from
        // the test task here (with another report)
        crossValidationTask.addReport(mlAdapter.getBatchTrainTestReportClass());

        // DKPro Lab issue 38: must be added as *first* task
        addTask(checkTask);
        addTask(preprocessTask);
        addTask(crossValidationTask);
    }

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        init();
        super.execute(aContext);
    }

    protected FoldDimensionBundle<String> getFoldDim(String[] fileNames) {
        return new CustomFoldDimensionBundle<String>("files", Dimension.create("", fileNames), foldFile, useDevSet);
    }

    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public void setMachineLearningAdapter(Class<? extends TCMachineLearningAdapter> mlAdapter)
    	throws TextClassificationException
    {
        try {
			this.mlAdapter = mlAdapter.newInstance();
		} catch (InstantiationException e) {
			throw new TextClassificationException(e);
		} catch (IllegalAccessException e) {
			throw new TextClassificationException(e);
		}
    }

    public void setPreprocessing(AnalysisEngineDescription preprocessing)
    {
        this.preprocessing = preprocessing;
    }

    public void setOperativeViews(List<String> operativeViews)
    {
        this.operativeViews = operativeViews;
    }

    /**
     * Adds a report for the inner test task
     * 
     * @param innerReport
     *            classification report or regression report
     */
    public void addInnerReport(Class<? extends Report> innerReport)
    {
        if (innerReports == null) {
            innerReports = new ArrayList<Class<? extends Report>>();
        }
        this.innerReports.add(innerReport);
    }

}