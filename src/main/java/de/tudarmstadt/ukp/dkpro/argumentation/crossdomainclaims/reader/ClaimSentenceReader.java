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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.reader;

import de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.utils.ArgUtils;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.Collection;

/**
 * 
 * @author Christian Stab
 */
public class ClaimSentenceReader extends XmiReader {
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
	    super.initialize(context);
	}
	
	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
	    super.getNext(aCAS);
	
	    JCas jcas;
	    try {
	        jcas = aCAS.getJCas();
	    } catch (CASException e) {
	        throw new CollectionException();
	    }
	    
	    DocumentMetaData.get(jcas).setDocumentTitle(DocumentMetaData.get(jcas).getDocumentId());
	    
	    Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
	    
	    for (Sentence s : sentences) {
			new TextClassificationUnit(jcas, s.getBegin(), s.getEnd()).addToIndexes();
			TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, s.getBegin(), s.getEnd());
	    	
	    	if (ArgUtils.isClaim(s, jcas)) {
    			outcome.setOutcome("Claim");
	    	} else {
   				outcome.setOutcome("None");
	    	}
	    	outcome.addToIndexes();
	    }
	}

}
