/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class DashboardSignalTest extends AbstractSignalsTest {
    @Test
    void dashboard_bindVisible_throwsException() {
        var dashboard = new Dashboard();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            dashboard.bindVisible(new ValueSignal<>(false));
        });
    }

    @Test
    void dashboardWidget_bindVisible_throwsException() {
        var widget = new DashboardWidget();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            widget.bindVisible(new ValueSignal<>(false));
        });
    }

    @Test
    void dashboardSection_bindVisible_throwsException() {
        var section = new DashboardSection();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            section.bindVisible(new ValueSignal<>(false));
        });
    }
}
