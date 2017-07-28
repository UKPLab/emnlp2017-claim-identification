

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


/** Discourse argument (arg1, arg2)
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * XML source: /Users/johannesdaxenberger/workspaces/experiments/de.tudarmstadt.ukp.dkpro.core.api.discourse-asl/target/jcasgen/typesystem.xml
 * @generated */
public class DiscourseArgument extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DiscourseArgument.class);
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
  protected DiscourseArgument() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DiscourseArgument(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DiscourseArgument(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DiscourseArgument(JCas jcas, int begin, int end) {
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
  //* Feature: parentRelationId

  /** getter for parentRelationId - gets ID of the parent relation
   * @generated */
  public int getParentRelationId() {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_parentRelationId == null)
      jcasType.jcas.throwFeatMissing("parentRelationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    return jcasType.ll_cas.ll_getIntValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_parentRelationId);}
    
  /** setter for parentRelationId - sets ID of the parent relation 
   * @generated */
  public void setParentRelationId(int v) {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_parentRelationId == null)
      jcasType.jcas.throwFeatMissing("parentRelationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    jcasType.ll_cas.ll_setIntValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_parentRelationId, v);}    
   
    
  //*--------------*
  //* Feature: argumentNumber

  /** getter for argumentNumber - gets 1 or 2
   * @generated */
  public int getArgumentNumber() {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_argumentNumber == null)
      jcasType.jcas.throwFeatMissing("argumentNumber", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    return jcasType.ll_cas.ll_getIntValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_argumentNumber);}
    
  /** setter for argumentNumber - sets 1 or 2 
   * @generated */
  public void setArgumentNumber(int v) {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_argumentNumber == null)
      jcasType.jcas.throwFeatMissing("argumentNumber", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    jcasType.ll_cas.ll_setIntValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_argumentNumber, v);}    
   
    
  //*--------------*
  //* Feature: argumentType

  /** getter for argumentType - gets argument type, e.g. Cause, etc.
   * @generated */
  public String getArgumentType() {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_argumentType == null)
      jcasType.jcas.throwFeatMissing("argumentType", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_argumentType);}
    
  /** setter for argumentType - sets argument type, e.g. Cause, etc. 
   * @generated */
  public void setArgumentType(String v) {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_argumentType == null)
      jcasType.jcas.throwFeatMissing("argumentType", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseArgument");
    jcasType.ll_cas.ll_setStringValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_argumentType, v);}    
  }

    