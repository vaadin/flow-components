/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.io.Serializable;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.charts.events.DrilldownEvent;
import com.vaadin.flow.component.charts.model.ChartFunction;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.style.Theme;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.component.charts.util.ChartSerialization.JsonWithFunctions;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;
import com.vaadin.flow.server.CspMode;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

class ChartFunctionTest {

    @JsDefinition
    public interface Formatters extends Serializable {
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
        ui.setCspMode(CspMode.STRICT);
        chart = new Chart();
    }

    @Test
    void tooltip_setFormatter_functionAndStringClearEachOther() {
        Tooltip tooltip = new Tooltip();
        ChartFunction function = FORMATTERS.scaled(1000, "k");

        tooltip.setFormatter("function() { return 1; }");
        tooltip.setFormatter(function);
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
        dataLabels.setFormatter(function);
        Assertions.assertSame(function, dataLabels.getFormatterFunction());
        Assertions.assertNull(dataLabels.getFormatter());

        dataLabels.setFormatter("function() { return 1; }");
        Assertions.assertNull(dataLabels.getFormatterFunction());
    }

    @Test
    void toJsonWithFunctions_functionsRemovedFromJsonWithTheirPaths() {
        ChartFunction tooltipFormatter = FORMATTERS.scaled(1, "%");
        ChartFunction labelFormatter = FORMATTERS.scaled(1000, "k");
        chart.getConfiguration().getTooltip().setFormatter(tooltipFormatter);
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
    void toJson_functionWrittenAsNull() {
        chart.getConfiguration().getTooltip()
                .setFormatter(FORMATTERS.scaled(1, "%"));

        String json = ChartSerialization.toJSON(chart.getConfiguration());

        Assertions.assertTrue(json.contains("\"formatter\":null"), json);
    }

    @Test
    void attached_drawChartWithFunction_sendsFunctionAsArgument() {
        ChartFunction function = FORMATTERS.scaled(1000, "k");
        chart.getConfiguration().getTooltip().setFormatter(function);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();

        chart.drawChart();

        JsCall call = singleChartJsCall();
        Assertions.assertEquals("updateConfiguration", call.methodName());
        Assertions.assertEquals(function, call.flattenArguments().getLast());
        Assertions.assertTrue(
                ui.dumpJsCalls(chart.getElement(), "updateConfiguration")
                        .isEmpty());
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
        Assertions.assertEquals(List.of("[1,\"dataLabels\",\"formatter\"]"),
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
        Assertions.assertEquals(function, call.flattenArguments().getLast());
    }

    @Test
    void setThemeWithFunction_sendsFunctionAsArgument() {
        ChartFunction function = FORMATTERS.scaled(1000, "k");
        Theme theme = new Theme();
        DataLabels dataLabels = new DataLabels(true);
        dataLabels.setFormatter(function);
        theme.getPlotOptions().getLine().setDataLabels(dataLabels);

        ChartOptions.get(ui.getUI()).setTheme(theme);

        List<JsCall> calls = ui.dumpJsCalls(ui.getUI().getElement(),
                ChartJs.class);
        Assertions.assertEquals(1, calls.size());
        Assertions.assertEquals(function,
                calls.get(0).flattenArguments().getLast());
    }

    private JsCall singleChartJsCall() {
        List<JsCall> calls = ui.dumpJsCalls(chart.getElement(), ChartJs.class);
        Assertions.assertEquals(1, calls.size());
        return calls.get(0);
    }

    private static List<String> paths(ArrayNode paths) {
        return paths.valueStream().map(JsonNode::toString).toList();
    }

    private static <T extends com.vaadin.flow.component.charts.model.AbstractSeries> T withFormatter(
            T series, ChartFunction formatter) {
        DataLabels dataLabels = new DataLabels(true);
        dataLabels.setFormatter(formatter);
        PlotOptionsLine plotOptions = new PlotOptionsLine();
        plotOptions.setDataLabels(dataLabels);
        series.setPlotOptions(plotOptions);
        return series;
    }
}
