
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

/** Discourse relation
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * @generated */
public class ExplicitDiscourseRelation_Type extends DiscourseRelation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ExplicitDiscourseRelation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ExplicitDiscourseRelation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ExplicitDiscourseRelation(addr, ExplicitDiscourseRelation_Type.this);
  			   ExplicitDiscourseRelation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ExplicitDiscourseRelation(addr, ExplicitDiscourseRelation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ExplicitDiscourseRelation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
 
  /** @generated */
  final Feature casFeat_discourseConnective1;
  /** @generated */
  final int     casFeatCode_discourseConnective1;
  /** @generated */ 
  public int getDiscourseConnective1(int addr) {
        if (featOkTst && casFeat_discourseConnective1 == null)
      jcas.throwFeatMissing("discourseConnective1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_discourseConnective1);
  }
  /** @generated */    
  public void setDiscourseConnective1(int addr, int v) {
        if (featOkTst && casFeat_discourseConnective1 == null)
      jcas.throwFeatMissing("discourseConnective1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
    ll_cas.ll_setRefValue(addr, casFeatCode_discourseConnective1, v);}
    
  
 
  /** @generated */
  final Feature casFeat_discourseConnective2;
  /** @generated */
  final int     casFeatCode_discourseConnective2;
  /** @generated */ 
  public int getDiscourseConnective2(int addr) {
        if (featOkTst && casFeat_discourseConnective2 == null)
      jcas.throwFeatMissing("discourseConnective2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_discourseConnective2);
  }
  /** @generated */    
  public void setDiscourseConnective2(int addr, int v) {
        if (featOkTst && casFeat_discourseConnective2 == null)
      jcas.throwFeatMissing("discourseConnective2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
    ll_cas.ll_setRefValue(addr, casFeatCode_discourseConnective2, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public ExplicitDiscourseRelation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_discourseConnective1 = jcas.getRequiredFeatureDE(casType, "discourseConnective1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseConnective", featOkTst);
    casFeatCode_discourseConnective1  = (null == casFeat_discourseConnective1) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_discourseConnective1).getCode();

 
    casFeat_discourseConnective2 = jcas.getRequiredFeatureDE(casType, "discourseConnective2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseConnective", featOkTst);
    casFeatCode_discourseConnective2  = (null == casFeat_discourseConnective2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_discourseConnective2).getCode();

  }
}



    