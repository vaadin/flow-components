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
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.charts.events.DrilldownEvent;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsCall;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ObjectNode;

class ChartJsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void setLang_setsHighchartsOptions() {
        Lang lang = new Lang();
        lang.setLoading("Loading...");

        ChartOptions.get(ui.getUI()).setLang(lang);

        List<JsCall> calls = getChartJsCalls(ui.getUI().getElement());
        Assertions.assertEquals(1, calls.size());
        JsCall call = calls.get(0);
        Assertions.assertEquals("setHighchartsOptions", call.methodName());
        ObjectNode options = (ObjectNode) call.arguments().get(0);
        Assertions.assertEquals("Loading...",
                options.get("lang").get("loading").asString());
    }

    @Test
    void drilldownCallback_drilldown_addsSeriesAsDrilldown() {
        Chart chart = new Chart();
        chart.getConfiguration().addSeries(new DataSeries());
        DataSeries series = new DataSeries();
        series.setData(1, 2, 3);
        chart.getConfiguration().addSeries(series);
        chart.setDrilldownCallback(event -> new DataSeries("Drilldown"));
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();

        ComponentUtil.fireEvent(chart, new DrilldownEvent(chart, true, null,
                null, null, null, 2, null, 1));

        List<JsCall> calls = getChartJsCalls(chart.getElement());
        Assertions.assertEquals(1, calls.size());
        JsCall call = calls.get(0);
        Assertions.assertEquals("addSeriesAsDrilldown", call.methodName());
        Assertions.assertEquals(1, call.arguments().get(0));
        Assertions.assertEquals(2, call.arguments().get(1));
        ObjectNode drilldownSeries = (ObjectNode) call.arguments().get(2);
        Assertions.assertEquals("Drilldown",
                drilldownSeries.get("name").asString());
    }

    private List<JsCall> getChartJsCalls(Element owner) {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .filter(pending -> pending.getOwner() == owner.getNode())
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(JavaScriptInvocation::getJsCall).filter(Objects::nonNull)
                .filter(call -> call.definitionType() == ChartJs.class)
                .toList();
    }
}
