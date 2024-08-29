/**
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
        clickElementWithJs("add-widget-at-index-1");
        assertWidgetsByTitle("Widget 1", "Widget at index 1", "Widget 2",
                "Widget 3");
    }

    @Test
    public void removeFirstAndLastWidgets_widgetsAreCorrectlyRemoved() {
        clickElementWithJs("remove-first-and-last-widgets");
        assertWidgetsByTitle("Widget 2");
    }

    @Test
    public void removeAllWidgets_widgetsAreCorrectlyRemoved() {
        clickElementWithJs("remove-all-widgets");
        assertWidgetsByTitle();
    }

    @Test
    public void changeMaximumColumnCountTo1_widgetsShouldBeOnTheSameColumn() {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        // The first two widgets should initially be on the same horizontal line
        int yOfWidget1 = widgets.get(0).getLocation().getY();
        Assert.assertEquals(yOfWidget1, widgets.get(1).getLocation().getY());

        clickElementWithJs("set-maximum-column-count-1");
        // The first two widgets should be on the same vertical line
        int xOfWidget1 = widgets.get(0).getLocation().getX();
        Assert.assertEquals(xOfWidget1, widgets.get(1).getLocation().getX());
    }

    @Test
    public void changeMaximumColumnCountToNull_widgetsShouldBeOnTheSameRow() {
        clickElementWithJs("set-maximum-column-count-1");
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        // The first two widgets should be on the same vertical line
        int xOfWidget1 = widgets.get(0).getLocation().getX();
        Assert.assertEquals(xOfWidget1, widgets.get(1).getLocation().getX());

        clickElementWithJs("set-maximum-column-count-null");
        // The widgets should be on the same horizontal line
        int yOfWidget1 = widgets.get(0).getLocation().getY();
        Assert.assertEquals(yOfWidget1, widgets.get(1).getLocation().getY());
    }

    @Test
    public void defaultWidgetColspanIsCorrect() {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        widgets.forEach(widget -> Assert.assertEquals(Integer.valueOf(1),
                widget.getColspan()));
    }

    @Test
    public void updateColspans_colspansForAllWidgetsUpdated() {
        clickElementWithJs("increase-all-colspans-by-1");
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        widgets.forEach(widget -> Assert.assertEquals(Integer.valueOf(2),
                widget.getColspan()));
        clickElementWithJs("decrease-all-colspans-by-1");
        widgets.forEach(widget -> Assert.assertEquals(Integer.valueOf(1),
                widget.getColspan()));
    }

    @Test
    public void setMinAndMaxColumnWidths_columnWidthIsInTheRange() {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        int initialWidth = widgets.get(0).getSize().getWidth();
        Assert.assertTrue(initialWidth > 50);
        clickElementWithJs("set-column-width-range-to-40px-50px");
        waitUntil(
                driver -> initialWidth != widgets.get(0).getSize().getWidth());
        int updatedWidth = widgets.get(0).getSize().getWidth();
        Assert.assertTrue(updatedWidth >= 40 && updatedWidth <= 50);
    }

    @Test
    public void setMinAndMaxColumnWidths_setMinAndMaxColumnWidthsNull_columnWidthReturnsToInitialState() {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        int initialWidth = widgets.get(0).getSize().getWidth();
        clickElementWithJs("set-column-width-range-to-40px-50px");
        waitUntil(
                driver -> initialWidth != widgets.get(0).getSize().getWidth());
        int updatedWidth = widgets.get(0).getSize().getWidth();
        clickElementWithJs("set-max-and-min-column-widths-null");
        waitUntil(
                driver -> updatedWidth != widgets.get(0).getSize().getWidth());
        Assert.assertEquals(initialWidth, widgets.get(0).getSize().getWidth());
    }

    private void assertWidgetsByTitle(String... expectedWidgetTitles) {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        List<String> widgetTitles = widgets.stream()
                .map(DashboardWidgetElement::getTitle).toList();
        Assert.assertEquals(Arrays.asList(expectedWidgetTitles), widgetTitles);
    }
}
