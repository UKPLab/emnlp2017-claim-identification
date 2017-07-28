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
package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePOSNGramFeatureExtractorBase;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.List;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

/**
 * Extracts POS n-grams.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LucenePOSNGramUFE
    extends LucenePOSNGramFeatureExtractorBase
    implements ClassificationUnitFeatureExtractor
{
	
    /**
     * This is the character for joining strings for pair ngrams.
     */
    public static final String NGRAM_GLUE = "_";

	@Override
	public List<Feature> extract(JCas view, TextClassificationUnit classificationUnit)
			throws TextClassificationException {
    	
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentPOSNgrams = null;
        documentPOSNgrams = getDocumentPosNgrams(view, classificationUnit, posNgramMinN, posNgramMaxN, useCanonicalTags);

        for (String topNgram : topKSet.getKeys()) {
            if (documentPOSNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0));
            }
        }
        return features;   
	}
	
	public static FrequencyDistribution<String> getDocumentPosNgrams(JCas jcas, Annotation focusAnnotation, int minN,
			int maxN, boolean useCanonical) {
		FrequencyDistribution<String> posNgrams = new FrequencyDistribution<String>();

		if (JCasUtil.selectCovered(jcas, Sentence.class, focusAnnotation).size() > 0) {
			for (Sentence s : selectCovered(jcas, Sentence.class, focusAnnotation)) {
				List<String> postagstrings = new ArrayList<String>();
				for (POS p : JCasUtil.selectCovered(jcas, POS.class, s)) {
					if (useCanonical) {
						postagstrings.add(p.getClass().getSimpleName());
					} else {
						postagstrings.add(p.getPosValue());
					}
				}
				String[] posarray = postagstrings.toArray(new String[postagstrings.size()]);
				for (List<String> ngram : new NGramStringListIterable(posarray, minN, maxN)) {
					posNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
				}
			}
		} else {
			List<String> postagstrings = new ArrayList<String>();
			for (POS p : selectCovered(POS.class, focusAnnotation)) {
				if (useCanonical) {
					postagstrings.add(p.getClass().getSimpleName());
				} else {
					postagstrings.add(p.getPosValue());
				}
			}
			String[] posarray = postagstrings.toArray(new String[postagstrings.size()]);
			for (List<String> ngram : new NGramStringListIterable(posarray, minN, maxN)) {
				posNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
			}
		}
		return posNgrams;
	}
}


