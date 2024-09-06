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
@TestPath("vaadin-dashboard-widget")
public class DashboardWidgetIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void titleIsSetCorrectly() {
        Assert.assertEquals("Widget 1",
                dashboardElement.getWidgets().get(0).getTitle());
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
    public void defaultWidgetRowspanIsCorrect() {
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        widgets.forEach(widget -> Assert.assertEquals(Integer.valueOf(1),
                widget.getRowspan()));
    }

    @Test
    public void updateRowspans_rowspansForAllWidgetsUpdated() {
        clickElementWithJs("increase-all-rowspans-by-1");
        List<DashboardWidgetElement> widgets = dashboardElement.getWidgets();
        widgets.forEach(widget -> Assert.assertEquals(Integer.valueOf(2),
                widget.getRowspan()));
        clickElementWithJs("decrease-all-rowspans-by-1");
        widgets.forEach(widget -> Assert.assertEquals(Integer.valueOf(1),
                widget.getRowspan()));
    }

    @Test
    public void widgetWithInitialContent_contentIsCorrectlySet() {
        DashboardWidgetElement firstWidget = dashboardElement.getWidgets()
                .get(0);
        Assert.assertNotNull(firstWidget.getContent());
        Assert.assertTrue(
                firstWidget.getContent().getText().contains("Some content"));
    }

    @Test
    public void updateWidgetContent_contentIsCorrectlyUpdated() {
        clickElementWithJs("update-content-of-the-first-widget");
        DashboardWidgetElement firstWidget = dashboardElement.getWidgets()
                .get(0);
        Assert.assertNotNull(firstWidget.getContent());
        Assert.assertFalse(
                firstWidget.getContent().getText().contains("Some content"));
        Assert.assertTrue(
                firstWidget.getContent().getText().contains("Updated content"));
    }

    @Test
    public void removeWidgetContent_contentIsCorrectlyRemoved() {
        clickElementWithJs("remove-content-of-the-first-widget");
        DashboardWidgetElement firstWidget = dashboardElement.getWidgets()
                .get(0);
        Assert.assertFalse(firstWidget.getText().contains("Some content"));
        Assert.assertNull(firstWidget.getContent());
    }

    @Test
    public void widgetWithInitialHeader_headerIsCorrectlySet() {
        DashboardWidgetElement firstWidget = dashboardElement.getWidgets()
                .get(0);
        Assert.assertNotNull(firstWidget.getHeader());
        Assert.assertTrue(
                firstWidget.getHeader().getText().contains("Some header"));
    }

    @Test
    public void updateWidgetHeader_headerIsCorrectlyUpdated() {
        clickElementWithJs("update-header-of-the-first-widget");
        DashboardWidgetElement firstWidget = dashboardElement.getWidgets()
                .get(0);
        Assert.assertNotNull(firstWidget.getHeader());
        Assert.assertFalse(
                firstWidget.getHeader().getText().contains("Some header"));
        Assert.assertTrue(
                firstWidget.getHeader().getText().contains("Updated header"));
    }

    @Test
    public void removeWidgetHeader_headerIsCorrectlyRemoved() {
        clickElementWithJs("remove-header-of-the-first-widget");
        DashboardWidgetElement firstWidget = dashboardElement.getWidgets()
                .get(0);
        Assert.assertFalse(firstWidget.getText().contains("Some header"));
        Assert.assertNull(firstWidget.getHeader());
    }
}
