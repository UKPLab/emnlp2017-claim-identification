

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



/** Discourse relation
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * XML source: /Users/johannesdaxenberger/workspaces/experiments/de.tudarmstadt.ukp.dkpro.core.api.discourse-asl/target/jcasgen/typesystem.xml
 * @generated */
public class ExplicitDiscourseRelation extends DiscourseRelation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ExplicitDiscourseRelation.class);
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
  protected ExplicitDiscourseRelation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ExplicitDiscourseRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ExplicitDiscourseRelation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ExplicitDiscourseRelation(JCas jcas, int begin, int end) {
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
  //* Feature: discourseConnective1

  /** getter for discourseConnective1 - gets Discourse connective (in case of explicit relations)
   * @generated */
  public DiscourseConnective getDiscourseConnective1() {
    if (ExplicitDiscourseRelation_Type.featOkTst && ((ExplicitDiscourseRelation_Type)jcasType).casFeat_discourseConnective1 == null)
      jcasType.jcas.throwFeatMissing("discourseConnective1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
    return (DiscourseConnective)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ExplicitDiscourseRelation_Type)jcasType).casFeatCode_discourseConnective1)));}
    
  /** setter for discourseConnective1 - sets Discourse connective (in case of explicit relations) 
   * @generated */
  public void setDiscourseConnective1(DiscourseConnective v) {
    if (ExplicitDiscourseRelation_Type.featOkTst && ((ExplicitDiscourseRelation_Type)jcasType).casFeat_discourseConnective1 == null)
      jcasType.jcas.throwFeatMissing("discourseConnective1", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((ExplicitDiscourseRelation_Type)jcasType).casFeatCode_discourseConnective1, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: discourseConnective2

  /** getter for discourseConnective2 - gets Discourse connective (in case of explicit relations)
   * @generated */
  public DiscourseConnective getDiscourseConnective2() {
    if (ExplicitDiscourseRelation_Type.featOkTst && ((ExplicitDiscourseRelation_Type)jcasType).casFeat_discourseConnective2 == null)
      jcasType.jcas.throwFeatMissing("discourseConnective2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
    return (DiscourseConnective)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ExplicitDiscourseRelation_Type)jcasType).casFeatCode_discourseConnective2)));}
    
  /** setter for discourseConnective2 - sets Discourse connective (in case of explicit relations) 
   * @generated */
  public void setDiscourseConnective2(DiscourseConnective v) {
    if (ExplicitDiscourseRelation_Type.featOkTst && ((ExplicitDiscourseRelation_Type)jcasType).casFeat_discourseConnective2 == null)
      jcasType.jcas.throwFeatMissing("discourseConnective2", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.ExplicitDiscourseRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((ExplicitDiscourseRelation_Type)jcasType).casFeatCode_discourseConnective2, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    