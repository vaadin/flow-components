/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.Arrays;
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
@TestPath("vaadin-dashboard")
public class DashboardIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void addWidgets_widgetsAreCorrectlyAdded() {
        assertWidgetsByTitle("Widget 1", "Widget 2", "Widget 3");
    }

    @Test
    public void addWidgetsAtIndex1_widgetIsAddedIntoTheCorrectPlace() {
        $("button").id("add-widget-at-index-1").click();
        assertWidgetsByTitle("Widget 1", "Widget at index 1", "Widget 2", "Widget 3");
    }

    @Test
    public void removeWidgets1And3_widgetsAreCorrectlyRemoved() {
        $("button").id("remove-widgets-1-and-3").click();
        assertWidgetsByTitle("Widget 2");
    }

    @Test
    public void removeAllWidgets_widgetsAreCorrectlyRemoved() {
        $("button").id("remove-all-widgets").click();
        assertWidgetsByTitle();
    }

    private void assertWidgetsByTitle(String... expectedWidgetTitles) {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        List<String> widgetTitles = widgets.stream()
                .map(DashboardWidgetElement::getTitle).toList();
        Assert.assertEquals(Arrays.asList(expectedWidgetTitles),
                widgetTitles);
    }
}
