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

import static com.vaadin.flow.component.charts.util.ChartSerialization.toJSON;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.ColorAxis;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Credits;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.Dimension;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.Marker;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.style.SolidColor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Tests that serialized Chart {@link Configuration} objects align with the JSON
 * Schema defined in the {@code update_chart_configuration} tool's
 * {@code getParametersSchema()}.
 * <p>
 * Uses the <a href="https://github.com/networknt/json-schema-validator">
 * networknt json-schema-validator</a> library to perform proper JSON Schema
 * validation (draft v4).
 */
class ChartAIToolsSchemaTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Schema schema;
    private static JsonNode configSchemaNode;

    /**
     * Properties serialized by {@link Configuration} that are intentionally
     * excluded from the tool schema because the LLM should not control them.
     * These are either internal framework fields or managed separately.
     */
    private static final Set<String> EXCLUDED_PROPERTIES = Set.of(
            "configuration.exporting", "configuration.series",
            "configuration.chart.styledMode");

    /** Internal index fields added by the serialization framework. */
    private static final Set<String> INTERNAL_FIELD_NAMES = Set.of("axisIndex",
            "paneIndex");

    @BeforeAll
    static void parseSchema() throws Exception {
        var callbacks = new NoOpCallbacks();
        LLMProvider.ToolSpec tool = ChartAITools
                .updateChartConfiguration(callbacks);

        JsonNode schemaNode = MAPPER.readTree(tool.getParametersSchema());
        // Build the schema for the "configuration" property only, since
        // ChartSerialization.toJSON serializes the Configuration object
        // directly (without the wrapping "configuration" key).
        configSchemaNode = schemaNode.get("properties").get("configuration");

        SchemaRegistry registry = SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_4);
        schema = registry.getSchema(configSchemaNode);
    }

    @Test
    void lineChart_withTitle() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.LINE);
        config.setTitle("Monthly Sales");

        assertMatchesSchema(config);
    }

    @Test
    void barChart_withAxes() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.BAR);
        config.setTitle("Sales by Region");
        config.getxAxis().setType(AxisType.CATEGORY);
        config.getxAxis().setCategories("North", "South", "East", "West");
        config.getyAxis().setTitle("Revenue");
        config.getyAxis().setMin(0);

        assertMatchesSchema(config);
    }

    @Test
    void pieChart_withPlotOptions() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.PIE);
        config.setTitle("Market Share");

        PlotOptionsPie piePlotOptions = new PlotOptionsPie();
        piePlotOptions.setInnerSize("50%");
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        dataLabels.setFormat("{point.name}: {point.percentage:.1f}%");
        piePlotOptions.setDataLabels(dataLabels);
        config.setPlotOptions(piePlotOptions);

        assertMatchesSchema(config);
    }

    @Test
    void columnChart_withStackingAndDataLabels() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.COLUMN);
        config.setTitle("Stacked Columns");

        PlotOptionsColumn columnPlotOptions = new PlotOptionsColumn();
        columnPlotOptions.setStacking(Stacking.NORMAL);
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        dataLabels.setFormat("{point.y:.0f}");
        columnPlotOptions.setDataLabels(dataLabels);
        columnPlotOptions.setBorderRadius(5);
        config.setPlotOptions(columnPlotOptions);

        assertMatchesSchema(config);
    }

    @Test
    void chart_withTooltip() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.LINE);

        Tooltip tooltip = new Tooltip();
        tooltip.setShared(true);
        tooltip.setPointFormat("{series.name}: <b>{point.y}</b>");
        tooltip.setHeaderFormat("<b>{point.key}</b><br/>");
        tooltip.setValueSuffix(" units");
        tooltip.setValuePrefix("$");
        config.setTooltip(tooltip);

        assertMatchesSchema(config);
    }

    @Test
    void chart_withLegend() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.LINE);

        Legend legend = new Legend();
        legend.setEnabled(true);
        legend.setAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.MIDDLE);
        legend.setLayout(LayoutDirection.VERTICAL);
        config.setLegend(legend);

        assertMatchesSchema(config);
    }

    @Test
    void chart_withCredits() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.LINE);

        Credits credits = new Credits(true);
        credits.setText("Data Source: Example");
        credits.setHref("https://example.com");
        config.setCredits(credits);

        assertMatchesSchema(config);
    }

    @Test
    void chart_withSubtitle() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.AREA);
        config.setTitle("Revenue");
        config.setSubTitle("2024 Fiscal Year");

        assertMatchesSchema(config);
    }

    @Test
    void gaugeChart_withPane() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.GAUGE);
        config.getChart().setPolar(true);

        Pane pane = new Pane();
        pane.setStartAngle(-150);
        pane.setEndAngle(150);
        pane.setCenter("50%", "50%");
        pane.setSize("100%");
        config.addPane(pane);

        assertMatchesSchema(config);
    }

    @Test
    void chart_withSeriesPlotOptions() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.LINE);

        PlotOptionsSeries seriesPlotOptions = new PlotOptionsSeries();
        seriesPlotOptions.setStacking(Stacking.PERCENT);
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        dataLabels.setFormat("{point.y:.0f}");
        seriesPlotOptions.setDataLabels(dataLabels);
        Marker marker = new Marker();
        marker.setEnabled(false);
        seriesPlotOptions.setMarker(marker);
        config.setPlotOptions(seriesPlotOptions);

        assertMatchesSchema(config);
    }

    @Test
    void heatmapChart_withColorAxis() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.HEATMAP);
        config.setTitle("Activity Heatmap");

        ColorAxis colorAxis = new ColorAxis();
        colorAxis.setMin(0);
        colorAxis.setMax(100);
        colorAxis.setMinColor(new SolidColor("#ffffff"));
        colorAxis.setMaxColor(new SolidColor("#003399"));
        config.addColorAxis(colorAxis);

        assertMatchesSchema(config);
    }

    @SuppressWarnings("deprecation")
    @Test
    void chart_withChartModelProperties() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.SCATTER);
        config.getChart().setBackgroundColor(new SolidColor("#f0f0f0"));
        config.getChart().setBorderColor(new SolidColor("#cccccc"));
        config.getChart().setBorderWidth(1);
        config.getChart().setBorderRadius(4);
        config.getChart().setWidth(800);
        config.getChart().setHeight("400");
        config.getChart().setInverted(false);
        config.getChart().setAnimation(true);
        config.getChart().setZoomType(Dimension.XY);
        config.getChart().setMarginTop(50);
        config.getChart().setMarginRight(20);
        config.getChart().setMarginBottom(60);
        config.getChart().setMarginLeft(80);
        config.getChart().setSpacingTop(10);
        config.getChart().setSpacingRight(10);
        config.getChart().setSpacingBottom(15);
        config.getChart().setSpacingLeft(10);
        config.getChart().setPlotBackgroundColor(new SolidColor("#fafafa"));
        config.getChart().setPlotBorderColor(new SolidColor("#dddddd"));
        config.getChart().setPlotBorderWidth(1);

        assertMatchesSchema(config);
    }

    @Test
    void chart_withXAxisCategories() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.COLUMN);
        config.getxAxis().setCategories("Jan", "Feb", "Mar", "Apr", "May");
        config.getxAxis().setTitle("Month");
        config.getxAxis().setMin(0);
        config.getxAxis().setMax(4);

        assertMatchesSchema(config);
    }

    @Test
    void chart_withYAxisLogarithmic() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.LINE);
        config.getyAxis().setType(AxisType.LOGARITHMIC);
        config.getyAxis().setTitle("Values (log scale)");
        config.getyAxis().setMin(1);
        config.getyAxis().setMax(10000);

        assertMatchesSchema(config);
    }

    @Test
    void barChart_withPlotOptionsAndDataLabels() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.BAR);

        PlotOptionsBar barPlotOptions = new PlotOptionsBar();
        barPlotOptions.setStacking(Stacking.PERCENT);
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        dataLabels.setFormat("{point.y:.1f}%");
        barPlotOptions.setDataLabels(dataLabels);
        barPlotOptions.setBorderRadius(3);
        config.setPlotOptions(barPlotOptions);

        assertMatchesSchema(config);
    }

    @Test
    void fullConfig_multipleSchemaProperties() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.COLUMN);
        config.getChart().setAnimation(true);
        config.setTitle("Full Configuration Test");
        config.setSubTitle("Testing all schema properties");
        config.getxAxis().setType(AxisType.CATEGORY);
        config.getxAxis().setCategories("A", "B", "C");
        config.getyAxis().setTitle("Values");
        config.getyAxis().setMin(0);

        Tooltip tooltip = new Tooltip();
        tooltip.setShared(true);
        tooltip.setValueSuffix(" units");
        config.setTooltip(tooltip);

        Legend legend = new Legend();
        legend.setEnabled(true);
        legend.setLayout(LayoutDirection.HORIZONTAL);
        config.setLegend(legend);

        Credits credits = new Credits(false);
        config.setCredits(credits);

        PlotOptionsColumn columnOpts = new PlotOptionsColumn();
        columnOpts.setStacking(Stacking.NORMAL);
        config.setPlotOptions(columnOpts);

        assertMatchesSchema(config);
    }

    @Test
    void bubbleChart_withZAxis() {
        Configuration config = new Configuration();
        config.getChart().setType(ChartType.BUBBLE);
        config.setTitle("Bubble Chart");
        config.getxAxis().setTitle("GDP");
        config.getyAxis().setTitle("Life Expectancy");
        config.getzAxis().setType(AxisType.LINEAR);
        config.getzAxis().setTitle("Population");
        config.getzAxis().setMin(0);
        config.getzAxis().setMax(1000000);

        assertMatchesSchema(config);
    }

    @Test
    void allChartTypes_matchSchema() {
        var allTypes = List.of(ChartType.LINE, ChartType.SPLINE, ChartType.AREA,
                ChartType.AREASPLINE, ChartType.BAR, ChartType.COLUMN,
                ChartType.PIE, ChartType.SCATTER, ChartType.GAUGE,
                ChartType.AREARANGE, ChartType.COLUMNRANGE,
                ChartType.AREASPLINERANGE, ChartType.BOXPLOT,
                ChartType.ERRORBAR, ChartType.BUBBLE, ChartType.FUNNEL,
                ChartType.WATERFALL, ChartType.PYRAMID, ChartType.SOLIDGAUGE,
                ChartType.HEATMAP, ChartType.TREEMAP, ChartType.POLYGON,
                ChartType.CANDLESTICK, ChartType.FLAGS, ChartType.TIMELINE,
                ChartType.OHLC, ChartType.ORGANIZATION, ChartType.SANKEY,
                ChartType.XRANGE, ChartType.GANTT, ChartType.BULLET);

        for (ChartType type : allTypes) {
            Configuration config = new Configuration();
            config.getChart().setType(type);
            config.setTitle(type.toString() + " chart");
            assertMatchesSchema(config);
        }
    }

    /**
     * Serializes the given configuration using
     * {@link com.vaadin.flow.component.charts.util.ChartSerialization#toJSON}
     * and validates the result against the JSON Schema. Also verifies that
     * every property in the serialized JSON is defined in the schema (i.e., the
     * schema fully covers the serialized output).
     */
    private static void assertMatchesSchema(Configuration config) {
        try {
            String json = toJSON(config);
            JsonNode configNode = MAPPER.readTree(json);

            // 1. Validate serialized JSON conforms to schema types/enums
            List<Error> errors = schema.validate(configNode);
            if (!errors.isEmpty()) {
                String details = errors.stream().map(Error::getMessage)
                        .collect(Collectors.joining("\n  - ", "\n  - ", ""));
                throw new AssertionError(
                        "Serialized configuration does not match schema:"
                                + details + "\n\nSerialized JSON:\n" + json);
            }

            // 2. Verify every property in the JSON is defined in the schema
            List<String> uncovered = new ArrayList<>();
            assertAllPropertiesCovered(configNode, configSchemaNode,
                    "configuration", uncovered);
            if (!uncovered.isEmpty()) {
                throw new AssertionError(
                        "Properties in serialized JSON not defined in schema:\n  - "
                                + String.join("\n  - ", uncovered)
                                + "\n\nSerialized JSON:\n" + json);
            }
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(
                    "Failed to validate configuration against schema", e);
        }
    }

    /**
     * Recursively walks the JSON node and checks that every object property at
     * each level is declared in the corresponding schema's "properties".
     * Handles "oneOf" by checking if at least one alternative covers all
     * properties.
     */
    private static void assertAllPropertiesCovered(JsonNode jsonNode,
            JsonNode schemaNode, String path, List<String> uncovered) {
        if (jsonNode == null || schemaNode == null) {
            return;
        }

        // Handle oneOf: at least one alternative must cover all properties
        if (schemaNode.has("oneOf")) {
            for (JsonNode option : schemaNode.get("oneOf")) {
                List<String> optionUncovered = new ArrayList<>();
                assertAllPropertiesCovered(jsonNode, option, path,
                        optionUncovered);
                if (optionUncovered.isEmpty()) {
                    return;
                }
            }
            uncovered
                    .add(path + ": no oneOf alternative covers all properties");
            return;
        }

        if (jsonNode.isObject() && schemaNode.has("properties")) {
            JsonNode schemaProperties = schemaNode.get("properties");
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.properties()
                    .iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                String fieldPath = path + "." + fieldName;

                if (!schemaProperties.has(fieldName)) {
                    if (!EXCLUDED_PROPERTIES.contains(fieldPath)
                            && !INTERNAL_FIELD_NAMES.contains(fieldName)) {
                        uncovered.add(fieldPath);
                    }
                    continue;
                }

                assertAllPropertiesCovered(field.getValue(),
                        schemaProperties.get(fieldName), fieldPath, uncovered);
            }
        }

        if (jsonNode.isArray() && schemaNode.has("items")) {
            int index = 0;
            for (JsonNode item : jsonNode) {
                assertAllPropertiesCovered(item, schemaNode.get("items"),
                        path + "[" + index + "]", uncovered);
                index++;
            }
        }
    }

    private static class NoOpCallbacks implements ChartAITools.Callbacks {
        @Override
        public String getState(String chartId) {
            return "{}";
        }

        @Override
        public void updateConfiguration(String chartId, String configJson) {
            // No-op
        }

        @Override
        public void updateData(String chartId, List<String> queries) {
            // No-op
        }

        @Override
        public Set<String> getChartIds() {
            return Set.of("chart-1");
        }
    }
}
