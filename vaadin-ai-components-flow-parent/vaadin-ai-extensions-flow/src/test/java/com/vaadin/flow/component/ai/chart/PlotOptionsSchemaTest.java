/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.chart;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Verifies that the plot options schemas generated at build time by
 * {@link PlotOptionsSchemaGenerator} end up on the classpath and are usable
 * through {@link PlotOptionsSchema}.
 */
class PlotOptionsSchemaTest {

    private static final String RESOURCE = "plot-options-schemas.json";

    @Test
    void schemaResource_isOnClasspath() {
        Assertions.assertNotNull(PlotOptionsSchema.class.getResource(RESOURCE),
                RESOURCE + " is missing next to PlotOptionsSchema. It is "
                        + "generated into the build output directory by the "
                        + "exec-maven-plugin execution in this module's pom, "
                        + "so the module must be built with Maven.");
    }

    @Test
    void everySupportedType_hasSchema() {
        var types = PlotOptionsSchema.supportedTypes();
        Assertions.assertFalse(types.isEmpty());
        for (String type : types) {
            String schema = PlotOptionsSchema.getSchema(type);
            Assertions.assertNotNull(schema,
                    "No generated schema for chart type '" + type + "'");
            JsonNode node = JacksonUtils.readTree(schema);
            Assertions.assertEquals("object", node.get("type").asString());
            Assertions.assertFalse(node.get("properties").isEmpty(),
                    "Schema for '" + type + "' has no properties");
        }
    }

    @Test
    void everySchema_hasDescriptionsFromChartSources() {
        for (String type : PlotOptionsSchema.supportedTypes()) {
            JsonNode properties = JacksonUtils
                    .readTree(PlotOptionsSchema.getSchema(type))
                    .get("properties");
            boolean described = false;
            for (var property : properties.properties()) {
                if (property.getValue().has("description")) {
                    described = true;
                    break;
                }
            }
            Assertions.assertTrue(described,
                    "Schema for '" + type
                            + "' has no property descriptions. The generator "
                            + "could not read the chart model sources.");
        }
    }

    @Test
    void unknownType_returnsNull() {
        Assertions.assertNull(PlotOptionsSchema.getSchema("nonexistent"));
        Assertions.assertNull(
                PlotOptionsSchema.getPlotOptionsClass("nonexistent"));
    }

    @Test
    void generator_missingSourceDirectory_fails(@TempDir Path tempDir) {
        var missingSources = tempDir.resolve("missing").toString();
        var output = tempDir.resolve("out.json").toString();
        Assertions.assertThrows(IllegalStateException.class,
                () -> PlotOptionsSchemaGenerator
                        .main(new String[] { missingSources, output }));
    }

    @Test
    void generator_sourceDirectoryWithoutChartModel_fails(
            @TempDir Path tempDir) {
        var output = tempDir.resolve("out.json").toString();
        Assertions.assertThrows(IllegalStateException.class,
                () -> PlotOptionsSchemaGenerator
                        .main(new String[] { tempDir.toString(), output }));
    }
}
