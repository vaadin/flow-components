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
import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.style.Theme;
import com.vaadin.flow.server.ContentSecurityPolicyMode;
import com.vaadin.flow.server.ContentSecurityPolicyViolationException;
import com.vaadin.tests.MockUIExtension;

/**
 * Tests that chart configuration with a JavaScript function as a string is
 * reported in the CSP modes, on each path that sends it to the browser.
 */
class ChartCspTest {

    private static final String FUNCTION = "function() { return this.y; }";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;

    @BeforeEach
    void setup() {
        chart = new Chart();
    }

    @Test
    void cspOff_functionInConfiguration_drawChart_updatesConfiguration() {
        ui.setContentSecurityPolicyMode(ContentSecurityPolicyMode.OFF);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        chart.getConfiguration().getTooltip().setFormatter(FUNCTION);

        chart.drawChart();

        List<Object> resets = ui
                .dumpJsCalls(chart.getElement(), "updateConfiguration").stream()
                .map(call -> call.flattenArguments().getLast()).toList();
        Assertions.assertEquals(List.of(false), resets);
    }

    @Test
    void strictMode_functionInConfiguration_drawChart_throws() {
        ui.setContentSecurityPolicyMode(ContentSecurityPolicyMode.STRICT);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        chart.getConfiguration().getTooltip().setFormatter(FUNCTION);

        Assertions.assertThrows(ContentSecurityPolicyViolationException.class,
                () -> chart.drawChart());
    }

    @Test
    void strictMode_functionInSeries_drawChart_throws() {
        ui.setContentSecurityPolicyMode(ContentSecurityPolicyMode.STRICT);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        chart.getConfiguration()
                .setSeries(withFormatter(new ListSeries(1, 2, 3)));

        Assertions.assertThrows(ContentSecurityPolicyViolationException.class,
                () -> chart.drawChart());
    }

    @Test
    void strictMode_attached_addSeriesWithFunction_throws() {
        ui.setContentSecurityPolicyMode(ContentSecurityPolicyMode.STRICT);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        Configuration configuration = chart.getConfiguration();
        ListSeries series = withFormatter(new ListSeries(1, 2, 3));

        Assertions.assertThrows(ContentSecurityPolicyViolationException.class,
                () -> configuration.addSeries(series));
    }

    @Test
    void strictMode_drilldownCallbackReturnsFunction_drilldown_throws() {
        ui.setContentSecurityPolicyMode(ContentSecurityPolicyMode.STRICT);
        chart.getConfiguration().addSeries(new DataSeries());
        DataSeries series = new DataSeries();
        series.setData(1, 2, 3);
        chart.getConfiguration().addSeries(series);
        chart.setDrilldownCallback(
                event -> withFormatter(new DataSeries("Drilldown")));
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        DrilldownEvent event = new DrilldownEvent(chart, true, null, null, null,
                null, 2, null, 1);

        Assertions.assertThrows(ContentSecurityPolicyViolationException.class,
                () -> ComponentUtil.fireEvent(chart, event));
    }

    @Test
    void strictMode_setThemeWithFunction_throws() {
        ui.setContentSecurityPolicyMode(ContentSecurityPolicyMode.STRICT);
        ChartOptions options = ChartOptions.get(ui.getUI());
        Theme theme = new Theme();
        theme.getPlotOptions().getLine().setDataLabels(createDataLabels());

        Assertions.assertThrows(ContentSecurityPolicyViolationException.class,
                () -> options.setTheme(theme));
    }

    private static <T extends AbstractSeries> T withFormatter(T series) {
        PlotOptionsLine plotOptions = new PlotOptionsLine();
        plotOptions.setDataLabels(createDataLabels());
        series.setPlotOptions(plotOptions);
        return series;
    }

    private static DataLabels createDataLabels() {
        DataLabels dataLabels = new DataLabels(true);
        dataLabels.setFormatter(FUNCTION);
        return dataLabels;
    }
}
