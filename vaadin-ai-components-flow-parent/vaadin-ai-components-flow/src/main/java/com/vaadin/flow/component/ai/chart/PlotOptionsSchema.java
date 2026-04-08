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

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Provides pre-generated JSON schemas for {@link AbstractPlotOptions}
 * subclasses. Schemas are generated at build time by
 * {@link PlotOptionsSchemaGenerator} and loaded from a classpath resource.
 *
 * @author Vaadin Ltd
 */
final class PlotOptionsSchema implements Serializable {

    private static final String SCHEMAS_RESOURCE = "plot-options-schemas.json";

    private static final Map<String, String> SCHEMAS;
    private static final Map<String, Class<? extends AbstractPlotOptions>> PLOT_OPTIONS_BY_TYPE;

    static {
        PLOT_OPTIONS_BY_TYPE = PlotOptionsSchemaGenerator.buildTypeMap();
        SCHEMAS = loadSchemas();
    }

    private PlotOptionsSchema() {
    }

    /**
     * Returns the pre-generated JSON schema for the plot options of the given
     * chart type.
     *
     * @param chartType
     *            chart type name (case-insensitive), e.g. "column", "series"
     * @return JSON schema string, or {@code null} if the type is unknown
     */
    static String getSchema(String chartType) {
        return SCHEMAS.get(chartType.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Returns the set of supported chart type names.
     */
    static Set<String> supportedTypes() {
        return PLOT_OPTIONS_BY_TYPE.keySet();
    }

    /**
     * Returns the PlotOptions class for the given chart type name, or
     * {@code null} if the type is unknown.
     */
    static Class<? extends AbstractPlotOptions> getPlotOptionsClass(
            String chartType) {
        return PLOT_OPTIONS_BY_TYPE.get(chartType.toLowerCase(Locale.ENGLISH));
    }

    private static Map<String, String> loadSchemas() {
        try (var input = PlotOptionsSchema.class
                .getResourceAsStream(SCHEMAS_RESOURCE)) {
            if (input == null) {
                return Map.of();
            }
            JsonNode root = JacksonUtils.getMapper().readTree(input);
            var result = new HashMap<String, String>();
            var entries = root.properties().iterator();
            while (entries.hasNext()) {
                var entry = entries.next();
                result.put(entry.getKey(), entry.getValue().toString());
            }
            return Map.copyOf(result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
