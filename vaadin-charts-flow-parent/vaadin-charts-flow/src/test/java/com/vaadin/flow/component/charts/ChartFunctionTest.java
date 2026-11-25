/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.charts.events.DrilldownEvent;
import com.vaadin.flow.component.charts.model.ChartFunction;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataProviderSeries;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.OhlcItem;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.Theme;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.component.charts.util.ChartSerialization.JsonWithFunctions;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;
import com.vaadin.flow.server.ContentSecurityPolicyMode;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

class ChartFunctionTest {

    @JsDefinition
    public interface Formatters extends JsSnippets {
        @JsExpression("return this.y / $0 + $1;")
        ChartFunction scaled(int divisor, String suffix);
    }

    private static final Formatters FORMATTERS = JsSnippets
            .of(Formatters.class);

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;

    @BeforeEach
    void setup() {
        ui.setContentSecurityPolicyMode(ContentSecurityPolicyMode.STRICT);
        chart = new Chart();
    }

    @Test
    void tooltip_setFormatter_functionAndStringClearEachOther() {
        Tooltip tooltip = new Tooltip();
        ChartFunction function = FORMATTERS.scaled(1000, "k");

        tooltip.setFormatter("function() { return 1; }");
        tooltip.setFormatterFunction(function);
        Assertions.assertSame(function, tooltip.getFormatterFunction());
        Assertions.assertNull(tooltip.getFormatter());

        tooltip.setFormatter("function() { return 1; }");
        Assertions.assertNull(tooltip.getFormatterFunction());
        Assertions.assertEquals("function() { return 1; }",
                tooltip.getFormatter());
    }

    @Test
    void dataLabels_setFormatter_functionAndStringClearEachOther() {
        DataLabels dataLabels = new DataLabels(true);
        ChartFunction function = FORMATTERS.scaled(1000, "k");

        dataLabels.setFormatter("function() { return 1; }");
        dataLabels.setFormatterFunction(function);
        Assertions.assertSame(function, dataLabels.getFormatterFunction());
        Assertions.assertNull(dataLabels.getFormatter());

        dataLabels.setFormatter("function() { return 1; }");
        Assertions.assertNull(dataLabels.getFormatterFunction());
    }

    @Test
    void toJsonWithFunctions_functionsRemovedFromJsonWithTheirPaths() {
        ChartFunction tooltipFormatter = FORMATTERS.scaled(1, "%");
        ChartFunction labelFormatter = FORMATTERS.scaled(1000, "k");
        chart.getConfiguration().getTooltip()
                .setFormatterFunction(tooltipFormatter);
        chart.getConfiguration()
                .setSeries(withFormatter(new ListSeries(1, 2), labelFormatter));

        JsonWithFunctions json = ChartSerialization
                .toJsonWithFunctions(chart.getConfiguration());

        Assertions.assertFalse(json.json().get("tooltip").has("formatter"));
        Assertions.assertFalse(json.json().get("series").get(0)
                .get("dataLabels").has("formatter"));
        Assertions.assertEquals(
                List.of("[\"series\",0,\"dataLabels\",\"formatter\"]",
                        "[\"tooltip\",\"formatter\"]"),
                paths(json.functionPaths()));
        Assertions.assertEquals(List.of(labelFormatter, tooltipFormatter),
                json.functions());
    }

    @Test
    void toJsonWithFunctions_functionInAxis_pathThroughAxisList() {
        ChartFunction function = FORMATTERS.scaled(1000, "k");
        Configuration configuration = chart.getConfiguration();
        configuration.getxAxis().getLabels().setFormatterFunction(function);
        configuration.addyAxis(new YAxis());
        YAxis second = new YAxis();
        second.getLabels().setFormatterFunction(function);
        configuration.addyAxis(second);

        JsonWithFunctions json = ChartSerialization
                .toJsonWithFunctions(configuration);

        Assertions.assertEquals(
                List.of("[\"xAxis\",\"labels\",\"formatter\"]",
                        "[\"yAxis\",1,\"labels\",\"formatter\"]"),
                paths(json.functionPaths()));
    }

    @Test
    void toJsonWithFunctions_cspOff_stringFunctionNotReported() {
        ui.setContentSecurityPolicyMode(ContentSecurityPolicyMode.OFF);
        chart.getConfiguration().getTooltip().setFormatter("function() {}");
        chart.getConfiguration().setSeries(withFormatter(new ListSeries(1, 2),
                FORMATTERS.scaled(1000, "k")));

        JsonWithFunctions json = ChartSerialization
                .toJsonWithFunctions(chart.getConfiguration());

        Assertions.assertTrue(json.hasFunctions());
        Assertions.assertFalse(json.hasStringFunction());
    }

