

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


/** Discourse connective
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * XML source: /Users/johannesdaxenberger/workspaces/experiments/de.tudarmstadt.ukp.dkpro.core.api.discourse-asl/target/jcasgen/typesystem.xml
 * @generated */
public class DiscourseConnective extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DiscourseConnective.class);
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
  protected DiscourseConnective() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DiscourseConnective(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DiscourseConnective(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DiscourseConnective(JCas jcas, int begin, int end) {
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
  //* Feature: connectiveType

  /** getter for connectiveType - gets connective type
   * @generated */
  public String getConnectiveType() {
    if (DiscourseConnective_Type.featOkTst && ((DiscourseConnective_Type)jcasType).casFeat_connectiveType == null)
      jcasType.jcas.throwFeatMissing("connectiveType", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseConnective");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DiscourseConnective_Type)jcasType).casFeatCode_connectiveType);}
    
  /** setter for connectiveType - sets connective type 
   * @generated */
  public void setConnectiveType(String v) {
    if (DiscourseConnective_Type.featOkTst && ((DiscourseConnective_Type)jcasType).casFeat_connectiveType == null)
      jcasType.jcas.throwFeatMissing("connectiveType", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseConnective");
    jcasType.ll_cas.ll_setStringValue(addr, ((DiscourseConnective_Type)jcasType).casFeatCode_connectiveType, v);}    
   
    
  //*--------------*
  //* Feature: parentRelationId

  /** getter for parentRelationId - gets ID of the parent relation
   * @generated */
  public int getParentRelationId() {
    if (DiscourseConnective_Type.featOkTst && ((DiscourseConnective_Type)jcasType).casFeat_parentRelationId == null)
      jcasType.jcas.throwFeatMissing("parentRelationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseConnective");
    return jcasType.ll_cas.ll_getIntValue(addr, ((DiscourseConnective_Type)jcasType).casFeatCode_parentRelationId);}
    
  /** setter for parentRelationId - sets ID of the parent relation 
   * @generated */
  public void setParentRelationId(int v) {
    if (DiscourseConnective_Type.featOkTst && ((DiscourseConnective_Type)jcasType).casFeat_parentRelationId == null)
      jcasType.jcas.throwFeatMissing("parentRelationId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseConnective");
    jcasType.ll_cas.ll_setIntValue(addr, ((DiscourseConnective_Type)jcasType).casFeatCode_parentRelationId, v);}    
  }

    