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

import de.bwaldvogel.liblinear.*;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
<pre>
{@literal
  -s type : set type of solver (default 1)
      0 -- L2-regularized logistic regression (primal)
      1 -- L2-regularized L2-loss support vector classification (dual)
      2 -- L2-regularized L2-loss support vector classification (primal)
      3 -- L2-regularized L1-loss support vector classification (dual)
      4 -- multi-class support vector classification by Crammer and Singer
      5 -- L1-regularized L2-loss support vector classification
      6 -- L1-regularized logistic regression
      7 -- L2-regularized logistic regression (dual)
     11 -- L2-regularized L2-loss support vector regression (dual)
     12 -- L2-regularized L1-loss support vector regression (dual)
     13 -- L2-regularized L2-loss support vector regression (primal)
  -c cost : set the parameter C (default 1)
  -e epsilon : set tolerance of termination criterion
    -s 0 and 2
        |f'(w)|_2 &lt;= eps*min(pos,neg)/l*|f'(w0)|_2,
        where f is the primal function and pos/neg are # of
        positive/negative data (default 0.01)
    -s 1, 3, 4 and 7
       Dual maximal violation <= eps; similar to libsvm (default 0.1)
    -s 5 and 6
       |f'(w)|_inf &le; eps*min(pos,neg)/l*|f'(w0)|_inf,
       where f is the primal function (default 0.01)
  -B bias : if bias &ge 0, instance x becomes [x; bias]; if &lt; 0, no bias term added (default -1)
  -wi weight: weights adjust the parameter C of different classes (see README for details)
  }
 </pre>
*/
public class LiblinearTestTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator
    private List<String> classificationArguments;
    @Discriminator
    private String featureMode;
    @Discriminator
    private String learningMode;
    @Discriminator
    private String balanceRandomSeed;

    public static String SEPARATOR_CHAR = ";";
    public static final double EPISILON_DEFAULT = 0.01;
    public static final double PARAM_C_DEFAULT = 1.0;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        if (multiLabel) {
            throw new TextClassificationException(
                    "Multi-label requested, but LIBLINEAR only supports single label setups.");
        }

        boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);

        File fileTrain = getTrainFile(aContext);
        File fileTest = getTestFile(aContext);

        Map<String, Integer> outcomeMapping = LiblinearUtils.createMapping(isRegression, fileTrain,
                fileTest);
        
         
        String idMappedTrainString = LiblinearUtils.replaceOutcome(fileTrain, outcomeMapping);
        // if requested, class-balance the training data (only applicable to binary classification scenarios)
        File idMappedTrainTempFile = LiblinearUtils.balanceAndConvert(idMappedTrainString, balanceRandomSeed);
        String idMappedTestString = LiblinearUtils.replaceOutcome(fileTest, outcomeMapping);
        
        writeMapping(aContext, outcomeMapping, isRegression);
        
        // write test file
        File idMappedTestTempFile = File.createTempFile("liblinear" + System.nanoTime(), ".tmp");
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(idMappedTestTempFile), "utf-8"));
        bw.write(idMappedTestString);
        bw.close();

        // default for bias is -1, documentation says to set it to 1 in order to get results closer
        // to libsvm
        // writer adds bias, so if we de-activate that here for some reason, we need to also
        // deactivate it there
        Problem train = Problem.readFromFile(idMappedTrainTempFile, 1.0);

        SolverType solver = LiblinearUtils.getSolver(classificationArguments);
        double C = LiblinearUtils.getParameterC(classificationArguments);
        double eps = LiblinearUtils.getParameterEpsilon(classificationArguments);

        Linear.setDebugOutput(null);

        Parameter parameter = new Parameter(solver, C, eps);
        Model model = Linear.train(train, parameter);

        Problem test = Problem.readFromFile(idMappedTestTempFile, 1.0);

        // we should delete the temporary files
        FileUtils.forceDelete(idMappedTestTempFile);
        FileUtils.forceDelete(idMappedTrainTempFile);

        predict(aContext, model, test);
    }

    private void writeMapping(TaskContext aContext, Map<String, Integer> outcomeMapping,
            boolean isRegression)
                throws IOException
    {
        if (isRegression) {
            return;
        }
        File mappingFile = new File(aContext.getStorageLocation("", AccessMode.READWRITE),
                LiblinearAdapter.getOutcomeMappingFilename());
        FileUtils.writeStringToFile(mappingFile, LiblinearUtils.outcomeMap2String(outcomeMapping));
    }

    private File getTestFile(TaskContext aContext)
    {
        File testFolder = aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        String testFileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTest = new File(testFolder, testFileName);
        return fileTest;
    }

    private File getTrainFile(TaskContext aContext)
    {
        File trainFolder = aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, trainFileName);

        return fileTrain;
    }

    private void predict(TaskContext aContext, Model model, Problem test)
        throws Exception
    {
        File predFolder = aContext.getStorageLocation("", AccessMode.READWRITE);
        String predFileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.predictionsFile);
        File predictionsFile = new File(predFolder, predFileName);

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(predictionsFile), "utf-8"));
        writer.append("#PREDICTION;GOLD" + "\n");

        Feature[][] testInstances = test.x;
        for (int i = 0; i < testInstances.length; i++) {
            Feature[] instance = testInstances[i];
            Double prediction = Linear.predict(model, instance);

            writer.write(prediction.intValue() + SEPARATOR_CHAR + new Double(test.y[i]).intValue());
            writer.write("\n");
        }

        writer.close();
    }

}