    @Test
    void toJsonWithFunctions_sameJsonAsToJson() {
        Configuration configuration = chart.getConfiguration();
        configuration.getxAxis().setCategories("a", "b");
        configuration.addyAxis(new YAxis());
        configuration.addPane(new Pane(0, 90));
        configuration.addPane(new Pane(90, 180));
        DataSeries items = new DataSeries();
        items.add(new DataSeriesItem("a", 1));
        items.add(new DataSeriesItem("b", 2.5));
        DataSeriesItem withX = new DataSeriesItem();
        withX.setX(10);
        withX.setY(5);
        items.add(withX);
        DataSeriesItem range = new DataSeriesItem();
        range.setLow(1);
        range.setHigh(2);
        items.add(range);
        items.add(new OhlcItem(1, 2, 3, 0.5, 2.5));
        configuration.addSeries(items);
        configuration.addSeries(new ListSeries(1, 2L, 3.5, null));
        configuration.addSeries(new DataProviderSeries<>(
                new ListDataProvider<>(List.of(1, 2)), Integer::doubleValue));

        JsonNode tree = ChartSerialization.toJsonWithFunctions(configuration)
                .json();

        // Compared as text: numbers can get other node types in a tree
        Assertions.assertEquals(ChartSerialization.toJSON(configuration),
                tree.toString());
        Assertions.assertFalse(containsPojo(tree), tree.toString());
    }

    private static boolean containsPojo(JsonNode node) {
        return node.isPojo()
                || node.valueStream().anyMatch(ChartFunctionTest::containsPojo);
    }

    @Test
    void toJson_functionLeftOut() {
        chart.getConfiguration().getTooltip()
                .setFormatterFunction(FORMATTERS.scaled(1, "%"));

        String json = ChartSerialization.toJSON(chart.getConfiguration());

        Assertions.assertFalse(json.contains("formatter"), json);
    }

    @Test
    void attached_drawChartWithFunction_sendsFunctionAsArgument() {
        ChartFunction function = FORMATTERS.scaled(1000, "k");
        chart.getConfiguration().getTooltip().setFormatterFunction(function);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();

        chart.drawChart();

        // One dump only: a dump clears all pending invocations
        List<PendingJavaScriptInvocation> invocations = ui
                .dumpPendingJavaScriptInvocations();
        Assertions.assertEquals(1, invocations.size());
        JsCall call = invocations.get(0).getInvocation().getJsCall();
        Assertions.assertEquals(ChartJs.class, call.definitionType());
        Assertions.assertEquals("updateConfiguration", call.methodName());
        Assertions.assertEquals(function, call.flattenArguments().getLast());
    }

    @Test
    void attached_drawChartWithoutFunction_callsUpdateConfiguration() {
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();

        chart.drawChart();

        Assertions.assertEquals(1, ui
                .dumpJsCalls(chart.getElement(), "updateConfiguration").size());
    }

    @Test
    void attached_addSeriesWithFunction_sendsFunctionAsArgument() {
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        ChartFunction function = FORMATTERS.scaled(1000, "k");

        chart.getConfiguration()
                .addSeries(withFormatter(new ListSeries(1, 2), function));

        JsCall call = singleChartJsCall();
        Assertions.assertEquals("callElementFunction", call.methodName());
        List<Object> arguments = call.flattenArguments();
        Assertions.assertEquals("__callChartFunction", arguments.get(0));
        Assertions.assertEquals(
                "[\"addSeries\",{\"data\":[1,2],\"dataLabels\":{\"enabled\":true},\"type\":\"line\"}]",
                arguments.get(1).toString());
        Assertions.assertEquals(List.of("[1,\"dataLabels\",\"formatter\"]"),
                paths((ArrayNode) arguments.get(2)));
        Assertions.assertEquals(function, arguments.get(3));
    }

    @Test
    void attached_addPointWithFunction_sendsFunctionAsArgument() {
        DataSeries series = new DataSeries();
        series.add(new DataSeriesItem("a", 1));
        chart.getConfiguration().addSeries(series);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        ChartFunction function = FORMATTERS.scaled(1000, "k");

        series.add(withFormatter(new DataSeriesItem("b", 2), function), true,
                false);

        JsCall call = singleChartJsCall();
        Assertions.assertEquals("callElementFunction", call.methodName());
        List<Object> arguments = call.flattenArguments();
        Assertions.assertEquals("__callSeriesFunction", arguments.get(0));
        Assertions.assertEquals(
                "[\"addPoint\",0,{\"dataLabels\":{\"enabled\":true},\"name\":\"b\",\"y\":2},true,false]",
                arguments.get(1).toString());
        Assertions.assertEquals(List.of("[2,\"dataLabels\",\"formatter\"]"),
                paths((ArrayNode) arguments.get(2)));
        Assertions.assertEquals(function, arguments.get(3));
    }

