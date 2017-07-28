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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.results;

import org.junit.Test;

/**
 * @author Ivan Habernal
 */
public class TableReportCreatorTest
{
    @Test
    public void loadMatrixFromCSVFile()
            throws Exception
    {
        System.out.println(new TableReportCreator().loadMatrixFromCSVFile(
                getClass().getClassLoader().getResourceAsStream("tables/cm1.csv")));
        System.out.println(new TableReportCreator().loadMatrixFromCSVFile(
                getClass().getClassLoader().getResourceAsStream("tables/cm2.csv")));

    }

}