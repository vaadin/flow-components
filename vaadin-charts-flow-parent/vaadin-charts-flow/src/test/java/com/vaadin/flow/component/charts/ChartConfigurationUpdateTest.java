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

import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.server.CspMode;
import com.vaadin.flow.server.CspViolationException;
import com.vaadin.tests.MockUIExtension;

class ChartConfigurationUpdateTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;

    @BeforeEach
    void setup() {
        chart = new Chart();
    }

    @Test
    void attach_updatesConfigurationWithoutReset() {
        ui.add(chart);

        Assertions.assertEquals(List.of(false), getConfigurationResets());
    }

    @Test
    void setConfigurationBeforeAttach_attach_updatesConfigurationWithoutReset() {
        chart.setConfiguration(new Configuration());
        ui.add(chart);

        Assertions.assertEquals(List.of(false), getConfigurationResets());
    }

    @Test
    void attached_setConfiguration_updatesConfigurationWithReset() {
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();

        chart.setConfiguration(new Configuration());

        Assertions.assertEquals(List.of(true), getConfigurationResets());
    }

    @Test
    void attach_setConfigurationInSameRoundTrip_updatesConfigurationOnceWithReset() {
        ui.add(chart);
        chart.setConfiguration(new Configuration());

        Assertions.assertEquals(List.of(true), getConfigurationResets());
    }

    @Test
    void attached_setConfigurationTwice_updatesConfigurationOnceWithReset() {
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();

        chart.setConfiguration(new Configuration());
        chart.setConfiguration(new Configuration());

        Assertions.assertEquals(List.of(true), getConfigurationResets());
    }

    @Test
    void attached_setConfiguration_reattach_updatesConfigurationWithoutReset() {
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();

        chart.setConfiguration(new Configuration());
        ui.remove(chart);
        ui.add(chart);

        Assertions.assertEquals(List.of(false), getConfigurationResets());
    }

    @Test
    void attached_setConfiguration_nextRoundTrip_doesNotUpdateConfiguration() {
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        chart.setConfiguration(new Configuration());
        ui.dumpPendingJavaScriptInvocations();

        Assertions.assertEquals(List.of(), getConfigurationResets());
    }

    @Test
    void cspOff_functionInConfiguration_drawChart_updatesConfiguration() {
        ui.setCspMode(CspMode.OFF);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        chart.getConfiguration().getTooltip()
                .setFormatter("function() { return this.y; }");

        chart.drawChart();

        Assertions.assertEquals(List.of(false), getConfigurationResets());
    }

    @Test
    void strictMode_functionInConfiguration_drawChart_throws() {
        ui.setCspMode(CspMode.STRICT);
        ui.add(chart);
        ui.dumpPendingJavaScriptInvocations();
        chart.getConfiguration().getTooltip()
                .setFormatter("function() { return this.y; }");

        Assertions.assertThrows(CspViolationException.class,
                () -> chart.drawChart());
    }

    private List<Object> getConfigurationResets() {
        return ui.dumpJsCalls(chart.getElement(), "updateConfiguration")
                .stream().map(call -> call.flattenArguments().getLast())
                .toList();
    }
}
