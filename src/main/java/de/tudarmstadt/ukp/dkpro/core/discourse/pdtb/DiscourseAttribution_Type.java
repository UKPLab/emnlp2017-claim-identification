
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

/* First created by JCasGen Wed Mar 23 19:38:34 CET 2016 */
package de.tudarmstadt.ukp.dkpro.core.discourse.pdtb;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** Attribution annotation (see PTDB for details); not connected
				to any particular relation as it may belong to two relations thus is covered
				by DiscourseRelation
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * @generated */
public class DiscourseAttribution_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DiscourseAttribution_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DiscourseAttribution_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DiscourseAttribution(addr, DiscourseAttribution_Type.this);
  			   DiscourseAttribution_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DiscourseAttribution(addr, DiscourseAttribution_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DiscourseAttribution.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseAttribution");
 
  /** @generated */
  final Feature casFeat_attributeId;
  /** @generated */
  final int     casFeatCode_attributeId;
  /** @generated */ 
  public int getAttributeId(int addr) {
        if (featOkTst && casFeat_attributeId == null)
      jcas.throwFeatMissing("attributeId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseAttribution");
    return ll_cas.ll_getIntValue(addr, casFeatCode_attributeId);
  }
  /** @generated */    
  public void setAttributeId(int addr, int v) {
        if (featOkTst && casFeat_attributeId == null)
      jcas.throwFeatMissing("attributeId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseAttribution");
    ll_cas.ll_setIntValue(addr, casFeatCode_attributeId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DiscourseAttribution_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_attributeId = jcas.getRequiredFeatureDE(casType, "attributeId", "uima.cas.Integer", featOkTst);
    casFeatCode_attributeId  = (null == casFeat_attributeId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_attributeId).getCode();

  }
}



    