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

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.charts.model.DataSeries;

/**
 * Converter from database query results into a {@link DataSeries} suitable for
 * rendering in a chart. The input format matches the result of
 * {@link DatabaseProvider#executeQuery(String)}, where each row is a
 * column-name-to-value map.
 *
 * @author Vaadin Ltd
 */
public interface DataConverter extends Serializable {

    /**
     * Converts database query results into a {@link DataSeries}.
     * <p>
     * Each element in the input list represents a single row returned by the
     * database, with column names as keys and column values as values.
     * </p>
     *
     * @param data
     *            the query results to convert, not {@code null}
     * @return a data series ready for use in a chart, never {@code null}
     * @throws NullPointerException
     *             if data is {@code null}
     */
    DataSeries convertToDataSeries(List<Map<String, Object>> data);
}
