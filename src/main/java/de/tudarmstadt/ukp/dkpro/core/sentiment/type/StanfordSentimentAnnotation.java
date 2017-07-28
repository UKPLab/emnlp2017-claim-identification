

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

/* First created by JCasGen Mon Nov 17 11:07:48 CET 2014 */
package de.tudarmstadt.ukp.dkpro.core.sentiment.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** Stanford CoreNLP Sentiment annotation
 * Updated by JCasGen Mon Nov 17 11:07:48 CET 2014
 * XML source: /home/svc_jenkins/workspace/DKPro Core ASL 1.7.x  (Google Code)/de.tudarmstadt.ukp.dkpro.core.api.sentiment-asl/target/jcasgen/typesystem.xml
 * @generated */
public class StanfordSentimentAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(StanfordSentimentAnnotation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected StanfordSentimentAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public StanfordSentimentAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public StanfordSentimentAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public StanfordSentimentAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: veryNegative

  /** getter for veryNegative - gets Value of veryNegative
   * @generated
   * @return value of the feature 
   */
  public double getVeryNegative() {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_veryNegative == null)
      jcasType.jcas.throwFeatMissing("veryNegative", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_veryNegative);}
    
  /** setter for veryNegative - sets Value of veryNegative 
   * @generated
   * @param v value to set into the feature 
   */
  public void setVeryNegative(double v) {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_veryNegative == null)
      jcasType.jcas.throwFeatMissing("veryNegative", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_veryNegative, v);}    
   
    
  //*--------------*
  //* Feature: negative

  /** getter for negative - gets Value of negative
   * @generated
   * @return value of the feature 
   */
  public double getNegative() {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_negative == null)
      jcasType.jcas.throwFeatMissing("negative", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_negative);}
    
  /** setter for negative - sets Value of negative 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNegative(double v) {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_negative == null)
      jcasType.jcas.throwFeatMissing("negative", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_negative, v);}    
   
    
  //*--------------*
  //* Feature: neutral

  /** getter for neutral - gets Value of neutral
   * @generated
   * @return value of the feature 
   */
  public double getNeutral() {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_neutral == null)
      jcasType.jcas.throwFeatMissing("neutral", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_neutral);}
    
  /** setter for neutral - sets Value of neutral 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNeutral(double v) {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_neutral == null)
      jcasType.jcas.throwFeatMissing("neutral", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_neutral, v);}    
   
    
  //*--------------*
  //* Feature: positive

  /** getter for positive - gets Value of positive
   * @generated
   * @return value of the feature 
   */
  public double getPositive() {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_positive == null)
      jcasType.jcas.throwFeatMissing("positive", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_positive);}
    
  /** setter for positive - sets Value of positive 
   * @generated
   * @param v value to set into the feature 
   */
  public void setPositive(double v) {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_positive == null)
      jcasType.jcas.throwFeatMissing("positive", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_positive, v);}    
   
    
  //*--------------*
  //* Feature: veryPositive

  /** getter for veryPositive - gets Value of veryPositive
   * @generated
   * @return value of the feature 
   */
  public double getVeryPositive() {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_veryPositive == null)
      jcasType.jcas.throwFeatMissing("veryPositive", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_veryPositive);}
    
  /** setter for veryPositive - sets Value of veryPositive 
   * @generated
   * @param v value to set into the feature 
   */
  public void setVeryPositive(double v) {
    if (StanfordSentimentAnnotation_Type.featOkTst && ((StanfordSentimentAnnotation_Type)jcasType).casFeat_veryPositive == null)
      jcasType.jcas.throwFeatMissing("veryPositive", "de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((StanfordSentimentAnnotation_Type)jcasType).casFeatCode_veryPositive, v);}    
  }

    