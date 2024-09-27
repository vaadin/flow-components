/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard/dense-layout")
public class DashboardDenseLayoutIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void defaultDashboard_widgetsAreInDefaultPositions() {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        int widget1y = widgets.get(0).getLocation().getY();
        int widget3y = widgets.get(2).getLocation().getY();
        Assert.assertNotEquals(widget1y, widget3y, 20);
    }

    @Test
    public void setDenseLayoutTrue_widgetsPositionsAreUpdated() {
        clickElementWithJs("toggle-dense-layout");
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        int widget1y = widgets.get(0).getLocation().getY();
        int widget3y = widgets.get(2).getLocation().getY();
        Assert.assertEquals(widget1y, widget3y, 20);
    }

    @Test
    public void setDenseLayoutFalse_widgetsAreReturnedToDefaultPositions() {
        clickElementWithJs("toggle-dense-layout");
        clickElementWithJs("toggle-dense-layout");
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        int widget1y = widgets.get(0).getLocation().getY();
        int widget3y = widgets.get(2).getLocation().getY();
        Assert.assertNotEquals(widget1y, widget3y, 20);
    }
}
