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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.lexical;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.lexical.utils.NGramUtils;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LuceneNgramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneNGramMetaCollector;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LemmaNGrams extends LuceneNgramFeatureExtractorBase implements ClassificationUnitFeatureExtractor {
	
	
	@Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses() {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LuceneNGramMetaCollector.class);

        return metaCollectorClasses;
    }

    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit) throws TextClassificationException {
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentNgrams = null;

        List<Token> tokens = JCasUtil.selectCovered(Token.class, classificationUnit);
        
        documentNgrams = NGramUtils.getAnnotationNgramsLemmatized(jcas, tokens, ngramLowerCase,
                filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);

        for (String topNgram : topKSet.getKeys()) {
            if (documentNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0));
            }
        }
        return features;
    }
}