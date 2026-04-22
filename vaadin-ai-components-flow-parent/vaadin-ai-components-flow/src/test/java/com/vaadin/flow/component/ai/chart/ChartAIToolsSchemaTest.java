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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.model.AbstractPlotOptions;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.ColorAxis;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Credits;
import com.vaadin.flow.component.charts.model.Cursor;
import com.vaadin.flow.component.charts.model.DashStyle;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.Dimension;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.Marker;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.SeriesTooltip;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.internal.JacksonUtils;

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
     * Tests that serialized {@link AbstractPlotOptions} objects validate
     * against the JSON schema returned by {@code get_plot_options_schema}. Each
     * test targets a specific field type handled by
     * {@link PlotOptionsSchemaGenerator}.
     */
    @Nested
    class PlotOptionsSchemaValidation {

        private static LLMProvider.ToolSpec schemaTool;

        @BeforeAll
        static void setUp() {
            schemaTool = ChartAITools.getPlotOptionsSchema();
        }

        @Test
        void parametersSchema_chartTypeHasTypeAndDescription()
                throws Exception {
            JsonNode paramSchema = MAPPER
                    .readTree(schemaTool.getParametersSchema());
            JsonNode chartType = paramSchema.get("properties").get("chartType");
            Assertions.assertNotNull(chartType, "chartType property missing");
            Assertions.assertTrue(chartType.has("type"),
                    "chartType should declare a type");
            Assertions.assertEquals("string", chartType.get("type").asString());
            Assertions.assertTrue(chartType.has("description"),
                    "chartType should have a description");
        }

        @Test
        void column_booleanAndNumberFields() {
            var opts = new PlotOptionsColumn();
            opts.setColorByPoint(true);
            opts.setAnimation(false);
            opts.setBorderRadius(5);
            opts.setBorderWidth(1);
            opts.setGroupPadding(0.2);
            assertPlotOptionsMatchSchema("column", opts);
        }

        @Test
        void column_stringAndColorFields() {
            var opts = new PlotOptionsColumn();
            opts.setClassName("custom-class");
            opts.setColor(new SolidColor("#ff0000"));
            opts.setNegativeColor(new SolidColor("#0000ff"));
            opts.setBorderColor(new SolidColor("#cccccc"));
            assertPlotOptionsMatchSchema("column", opts);
        }

        @Test
        void column_enumField_stacking() {
            var opts = new PlotOptionsColumn();
            opts.setStacking(Stacking.NORMAL);
            assertPlotOptionsMatchSchema("column", opts);
        }

        @Test
        void series_enumFields_dashStyleAndCursor() {
            var opts = new PlotOptionsSeries();
            opts.setDashStyle(DashStyle.LONGDASHDOT);
            opts.setCursor(Cursor.POINTER);
            assertPlotOptionsMatchSchema("series", opts);
        }

        @Test
        void series_stringArrayField_keys() {
            var opts = new PlotOptionsSeries();
            opts.setKeys("x", "y", "name");
            assertPlotOptionsMatchSchema("series", opts);
        }

        @Test
        void pie_colorArrayField() {
            var opts = new PlotOptionsPie();
            opts.setColors(new SolidColor("#ff0000"), new SolidColor("#00ff00"),
                    new SolidColor("#0000ff"));
            assertPlotOptionsMatchSchema("pie", opts);
        }

        @Test
        void series_expandableObject_dataLabels() {
            var opts = new PlotOptionsSeries();
            DataLabels dataLabels = new DataLabels();
            dataLabels.setEnabled(true);
            dataLabels.setFormat("{point.y:.1f}");
            dataLabels.setRotation(45);
            dataLabels.setColor(new SolidColor("#333333"));
            opts.setDataLabels(dataLabels);
            assertPlotOptionsMatchSchema("series", opts);
        }

        @Test
        void series_expandableObject_marker() {
            var opts = new PlotOptionsSeries();
            Marker marker = new Marker();
            marker.setEnabled(true);
            marker.setRadius(4);
            marker.setLineWidth(2);
            marker.setFillColor(new SolidColor("#ff0000"));
            opts.setMarker(marker);
            assertPlotOptionsMatchSchema("series", opts);
        }

        @Test
        void series_expandableObject_seriesTooltip() {
            var opts = new PlotOptionsSeries();
            SeriesTooltip tooltip = new SeriesTooltip();
            tooltip.setValueSuffix(" units");
            tooltip.setValuePrefix("$");
            tooltip.setPointFormat("{series.name}: <b>{point.y}</b>");
            opts.setTooltip(tooltip);
            assertPlotOptionsMatchSchema("series", opts);
        }

        @Test
        void line_lineWidthAndMarkerCombined() {
            var opts = new PlotOptionsLine();
            opts.setLineWidth(3);
            Marker marker = new Marker();
            marker.setEnabled(false);
            opts.setMarker(marker);
            assertPlotOptionsMatchSchema("line", opts);
        }

        @Test
        void pie_uniqueFields() {
            var opts = new PlotOptionsPie();
            opts.setDataLabels(null);
            opts.setInnerSize("50%");
            opts.setStartAngle(-90);
            opts.setEndAngle(90);
            opts.setSlicedOffset(10);
            assertPlotOptionsMatchSchema("pie", opts);
        }

        /**
         * Verifies the generated schema structure for specific properties,
         * covering every code path in {@link PlotOptionsSchemaGenerator}:
         * boolean, number, string, Color, enum, array, expandable nested
         * objects, non-expandable objects, descriptions, and field filters.
         */
        @Test
        void generatedSchema_coversAllFieldTypes() {
            JsonNode series = getSchemaNode("series").get("properties");

            // Boolean field
            assertPropertyType(series, "animation", "boolean");
            // Number field
            assertPropertyType(series, "lineWidth", "number");
            // String field
            assertPropertyType(series, "cursor", "string");
            // Color → string with CSS color description
            assertPropertyEquals(series, "color", "{\"type\":\"string\","
                    + "\"description\":\"CSS color (e.g. '#ff0000')\"}");
            // Enum → string with enum values
            JsonNode stacking = series.get("stacking");
            Assertions.assertEquals("string", stacking.get("type").asString());
            Assertions.assertTrue(stacking.has("enum"),
                    "Enum field should have 'enum' values");
            // Non-expandable objects are excluded (no useful schema)
            Assertions.assertNull(series.get("states"),
                    "Opaque object properties should be excluded");
            // Expandable types → nested properties
            for (String expanded : List.of("dataLabels", "marker", "tooltip")) {
                Assertions.assertTrue(series.get(expanded).has("properties"),
                        expanded + " should be expanded with nested "
                                + "properties");
            }
            // Descriptions parsed from JavaDoc, with {@link} tags cleaned
            Assertions.assertTrue(series.get("lineWidth").has("description"),
                    "Fields should have descriptions from JavaDoc");
            String animationDesc = series.get("animation").get("description")
                    .asString();
            Assertions.assertFalse(animationDesc.contains("{@link"),
                    "Descriptions should not contain raw JavaDoc tags: "
                            + animationDesc);
            // _fn_ fields excluded
            Assertions.assertFalse(series.has("_fn_pointFormatter"),
                    "_fn_ fields should be excluded");
            // Internal/technical fields excluded
            for (String excluded : List.of("className", "enableMouseTracking",
                    "turboThreshold", "skipKeyboardNavigation")) {
                Assertions.assertFalse(series.has(excluded),
                        "Internal field should be excluded: " + excluded);
            }
            // Color array (on pie)
            JsonNode pie = getSchemaNode("pie").get("properties");
            JsonNode colors = pie.get("colors");
            Assertions.assertEquals("array", colors.get("type").asString());
            Assertions.assertEquals("CSS color",
                    colors.get("items").get("description").asString());
        }

        private void assertPropertyType(JsonNode properties, String name,
                String expectedType) {
            Assertions.assertNotNull(properties.get(name),
                    "Missing property: " + name);
            Assertions.assertEquals(expectedType,
                    properties.get(name).get("type").asString(),
                    name + " should have type " + expectedType);
        }

        private void assertPropertyEquals(JsonNode properties, String name,
                String expectedJson) {
            Assertions.assertNotNull(properties.get(name),
                    "Missing property: " + name);
            Assertions.assertEquals(MAPPER.readTree(expectedJson),
                    properties.get(name), name + " schema mismatch");
        }

        @Test
        void descriptions_containNoJavaCodeFragments() {
            for (String type : PlotOptionsSchema.supportedTypes()) {
                JsonNode schema = getSchemaNode(type);
                JsonNode properties = schema.get("properties");
                if (properties == null) {
                    continue;
                }
                for (var field : properties.properties()) {
                    JsonNode desc = field.getValue().get("description");
                    if (desc == null) {
                        continue;
                    }
                    String text = desc.asString();
                    Assertions.assertFalse(
                            text.contains("public ")
                                    || text.contains("private ")
                                    || text.contains("return "),
                            type + "." + field.getKey()
                                    + " description contains Java code: "
                                    + text.substring(0,
                                            Math.min(text.length(), 80)));
                }
            }
        }

        @Test
        void allChartTypes_haveSchemas() {
            // Hardcoded list independent of the generator's supplier
            // list so this catches accidentally removed types.
            var expectedTypes = new String[] { "series", "area", "arearange",
                    "areaspline", "areasplinerange", "bar", "boxplot", "bubble",
                    "bullet", "candlestick", "column", "columnrange",
                    "errorbar", "flags", "funnel", "gantt", "gauge", "heatmap",
                    "line", "ohlc", "organization", "pie", "polygon", "pyramid",
                    "sankey", "scatter", "solidgauge", "spline", "timeline",
                    "treemap", "waterfall", "xrange" };
            for (String type : expectedTypes) {
                String result = schemaTool.execute(
                        JacksonUtils.createObjectNode().put("chartType", type));
                Assertions.assertFalse(result.startsWith("Error"),
                        "Missing schema for type '" + type + "': " + result);
            }
        }

        private JsonNode getSchemaNode(String chartType) {
            String json = schemaTool.execute(JacksonUtils.createObjectNode()
                    .put("chartType", chartType));
            Assertions.assertFalse(json.startsWith("Error"),
                    "Schema lookup failed: " + json);
            return MAPPER.readTree(json);
        }

        private void assertPlotOptionsMatchSchema(String chartType,
                AbstractPlotOptions plotOptions) {
            String schemaJson = schemaTool.execute(JacksonUtils
                    .createObjectNode().put("chartType", chartType));
            Assertions.assertFalse(schemaJson.startsWith("Error"),
                    "Schema lookup failed: " + schemaJson);
            var schema = SchemaRegistry
                    .withDefaultDialect(SpecificationVersion.DRAFT_4)
                    .getSchema(schemaJson);
            var json = toJSON(plotOptions);
            var errors = schema.validate(MAPPER.readTree(json));
            Assertions.assertTrue(errors.isEmpty(),
                    "Plot options JSON does not match " + chartType
                            + " schema: " + errors);
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
     * Handles "anyOf" by checking if at least one alternative covers all
     * properties.
     */
    private static void assertAllPropertiesCovered(JsonNode jsonNode,
            JsonNode schemaNode, String path, List<String> uncovered) {
        if (jsonNode == null || schemaNode == null) {
            return;
        }

        // Handle anyOf: at least one alternative must cover all properties
        if (schemaNode.has("anyOf")) {
            for (JsonNode option : schemaNode.get("anyOf")) {
                List<String> optionUncovered = new ArrayList<>();
                assertAllPropertiesCovered(jsonNode, option, path,
                        optionUncovered);
                if (optionUncovered.isEmpty()) {
                    return;
                }
            }
            uncovered
                    .add(path + ": no anyOf alternative covers all properties");
            return;
        }

        if (jsonNode.isObject()) {
            // An object node's schema should declare "type": "object"
            if (!schemaNode.has("type") && (schemaNode.has("properties")
                    || schemaNode.has("additionalProperties"))) {
                uncovered.add(path + ": missing \"type\": \"object\"");
            }
            if (schemaNode.has("properties")) {
                JsonNode schemaProperties = schemaNode.get("properties");
                JsonNode additionalProps = schemaNode
                        .get("additionalProperties");
                Iterator<Map.Entry<String, JsonNode>> fields = jsonNode
                        .properties().iterator();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String fieldName = field.getKey();
                    String fieldPath = path + "." + fieldName;

                    if (schemaProperties.has(fieldName)) {
                        assertAllPropertiesCovered(field.getValue(),
                                schemaProperties.get(fieldName), fieldPath,
                                uncovered);
                    } else if (additionalProps != null
                            && additionalProps.isObject()
                            && additionalProps.has("properties")) {
                        assertAllPropertiesCovered(field.getValue(),
                                additionalProps, fieldPath, uncovered);
                    } else if (additionalProps == null
                            && !EXCLUDED_PROPERTIES.contains(fieldPath)
                            && !INTERNAL_FIELD_NAMES.contains(fieldName)) {
                        uncovered.add(fieldPath);
                    }
                }
            } else if (schemaNode.has("additionalProperties")
                    && schemaNode.get("additionalProperties").isObject()
                    && schemaNode.get("additionalProperties")
                            .has("properties")) {
                JsonNode additionalProps = schemaNode
                        .get("additionalProperties");
                for (var field : jsonNode.properties()) {
                    assertAllPropertiesCovered(field.getValue(),
                            additionalProps, path + "." + field.getKey(),
                            uncovered);
                }
            } else if (!schemaNode.has("additionalProperties")
                    && jsonNode.size() > 0) {
                for (var field : jsonNode.properties()) {
                    String fieldPath = path + "." + field.getKey();
                    if (!EXCLUDED_PROPERTIES.contains(fieldPath)
                            && !INTERNAL_FIELD_NAMES.contains(field.getKey())) {
                        uncovered.add(fieldPath);
                    }
                }
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
