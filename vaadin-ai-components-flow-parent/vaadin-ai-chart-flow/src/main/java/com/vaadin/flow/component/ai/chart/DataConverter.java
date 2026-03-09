/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.charts.model.DataSeries;

/**
 * Interface for converting database query results into Vaadin Charts DataSeries
 * objects.
 * <p>
 * Implementations of this interface handle the transformation of tabular
 * database results into chart-appropriate data structures.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface DataConverter extends Serializable {

    /**
     * Converts database query results into a DataSeries for use in Vaadin
     * Charts.
     *
     * @param queryResults
     *            the database query results, where each map represents a row
     *            with column names as keys
     * @return a DataSeries containing the converted data
     * @throws IllegalArgumentException
     *             if the query results cannot be converted
     */
    DataSeries convertToDataSeries(List<Map<String, Object>> queryResults);
}
