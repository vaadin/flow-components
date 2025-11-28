/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.ai.state.AiStateSupport;
import com.vaadin.flow.component.charts.Chart;

/**
 * Helper that captures and restores ChartState using AiStateSupport.
 * <p>
 * This class provides a simple way to capture and restore the state of a chart
 * component for persistence or session management.
 * </p>
 * <p>
 * <strong>Note:</strong> The current implementation captures a reference to the
 * Configuration object. For proper serialization and persistence across sessions,
 * a more sophisticated serialization strategy would be needed that handles the
 * complex object graph of Chart configurations.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * Chart chart = new Chart();
 * ChartStateSupport stateSupport = new ChartStateSupport(chart);
 *
 * // Capture current state (in-memory snapshot)
 * ChartState snapshot = stateSupport.capture();
 *
 * // Later: restore from saved state
 * stateSupport.restore(snapshot);
 * </pre>
 *
 * @author Vaadin Ltd
 */
public final class ChartStateSupport implements AiStateSupport<ChartState> {

    private final Chart chart;

    /**
     * Creates a new chart state support instance.
     *
     * @param chart
     *            the chart to manage state for
     */
    public ChartStateSupport(Chart chart) {
        this.chart = chart;
    }

    @Override
    public ChartState capture() {
        var config = chart.getConfiguration();

        // Create a simplified JSON representation of the chart configuration
        // that captures the essential visual state without circular references
        String configJson = serializeConfiguration(config);

        return new ChartState(configJson);
    }

    @Override
    public void restore(ChartState state) {
        // Parse and restore the basic configuration from JSON
        try {
            deserializeConfiguration(state.getConfigurationJson());
            chart.drawChart();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to restore chart state: " + e.getMessage(), e
            );
        }
    }

    private String serializeConfiguration(com.vaadin.flow.component.charts.model.Configuration config) {
        // Create a minimal JSON representation of key chart properties
        // This avoids the circular reference issues with full serialization
        StringBuilder json = new StringBuilder("{");
        boolean hasContent = false;

        // Capture chart type
        if (config.getChart() != null && config.getChart().getType() != null) {
            json.append("\"type\":\"").append(config.getChart().getType().toString()).append("\"");
            hasContent = true;
        }

        // Capture title
        if (config.getTitle() != null && config.getTitle().getText() != null) {
            if (hasContent) json.append(",");
            json.append("\"title\":\"").append(escapeJson(config.getTitle().getText())).append("\"");
            hasContent = true;
        }

        // Note: Subtitle getter not available in Configuration API
        // Only setter is available, so we skip subtitle in serialization

        json.append("}");
        return json.toString();
    }

    private void deserializeConfiguration(String configJson) {
        // Parse the JSON and restore basic configuration
        // This is a simplified implementation that handles the essential properties
        if (configJson == null || configJson.trim().isEmpty() || configJson.equals("{}")) {
            return;
        }

        var config = chart.getConfiguration();

        // Simple JSON parsing for our limited properties
        if (configJson.contains("\"type\":")) {
            String type = extractJsonValue(configJson, "type");
            if (type != null && !type.isEmpty()) {
                var chartType = parseChartType(type);
                if (chartType != null) {
                    config.getChart().setType(chartType);
                }
            }
        }

        if (configJson.contains("\"title\":")) {
            String title = extractJsonValue(configJson, "title");
            if (title != null) {
                config.setTitle(title);
            }
        }
    }

    private com.vaadin.flow.component.charts.model.ChartType parseChartType(String typeString) {
        // Map string representation to ChartType constant
        return switch (typeString.toLowerCase()) {
            case "line" -> com.vaadin.flow.component.charts.model.ChartType.LINE;
            case "spline" -> com.vaadin.flow.component.charts.model.ChartType.SPLINE;
            case "area" -> com.vaadin.flow.component.charts.model.ChartType.AREA;
            case "areaspline" -> com.vaadin.flow.component.charts.model.ChartType.AREASPLINE;
            case "column" -> com.vaadin.flow.component.charts.model.ChartType.COLUMN;
            case "bar" -> com.vaadin.flow.component.charts.model.ChartType.BAR;
            case "pie" -> com.vaadin.flow.component.charts.model.ChartType.PIE;
            case "scatter" -> com.vaadin.flow.component.charts.model.ChartType.SCATTER;
            default -> null;
        };
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return null;
        }
        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return null;
        }
        return unescapeJson(json.substring(startIndex, endIndex));
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String unescapeJson(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("\\\"", "\"")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t")
                   .replace("\\\\", "\\");
    }
}