    @Test
    void attached_updatePointWithFunction_sendsFunctionAsArgument() {
        DataSeries series = new DataSeries();
        series.add(new DataSeriesItem("a", 1));
        DataSeriesItem item = new DataSeriesItem("b", 2);
        series.add(item);
        chart.getConfiguration().addSeries(series);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        ChartFunction function = FORMATTERS.scaled(1000, "k");

        withFormatter(item, function);
        series.update(item);

        JsCall call = singleChartJsCall();
        Assertions.assertEquals("callElementFunction", call.methodName());
        List<Object> arguments = call.flattenArguments();
        Assertions.assertEquals("__callPointFunction", arguments.get(0));
        Assertions.assertEquals(
                "[\"update\",0,1,{\"dataLabels\":{\"enabled\":true},\"name\":\"b\",\"y\":2}]",
                arguments.get(1).toString());
        Assertions.assertEquals(List.of("[3,\"dataLabels\",\"formatter\"]"),
                paths((ArrayNode) arguments.get(2)));
        Assertions.assertEquals(function, arguments.get(3));
    }

    @Test
    void drilldownWithFunction_sendsFunctionAsArgument() {
        chart.getConfiguration().addSeries(new DataSeries());
        DataSeries series = new DataSeries();
        series.setData(1, 2, 3);
        chart.getConfiguration().addSeries(series);
        ChartFunction function = FORMATTERS.scaled(1000, "k");
        chart.setDrilldownCallback(
                event -> withFormatter(new DataSeries("Drilldown"), function));
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();

        ComponentUtil.fireEvent(chart, new DrilldownEvent(chart, true, null,
                null, null, null, 2, null, 1));

        JsCall call = singleChartJsCall();
        Assertions.assertEquals("addSeriesAsDrilldown", call.methodName());
        List<Object> arguments = call.flattenArguments();
        Assertions.assertEquals(5, arguments.size());
        Assertions.assertFalse(((JsonNode) arguments.get(2)).get("dataLabels")
                .has("formatter"));
        Assertions.assertEquals(List.of("[\"dataLabels\",\"formatter\"]"),
                paths((ArrayNode) arguments.get(3)));
        Assertions.assertEquals(function, arguments.get(4));
    }

    @Test
    void setThemeWithFunction_sendsFunctionAsArgument() {
        ChartFunction function = FORMATTERS.scaled(1000, "k");
        Theme theme = new Theme();
        DataLabels dataLabels = new DataLabels(true);
        dataLabels.setFormatterFunction(function);
        theme.getPlotOptions().getLine().setDataLabels(dataLabels);

        ChartOptions.get(ui.getUI()).setTheme(theme);

        List<JsCall> calls = ui.dumpJsCalls(ui.getUI().getElement(),
                ChartJs.class);
        Assertions.assertEquals(1, calls.size());
        List<Object> arguments = calls.get(0).flattenArguments();
        Assertions.assertEquals(3, arguments.size());
        Assertions.assertFalse(((JsonNode) arguments.get(0)).get("plotOptions")
                .get("line").get("dataLabels").has("formatter"));
        Assertions.assertEquals(List
                .of("[\"plotOptions\",\"line\",\"dataLabels\",\"formatter\"]"),
                paths((ArrayNode) arguments.get(1)));
        Assertions.assertEquals(function, arguments.get(2));
    }

    private JsCall singleChartJsCall() {
        List<JsCall> calls = ui.dumpJsCalls(chart.getElement(), ChartJs.class);
        Assertions.assertEquals(1, calls.size());
        return calls.get(0);
    }

    private static List<String> paths(ArrayNode paths) {
        return paths.valueStream().map(JsonNode::toString).toList();
    }

    private static DataSeriesItem withFormatter(DataSeriesItem item,
            ChartFunction formatter) {
        DataLabels dataLabels = new DataLabels(true);
        dataLabels.setFormatterFunction(formatter);
        item.setDataLabels(dataLabels);
        return item;
    }

    private static <T extends com.vaadin.flow.component.charts.model.AbstractSeries> T withFormatter(
            T series, ChartFunction formatter) {
        DataLabels dataLabels = new DataLabels(true);
        dataLabels.setFormatterFunction(formatter);
        PlotOptionsLine plotOptions = new PlotOptionsLine();
        plotOptions.setDataLabels(dataLabels);
        series.setPlotOptions(plotOptions);
        return series;
    }
}
