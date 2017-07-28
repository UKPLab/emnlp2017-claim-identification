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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.sentiment;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.FeatureUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

/**
 * Returns five double-valued features for very neg, neg, neu, pos, very pos from Stanford
 * sentiment analysis
 *
 * @author Ivan Habernal
 */
public class StanfordSentimentUFE
	extends FeatureExtractorResource_ImplBase
	implements ClassificationUnitFeatureExtractor{
    static Logger log = Logger.getLogger(StanfordSentimentUFE.class);

    @Override
	public List<Feature> extract(JCas jCas, TextClassificationUnit classificationUnit)
            throws TextClassificationException
    {
        List<Feature> result = new ArrayList<>();
        Sentence sentence = FeatureUtils.getCoveringSentence(classificationUnit);

        List<StanfordSentimentAnnotation> sentimentAnnotations = selectCovered(
                StanfordSentimentAnnotation.class, sentence);
        if (sentimentAnnotations.size() != 1) {
            log.warn("No sentiment annotations for sentence " + sentence.getCoveredText());
            result.add(new Feature("sentimentVeryNegative", 0));
            result.add(new Feature("sentimentNegative", 0));
            result.add(new Feature("sentimentNeutral", 0));
            result.add(new Feature("sentimentPositive", 0));
            result.add(new Feature("sentimentVeryPositive", 0));
        }
        else {
            StanfordSentimentAnnotation sentiment = sentimentAnnotations.get(0);

            result.add(new Feature("sentimentVeryNegative",
                    sentiment.getVeryNegative()));
            result.add(new Feature("sentimentNegative", sentiment.getNegative()));
            result.add(new Feature("sentimentNeutral", sentiment.getNeutral()));
            result.add(new Feature("sentimentPositive", sentiment.getPositive()));
            result.add(new Feature("sentimentVeryPositive",
                    sentiment.getVeryPositive()));
        }
        return result;
    }
}
