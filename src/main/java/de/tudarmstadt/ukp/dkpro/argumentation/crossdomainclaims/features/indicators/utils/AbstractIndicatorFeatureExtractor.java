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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.indicators.utils;

import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import java.util.Map;

public class AbstractIndicatorFeatureExtractor extends FeatureExtractorResource_ImplBase {

	public static final String PARAM_BWD_LIST_PATH = "bwdIndicatorListPath";
    @ConfigurationParameter(name = PARAM_BWD_LIST_PATH, mandatory = true, defaultValue = "indicators/BackwardIndicators.txt")
    private String bwdIndicatorListPath;

    public static final String PARAM_FWD_LIST_PATH = "fwdIndicatorListPath";
    @ConfigurationParameter(name = PARAM_FWD_LIST_PATH, mandatory = true, defaultValue = "indicators/ForwardIndicators.txt")
    private String fwdIndicatorListPath;
    
    public static final String PARAM_THESIS_LIST_PATH = "thesisIndicatorListPath";
    @ConfigurationParameter(name = PARAM_THESIS_LIST_PATH, mandatory = true, defaultValue = "indicators/ThesisIndicators.txt")
    private String thesisIndicatorListPath;
    
    public static final String PARAM_REBUTTAL_LIST_PATH = "rebuttalIndicatorListPath";
    @ConfigurationParameter(name = PARAM_REBUTTAL_LIST_PATH, mandatory = true, defaultValue = "indicators/RebuttalIndicators.txt")
    private String rebuttalIndicatorListPath;
    
    protected IndicatorUtils indicatorUtils = null;
    
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
    	 if (!super.initialize(aSpecifier, aAdditionalParams)) {
             return false;
         }
    	 
    	 init();
    	 return true;
    }
    
    public void init() {
    	indicatorUtils = IndicatorUtils.getInstance(fwdIndicatorListPath, bwdIndicatorListPath, rebuttalIndicatorListPath, thesisIndicatorListPath);
    }
    
}
