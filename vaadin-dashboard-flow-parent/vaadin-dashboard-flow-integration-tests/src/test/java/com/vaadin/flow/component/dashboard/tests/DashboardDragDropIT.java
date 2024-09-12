/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard/drag-drop")
public class DashboardDragDropIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void reorderWidgetOnClientSide_itemsAreReorderedCorrectly() {
        var draggedWidget = dashboardElement.getWidgets().get(0);
        var targetWidget = dashboardElement.getWidgets().get(1);
        dragDropElement(draggedWidget, targetWidget);
        Assert.assertEquals(draggedWidget.getTitle(),
                dashboardElement.getWidgets().get(1).getTitle());
    }

    @Test
    public void reorderSectionOnClientSide_itemsAreReorderedCorrectly() {
        var draggedSection = dashboardElement.getSections().get(1);
        var targetWidget = dashboardElement.getWidgets().get(0);
        dragDropElement(draggedSection, targetWidget);
        Assert.assertEquals(draggedSection.getTitle(),
                dashboardElement.getSections().get(0).getTitle());
    }

    @Test
    public void reorderWidgetInSectionOnClientSide_itemsAreReorderedCorrectly() {
        var firstSection = dashboardElement.getSections().get(0);
        var draggedWidget = firstSection.getWidgets().get(0);
        var targetWidget = firstSection.getWidgets().get(1);
        dragDropElement(draggedWidget, targetWidget);
        firstSection = dashboardElement.getSections().get(0);
        Assert.assertEquals(draggedWidget.getTitle(),
                firstSection.getWidgets().get(1).getTitle());
    }

    @Test
    public void detachReattach_reorderWidgetOnClientSide_itemsAreReorderedCorrectly() {
        clickElementWithJs("toggle-attached");
        clickElementWithJs("toggle-attached");
        dashboardElement = $(DashboardElement.class).waitForFirst();
        reorderWidgetOnClientSide_itemsAreReorderedCorrectly();
    }

    @Test
    public void setDashboardNotEditable_widgetCannotBeDragged() {
        var widget = dashboardElement.getWidgets().get(0);
        Assert.assertTrue(isHeaderActionsVisible(widget));
        clickElementWithJs("toggle-editable");
        Assert.assertFalse(isHeaderActionsVisible(widget));
    }

    @Test
    public void setDashboardEditable_widgetCanBeDragged() {
        clickElementWithJs("toggle-editable");
        clickElementWithJs("toggle-editable");
        Assert.assertTrue(
                isHeaderActionsVisible(dashboardElement.getWidgets().get(0)));
    }

    private void dragDropElement(TestBenchElement draggedElement,
            TestBenchElement targetElement) {
        var dragHandle = getDragHandle(draggedElement);

        var yOffset = draggedElement.getLocation().getY() < targetElement
                .getLocation().getY() ? 10 : -10;
        var xOffset = draggedElement.getLocation().getX() < targetElement
                .getLocation().getX() ? 10 : -10;

        new Actions(driver).clickAndHold(dragHandle)
                .moveToElement(targetElement, xOffset, yOffset)
                .release(targetElement).build().perform();
    }

    private static boolean isHeaderActionsVisible(TestBenchElement element) {
        TestBenchElement headerActions = element.$("*").withId("header-actions")
                .first();
        return !"none".equals(headerActions.getCssValue("display"));
    }

    private static TestBenchElement getDragHandle(TestBenchElement element) {
        return element.$("*").withClassName("drag-handle").first();
    }
}
