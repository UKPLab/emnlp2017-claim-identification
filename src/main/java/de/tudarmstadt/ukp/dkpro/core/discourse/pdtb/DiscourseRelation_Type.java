
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

/** Discourse relation
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * @generated */
public class DiscourseRelation_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DiscourseRelation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DiscourseRelation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DiscourseRelation(addr, DiscourseRelation_Type.this);
  			   DiscourseRelation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DiscourseRelation(addr, DiscourseRelation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DiscourseRelation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
 
  /** @generated */
  final Feature casFeat_relationId;
  /** @generated */
  final int     casFeatCode_relationId;
  /** @generated */ 
  public int getRelationId(int addr) {
        if (featOkTst && casFeat_relationId == null)
      jcas.throwFeatMissing("relationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_relationId);
  }
  /** @generated */    
  public void setRelationId(int addr, int v) {
        if (featOkTst && casFeat_relationId == null)
      jcas.throwFeatMissing("relationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    ll_cas.ll_setIntValue(addr, casFeatCode_relationId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_arg1;
  /** @generated */
  final int     casFeatCode_arg1;
  /** @generated */ 
  public int getArg1(int addr) {
        if (featOkTst && casFeat_arg1 == null)
      jcas.throwFeatMissing("arg1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_arg1);
  }
  /** @generated */    
  public void setArg1(int addr, int v) {
        if (featOkTst && casFeat_arg1 == null)
      jcas.throwFeatMissing("arg1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    ll_cas.ll_setRefValue(addr, casFeatCode_arg1, v);}
    
  
 
  /** @generated */
  final Feature casFeat_arg2;
  /** @generated */
  final int     casFeatCode_arg2;
  /** @generated */ 
  public int getArg2(int addr) {
        if (featOkTst && casFeat_arg2 == null)
      jcas.throwFeatMissing("arg2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_arg2);
  }
  /** @generated */    
  public void setArg2(int addr, int v) {
        if (featOkTst && casFeat_arg2 == null)
      jcas.throwFeatMissing("arg2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    ll_cas.ll_setRefValue(addr, casFeatCode_arg2, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DiscourseRelation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_relationId = jcas.getRequiredFeatureDE(casType, "relationId", "uima.cas.Integer", featOkTst);
    casFeatCode_relationId  = (null == casFeat_relationId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relationId).getCode();

 
    casFeat_arg1 = jcas.getRequiredFeatureDE(casType, "arg1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument", featOkTst);
    casFeatCode_arg1  = (null == casFeat_arg1) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_arg1).getCode();

 
    casFeat_arg2 = jcas.getRequiredFeatureDE(casType, "arg2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument", featOkTst);
    casFeatCode_arg2  = (null == casFeat_arg2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_arg2).getCode();

  }
}



    