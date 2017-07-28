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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FeatureUtils {

	/**
	 * Returns a covering paragraph of an argument component
	 * @param classificationUnit
	 * @return
	 */
	public static Paragraph getCoveringParagraph(Annotation classificationUnit) {
		Collection<Paragraph> sentences = JCasUtil.selectCovering(Paragraph.class, classificationUnit);
		if (sentences.size()!=1) return null;
		else return sentences.iterator().next();
	}
	
	/**
	 * Returns a covering sentence of an argument component
	 * @param classificationUnit
	 * @return
	 */
	public static Sentence getCoveringSentence(Annotation classificationUnit) {
		Collection<Sentence> sentences = JCasUtil.selectCovering(Sentence.class, classificationUnit);
		if (sentences.size()!=1) return null;
		else return sentences.iterator().next();
	}
	
    /**
     * Selects annotations with desired type ({@code type} parameter) that overlap the given
     * {@code annotation}, such that at least a part of the selected annotations
     * {@link #doOverlap(org.apache.uima.jcas.tcas.Annotation, org.apache.uima.jcas.tcas.Annotation)}
     * with the given {@code annotation}.
     * See {@code JCasUtil2Test.testSelectOverlapping()} for details.
     *
     * @param type       desired type
     * @param annotation current annotation for which the overlapping annotations are being selected
     * @param jCas       the JCas
     * @return collection of overlapping annotations
     */
    public static <T extends TOP> List<T> selectOverlapping(Class<T> type,Annotation annotation, JCas jCas) {
        Collection<T> allAnnotations = JCasUtil.select(jCas, type);

        List<T> result = new ArrayList<T>();

        for (T a : allAnnotations) {
            if ((a instanceof Annotation) && (doOverlap(annotation, (Annotation) a))) {
                result.add(a);
            }
        }

        return result;
    }
    
    

    /**
     * Returns whether the given annotations have a non-empty overlap.
     * <p/>
     * <p>
     * Note that this method is symmetric. Two annotations overlap
     * if they have at least one character position in common.
     * Annotations that merely touch at the begin or end are not
     * overlapping.
     * <p/>
     * <ul>
     * <li>anno1[0,1], anno2[1,2] => no overlap</li>
     * <li>anno1[0,2], anno2[1,2] => overlap</li>
     * <li>anno1[0,2], anno2[0,2] => overlap (same span)</li>
     * </ul>
     * </p>
     *
     * @param anno1 first annotation
     * @param anno2 second annotation
     * @return whether the annotations overlap
     */
    public static <T extends Annotation> boolean doOverlap(final T anno1, final T anno2) {
        return anno1.getEnd() > anno2.getBegin() && anno1.getBegin() < anno2.getEnd();
    }
}
