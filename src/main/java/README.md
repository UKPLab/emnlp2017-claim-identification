Running the Java-based experiments:

* install dependencies with Maven: run `mvn clean install -U` in the project root folder
* this will create a runable jar in the `target` folder
* experimental main classes can be found in `de/tudarmstadt/ukp/dkpro/argumentation/crossdomainclaims/experiments`, e.g. TrainTestSampledLODO
* to run, execute e.g. `java -cp de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims-0.0.1-SNAPSHOT-standalone.jar
de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.TrainTestSampledLODO
parent-dir-traindata-sentence
parent-dir-testdata-document
Habernal2015 1 results discourse EN EN`
