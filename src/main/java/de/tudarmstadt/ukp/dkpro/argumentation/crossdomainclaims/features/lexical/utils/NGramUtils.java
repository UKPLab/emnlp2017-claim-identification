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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.lexical.utils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.SkipNgramStringListIterable;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

import static org.apache.uima.fit.util.JCasUtil.*;

public class NGramUtils
{
	/**
	 * This is the character for joining strings for pair ngrams.
	 */
    public static String NGRAM_GLUE = "_";
    
    public static FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation focusAnnotation,
            boolean lowerCaseNGrams, boolean filterPartialMatches, int minN, int maxN)
    {
        Set<String> empty = Collections.emptySet();
        return getAnnotationNgrams(jcas, focusAnnotation, lowerCaseNGrams, filterPartialMatches, minN, maxN, empty);
    }

    
    public static FrequencyDistribution<String> getAnnotationNgramsLemmatized(
            JCas jcas,
            Collection<Token> tokens,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> annoNgrams = new FrequencyDistribution<String>();
        
        for (List<String> ngram : new NGramStringListIterable(getLemmata(tokens), minN, maxN)) {
        	
        	if(lowerCaseNGrams){
        		ngram = lower(ngram);
        	}

            if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                annoNgrams.inc(ngramString);
            }
        }
        return annoNgrams;
    }
    
    
    private static String[] getLemmata(Collection<Token> tokens) {
    	String[] result = new String[tokens.size()];
    	
    	Token[] ts = tokens.toArray(new Token[tokens.size()]);
    	
    	for (int i=0; i<tokens.size(); i++) {
    		result[i] = ts[i].getLemma().getValue();
    	}
    	return result;
    }
    
    
    public static FrequencyDistribution<String> getAnnotationNgrams(
            JCas jcas,
            Collection<Token> tokens,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> annoNgrams = new FrequencyDistribution<String>();
        
        for (List<String> ngram : new NGramStringListIterable(toText(tokens), minN, maxN)) {
        	
        	if(lowerCaseNGrams){
        		ngram = lower(ngram);
        	}

            if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                annoNgrams.inc(ngramString);
            }
        }
        return annoNgrams;
    }

    
    
    public static FrequencyDistribution<String> getAnnotationNgrams(
            JCas jcas,
            Annotation focusAnnotation,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> annoNgrams = new FrequencyDistribution<String>();

        // If the focusAnnotation contains sentence annotations, extract the ngrams sentence-wise
        // if not, extract them from all tokens in the focusAnnotation
        if (JCasUtil.selectCovered(jcas, Sentence.class, focusAnnotation).size() > 0) {
            for (Sentence s : selectCovered(jcas, Sentence.class, focusAnnotation)) {
                for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(
                        Token.class, s)), minN, maxN)) {
                	
                	if(lowerCaseNGrams){
                		ngram = lower(ngram);
                	}

                    if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                        String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                        annoNgrams.inc(ngramString);
                    }
                }
            }
        }
        // FIXME the focus annotation branch doesn't make much sense
        else {
            for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(Token.class,
                    focusAnnotation)), minN, maxN)) {
            	
            	if(lowerCaseNGrams){
            		ngram = lower(ngram);
            	}

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    annoNgrams.inc(ngramString);
                }
            }
        }
        return annoNgrams;
    }


    public static FrequencyDistribution<String> getDocumentNgrams(JCas jcas,
            boolean lowerCaseNGrams, boolean filterPartialMatches, int minN, int maxN)
    {
        Set<String> empty = Collections.emptySet();
        return getDocumentNgrams(jcas, lowerCaseNGrams, filterPartialMatches, minN, maxN, empty);
    }

    public static FrequencyDistribution<String> getDocumentNgrams(
            JCas jcas,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {
            for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(Token.class,
                    s)), minN, maxN)) {

            	if(lowerCaseNGrams){
            		ngram = lower(ngram);
            	}

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    documentNgrams.inc(ngramString);
                }
            }
        }
        return documentNgrams;
    }
    
    
    public static FrequencyDistribution<String> getDocumentLemmaNgrams(
            JCas jcas,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {
            for (List<String> ngram : new NGramStringListIterable(getLemmata(selectCovered(Token.class,
                    s)), minN, maxN)) {

            	if(lowerCaseNGrams){
            		ngram = lower(ngram);
            	}

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    documentNgrams.inc(ngramString);
                }
            }
        }
        return documentNgrams;
    }
    

    public static FrequencyDistribution<String> getDocumentPosNgrams(JCas jcas, int minN, int maxN, boolean useCanonical)
    {
        FrequencyDistribution<String> posNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {        
            List<String> postagstrings = new ArrayList<String>();
            for (POS p : JCasUtil.selectCovered(jcas, POS.class, s)) {
                if (useCanonical) {
                    postagstrings.add(p.getClass().getSimpleName());
                }
                else {
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
    
    public static FrequencyDistribution<String> getAnnotationPosNgrams(JCas jcas, Annotation anno, int minN, int maxN, boolean useCanonical) {
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        
        System.err.println("Attention: unit classification POS ngrams are not yet implemented");
        // FIXME implement this
        return fd;
    }


    /**
     * An ngram (represented by the list of tokens) does not pass the stopword filter:
     * a) filterPartialMatches=true - if it contains any stopwords
     * b) filterPartialMatches=false - if it entirely consists of stopwords
     * 
     * @param tokenList The list of tokens in a single ngram
     * @param stopwords The set of stopwords used for filtering
     * @param filterPartialMatches Whether ngrams where only parts are stopwords should also be filtered. For example, "United States of America" would be filtered, as it contains the stopword "of".
     * @return Whether the ngram (represented by the list of tokens) passes the stopword filter or not. 
     */
    public static boolean passesNgramFilter(List<String> tokenList, Set<String> stopwords, boolean filterPartialMatches)
    {
    	List<String> filteredList = new ArrayList<String>();
        for (String ngram : tokenList) {
            if (!stopwords.contains(ngram)) {
                filteredList.add(ngram);
            }
        }
        
        if (filterPartialMatches) {
            return filteredList.size() == tokenList.size();
        }
        else {
            return filteredList.size() != 0;
        }
    }

    public static FrequencyDistribution<String> getDocumentSkipNgrams(
            JCas jcas,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            int skipN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {
            for (List<String> ngram : new SkipNgramStringListIterable(
                    toText(selectCovered(Token.class, s)), minN, maxN, skipN))
            {
            	if(lowerCaseNGrams){
            		ngram = lower(ngram);
            	}

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    documentNgrams.inc(ngramString);
                }
            }
        }
        return documentNgrams;
    }
    
    public static List<String> lower(List<String> ngram){
    	List<String> newNgram = new ArrayList<String>();
    	for(String token: ngram){
    		newNgram.add(token.toLowerCase());
    	}
    	return newNgram;
    }
}