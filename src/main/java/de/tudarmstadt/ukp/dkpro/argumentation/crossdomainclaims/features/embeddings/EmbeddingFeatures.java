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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.embeddings;

 import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.FeatureUtils;
import de.tudarmstadt.ukp.dkpro.argumentation.type.Embeddings;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import no.uib.cipr.matrix.DenseVector;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class EmbeddingFeatures
	extends FeatureExtractorResource_ImplBase
	implements ClassificationUnitFeatureExtractor {


    private static final String FEATURE_NAME = "_wordEmbedding_";

    @Override
	public List<Feature> extract(JCas jCas, TextClassificationUnit classificationUnit)
            throws TextClassificationException
    {
        // and load the appropriate distance to centroids
        Sentence sentence = FeatureUtils.getCoveringSentence(classificationUnit);
        List<Embeddings> embeddingsList = JCasUtil.selectCovered(Embeddings.class, sentence);

        if (embeddingsList.size() != 1) {
            throw new TextClassificationException(new IllegalStateException(
                    "Expected 1 embedding annotations for sentence, but " +
                            embeddingsList.size() + " found." +
                            "Sentence: " + sentence.getBegin() + sentence.getEnd() + ", "
                            + StringUtils.join(embeddingsList.iterator(), "\n")));
        }

        Embeddings embeddings = embeddingsList.iterator().next();
        DenseVector embeddingsVector = new DenseVector(embeddings.getVector().toArray());

        List<Feature> result = new ArrayList<>(embeddingsVector.size());

        for (int i = 0; i < embeddingsVector.size(); i++) {
            double entry = embeddingsVector.get(i);
            result.add(new Feature(FEATURE_NAME + i, entry));
        }

        return result;
    }
}
