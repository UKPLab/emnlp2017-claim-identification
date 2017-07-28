

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


/** Attribution annotation (see PTDB for details); not connected
				to any particular relation as it may belong to two relations thus is covered
				by DiscourseRelation
 * Updated by JCasGen Wed Mar 23 19:38:34 CET 2016
 * XML source: /Users/johannesdaxenberger/workspaces/experiments/de.tudarmstadt.ukp.dkpro.core.api.discourse-asl/target/jcasgen/typesystem.xml
 * @generated */
public class DiscourseAttribution extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DiscourseAttribution.class);
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
  protected DiscourseAttribution() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DiscourseAttribution(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DiscourseAttribution(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DiscourseAttribution(JCas jcas, int begin, int end) {
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
  //* Feature: attributeId

  /** getter for attributeId - gets 
   * @generated */
  public int getAttributeId() {
    if (DiscourseAttribution_Type.featOkTst && ((DiscourseAttribution_Type)jcasType).casFeat_attributeId == null)
      jcasType.jcas.throwFeatMissing("attributeId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseAttribution");
    return jcasType.ll_cas.ll_getIntValue(addr, ((DiscourseAttribution_Type)jcasType).casFeatCode_attributeId);}
    
  /** setter for attributeId - sets  
   * @generated */
  public void setAttributeId(int v) {
    if (DiscourseAttribution_Type.featOkTst && ((DiscourseAttribution_Type)jcasType).casFeat_attributeId == null)
      jcasType.jcas.throwFeatMissing("attributeId", "de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.DiscourseAttribution");
    jcasType.ll_cas.ll_setIntValue(addr, ((DiscourseAttribution_Type)jcasType).casFeatCode_attributeId, v);}    
  }

    