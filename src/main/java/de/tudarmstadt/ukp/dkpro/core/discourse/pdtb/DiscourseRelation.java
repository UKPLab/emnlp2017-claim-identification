

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

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** Discourse relation
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * XML source: /Users/johannesdaxenberger/workspaces/experiments/de.tudarmstadt.ukp.dkpro.core.api.discourse-asl/target/jcasgen/typesystem.xml
 * @generated */
public class DiscourseRelation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DiscourseRelation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DiscourseRelation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DiscourseRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DiscourseRelation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DiscourseRelation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: relationId

  /** getter for relationId - gets id of the relation
   * @generated */
  public int getRelationId() {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_relationId == null)
      jcasType.jcas.throwFeatMissing("relationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_relationId);}
    
  /** setter for relationId - sets id of the relation 
   * @generated */
  public void setRelationId(int v) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_relationId == null)
      jcasType.jcas.throwFeatMissing("relationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    jcasType.ll_cas.ll_setIntValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_relationId, v);}    
   
    
  //*--------------*
  //* Feature: arg1

  /** getter for arg1 - gets arg 1
   * @generated */
  public DiscourseArgument getArg1() {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_arg1 == null)
      jcasType.jcas.throwFeatMissing("arg1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    return (DiscourseArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arg1)));}
    
  /** setter for arg1 - sets arg 1 
   * @generated */
  public void setArg1(DiscourseArgument v) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_arg1 == null)
      jcasType.jcas.throwFeatMissing("arg1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arg1, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: arg2

  /** getter for arg2 - gets arg 2
   * @generated */
  public DiscourseArgument getArg2() {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_arg2 == null)
      jcasType.jcas.throwFeatMissing("arg2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    return (DiscourseArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arg2)));}
    
  /** setter for arg2 - sets arg 2 
   * @generated */
  public void setArg2(DiscourseArgument v) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_arg2 == null)
      jcasType.jcas.throwFeatMissing("arg2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arg2, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    