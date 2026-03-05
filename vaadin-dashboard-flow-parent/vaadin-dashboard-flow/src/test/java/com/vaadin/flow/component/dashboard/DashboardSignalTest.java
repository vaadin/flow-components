/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import org.junit.Test;

import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class DashboardSignalTest extends AbstractSignalsUnitTest {
    @Test(expected = UnsupportedOperationException.class)
    public void dashboard_bindVisible_throwsException() {
        var dashboard = new Dashboard();
        dashboard.bindVisible(new ValueSignal<>(false));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void dashboardWidget_bindVisible_throwsException() {
        var widget = new DashboardWidget();
        widget.bindVisible(new ValueSignal<>(false));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void dashboardSection_bindVisible_throwsException() {
        var section = new DashboardSection();
        section.bindVisible(new ValueSignal<>(false));
    }
}
