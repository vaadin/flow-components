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

import com.vaadin.flow.component.charts.model.Series;

/**
 * Converts tabular data into chart {@link Series} for rendering. The input is a
 * list of rows, where each row is a column-name-to-value map.
 * <p>
 * Implementations may return one or more series depending on the data. The
 * {@link DefaultDataConverter} provides automatic chart type detection based on
 * column names and supports multi-series grouping via a {@code series} column.
 * Custom implementations can apply any conversion logic appropriate for their
 * use case.
 * </p>
 *
 * @author Vaadin Ltd
 * @see DefaultDataConverter
 */
public interface DataConverter extends Serializable {

    /**
     * Converts tabular data into one or more chart series.
     * <p>
     * Each element in the input list represents a single row, with column names
     * as keys and column values as values.
     * </p>
     *
     * @param data
     *            the tabular data to convert, not {@code null}
     * @return a list of series ready for use in a chart, never {@code null} and
     *         never empty
     * @throws NullPointerException
     *             if data is {@code null}
     */
    List<Series> convertToSeries(List<Map<String, Object>> data);
}
