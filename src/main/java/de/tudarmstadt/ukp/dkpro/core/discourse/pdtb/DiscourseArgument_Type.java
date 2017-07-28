
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

/** Discourse argument (arg1, arg2)
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * @generated */
public class DiscourseArgument_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DiscourseArgument_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DiscourseArgument_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DiscourseArgument(addr, DiscourseArgument_Type.this);
  			   DiscourseArgument_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DiscourseArgument(addr, DiscourseArgument_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DiscourseArgument.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
 
  /** @generated */
  final Feature casFeat_parentRelationId;
  /** @generated */
  final int     casFeatCode_parentRelationId;
  /** @generated */ 
  public int getParentRelationId(int addr) {
        if (featOkTst && casFeat_parentRelationId == null)
      jcas.throwFeatMissing("parentRelationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    return ll_cas.ll_getIntValue(addr, casFeatCode_parentRelationId);
  }
  /** @generated */    
  public void setParentRelationId(int addr, int v) {
        if (featOkTst && casFeat_parentRelationId == null)
      jcas.throwFeatMissing("parentRelationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    ll_cas.ll_setIntValue(addr, casFeatCode_parentRelationId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_argumentNumber;
  /** @generated */
  final int     casFeatCode_argumentNumber;
  /** @generated */ 
  public int getArgumentNumber(int addr) {
        if (featOkTst && casFeat_argumentNumber == null)
      jcas.throwFeatMissing("argumentNumber", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    return ll_cas.ll_getIntValue(addr, casFeatCode_argumentNumber);
  }
  /** @generated */    
  public void setArgumentNumber(int addr, int v) {
        if (featOkTst && casFeat_argumentNumber == null)
      jcas.throwFeatMissing("argumentNumber", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    ll_cas.ll_setIntValue(addr, casFeatCode_argumentNumber, v);}
    
  
 
  /** @generated */
  final Feature casFeat_argumentType;
  /** @generated */
  final int     casFeatCode_argumentType;
  /** @generated */ 
  public String getArgumentType(int addr) {
        if (featOkTst && casFeat_argumentType == null)
      jcas.throwFeatMissing("argumentType", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    return ll_cas.ll_getStringValue(addr, casFeatCode_argumentType);
  }
  /** @generated */    
  public void setArgumentType(int addr, String v) {
        if (featOkTst && casFeat_argumentType == null)
      jcas.throwFeatMissing("argumentType", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    ll_cas.ll_setStringValue(addr, casFeatCode_argumentType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DiscourseArgument_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_parentRelationId = jcas.getRequiredFeatureDE(casType, "parentRelationId", "uima.cas.Integer", featOkTst);
    casFeatCode_parentRelationId  = (null == casFeat_parentRelationId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_parentRelationId).getCode();

 
    casFeat_argumentNumber = jcas.getRequiredFeatureDE(casType, "argumentNumber", "uima.cas.Integer", featOkTst);
    casFeatCode_argumentNumber  = (null == casFeat_argumentNumber) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_argumentNumber).getCode();

 
    casFeat_argumentType = jcas.getRequiredFeatureDE(casType, "argumentType", "uima.cas.String", featOkTst);
    casFeatCode_argumentType  = (null == casFeat_argumentType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_argumentType).getCode();

  }
}



    