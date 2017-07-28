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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.utils;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.syntactic.PunctuationSequenceUFE;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.FreqDistBasedMetaCollector;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunctuationSequenceMetaCollector extends FreqDistBasedMetaCollector {
    
    public static final String PARAM_PUNCTUATION_SEQUENCE_FD_FILE = "punctSequenceFdFile";
    @ConfigurationParameter(name = PARAM_PUNCTUATION_SEQUENCE_FD_FILE, mandatory = true)
    private File punctSequenceFdFile;
    
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
    }
    
    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
    	Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
    	for (Sentence sentence : sentences) {
    		String sequences = createPunctuationSequence(sentence.getCoveredText());
    		fd.addSample(sequences, 1);
		}
    }

    @Override
    public Map<String, String> getParameterKeyPairs() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(PunctuationSequenceUFE.PARAM_PUNCTUATION_SEQUENCE_FD_FILE, PARAM_PUNCTUATION_SEQUENCE_FD_FILE);
        return mapping;
    }

    @Override
    protected File getFreqDistFile() {
        return punctSequenceFdFile;
    }
    
	public static String createPunctuationSequence(String text){
		// normalize quotes
		String normalized = text.replaceAll("'","\"");
		normalized = normalized.replaceAll("`", "\"");
		normalized = normalized.replaceAll("´", "\"");
		
		Matcher punctPattern = Pattern.compile("\\p{Punct}").matcher(normalized);
		StringBuilder pattern = new StringBuilder();
		
		while (punctPattern.find()) {
			pattern.append(normalized.charAt(punctPattern.start()));
		}
		return pattern.toString();
	}
}
