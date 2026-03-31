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
import java.util.Objects;

import com.vaadin.flow.component.charts.model.Configuration;

/**
 * Serializable chart state for persistence across sessions. Captured via
 * {@link ChartAIController#getState()} and restored via
 * {@link ChartAIController#restoreState(ChartState)}.
 *
 * @param queries
 *            the SQL queries for the chart's data series
 * @param configuration
 *            the chart configuration
 * @author Vaadin Ltd
 */
public record ChartState(List<String> queries,
        Configuration configuration) implements Serializable {
    /**
     * Creates a new state instance.
     *
     * @param queries
     *            the SQL queries, not {@code null}
     * @param configuration
     *            the chart configuration, not {@code null}
     */
    public ChartState {
        queries = List.copyOf(queries);
        Objects.requireNonNull(configuration, "Configuration cannot be null");
    }
}
