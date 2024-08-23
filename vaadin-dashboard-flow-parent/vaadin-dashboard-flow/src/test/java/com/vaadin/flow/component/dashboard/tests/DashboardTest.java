/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;

public class DashboardTest {

    @Test
    public void addWidget_widgetIsAdded() {
        Dashboard dashboard = new Dashboard();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);

        Assert.assertEquals(List.of(widget1, widget2), dashboard.getWidgets());
    }

    @Test
    public void addNullWidget_exceptionIsThrown() {
        Dashboard dashboard = new Dashboard();
        Assert.assertThrows(NullPointerException.class, () -> dashboard.add((DashboardWidget) null));
    }

    @Test
    public void addNullWidgetInArray_noWidgetIsAdded() {
        Dashboard dashboard = new Dashboard();
        DashboardWidget widget = new DashboardWidget();
        try {
            dashboard.add(widget, null);
        } catch (NullPointerException e) {
            // Do nothing
        }
        Assert.assertEquals(Collections.emptyList(), dashboard.getWidgets());
    }

    @Test
    public void addWidgetAtIndex_widgetIsCorrectlyAdded() {
        Dashboard dashboard = new Dashboard();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        DashboardWidget widget3 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        dashboard.addWidgetAtIndex(1, widget3);

        Assert.assertEquals(List.of(widget1, widget3, widget2),
                dashboard.getWidgets());
    }

    @Test
    public void addNullWidgetAtIndex_exceptionIsThrown() {
        Dashboard dashboard = new Dashboard();
        Assert.assertThrows(NullPointerException.class, () -> dashboard.addWidgetAtIndex(0, null));
    }

    @Test
    public void removeWidget_widgetIsRemoved() {
        Dashboard dashboard = new Dashboard();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        dashboard.remove(widget1);

        Assert.assertEquals(List.of(widget2), dashboard.getWidgets());
    }

    @Test
    public void removeNullWidget_exceptionIsThrown() {
        Dashboard dashboard = new Dashboard();
        Assert.assertThrows(NullPointerException.class, () -> dashboard.remove((DashboardWidget) null));

    }

    @Test
    public void removeAllWidgets_widgetsAreRemoved() {
        Dashboard dashboard = new Dashboard();
        DashboardWidget widget1 = new DashboardWidget();
        DashboardWidget widget2 = new DashboardWidget();
        dashboard.add(widget1, widget2);
        dashboard.removeAll();

        Assert.assertEquals(Collections.emptyList(), dashboard.getWidgets());
    }
}
