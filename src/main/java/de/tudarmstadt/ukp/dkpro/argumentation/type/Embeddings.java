

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

/* First created by JCasGen Fri Feb 26 16:21:51 CET 2016 */
package de.tudarmstadt.ukp.dkpro.argumentation.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Feb 26 16:21:51 CET 2016
 * XML source: /Users/johannesdaxenberger/workspaces/experiments/de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims/src/main/resources/desc/types/EmbeddingsAnnotation.xml
 * @generated */
public class Embeddings extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Embeddings.class);
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
  protected Embeddings() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Embeddings(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Embeddings(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Embeddings(JCas jcas, int begin, int end) {
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
  //* Feature: vector

  /** getter for vector - gets Vector in embeddings space
   * @generated
   * @return value of the feature 
   */
  public DoubleArray getVector() {
    if (Embeddings_Type.featOkTst && ((Embeddings_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "de.tudarmstadt.ukp.dkpro.argumentation.type.Embeddings");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Embeddings_Type)jcasType).casFeatCode_vector)));}
    
  /** setter for vector - sets Vector in embeddings space 
   * @generated
   * @param v value to set into the feature 
   */
  public void setVector(DoubleArray v) {
    if (Embeddings_Type.featOkTst && ((Embeddings_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "de.tudarmstadt.ukp.dkpro.argumentation.type.Embeddings");
    jcasType.ll_cas.ll_setRefValue(addr, ((Embeddings_Type)jcasType).casFeatCode_vector, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for vector - gets an indexed value - Vector in embeddings space
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public double getVector(int i) {
    if (Embeddings_Type.featOkTst && ((Embeddings_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "de.tudarmstadt.ukp.dkpro.argumentation.type.Embeddings");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Embeddings_Type)jcasType).casFeatCode_vector), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Embeddings_Type)jcasType).casFeatCode_vector), i);}

  /** indexed setter for vector - sets an indexed value - Vector in embeddings space
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setVector(int i, double v) { 
    if (Embeddings_Type.featOkTst && ((Embeddings_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "de.tudarmstadt.ukp.dkpro.argumentation.type.Embeddings");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Embeddings_Type)jcasType).casFeatCode_vector), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Embeddings_Type)jcasType).casFeatCode_vector), i, v);}
  }

    