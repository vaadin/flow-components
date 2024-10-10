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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.dashboard.testbench.DashboardElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard/item-move")
public class DashboardItemMoveIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        getDriver().manage().window().setSize(new Dimension(1920, 1080));
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void dragMoveWidget_itemIsMovedCorrectly() {
        var draggedWidget = dashboardElement.getWidgets().get(0);
        var targetWidget = dashboardElement.getWidgets().get(1);
        dragMoveElement(draggedWidget, targetWidget);
        Assert.assertEquals(draggedWidget.getTitle(),
                dashboardElement.getWidgets().get(1).getTitle());
    }

    @Test
    public void dragMoveSection_itemIsMovedCorrectly() {
        var draggedSection = dashboardElement.getSections().get(1);
        var targetWidget = dashboardElement.getWidgets().get(0);
        dragMoveElement(draggedSection, targetWidget);
        Assert.assertEquals(draggedSection.getTitle(),
                dashboardElement.getSections().get(0).getTitle());
    }

    @Test
    public void dragMoveWidgetInSection_itemIsMovedCorrectly() {
        var firstSection = dashboardElement.getSections().get(0);
        var draggedWidget = firstSection.getWidgets().get(0);
        var targetWidget = firstSection.getWidgets().get(1);
        dragMoveElement(draggedWidget, targetWidget);
        firstSection = dashboardElement.getSections().get(0);
        Assert.assertEquals(draggedWidget.getTitle(),
                firstSection.getWidgets().get(1).getTitle());
    }

    @Test
    public void keyboardMoveWidget_itemIsMovedCorrectly() {
        var widgetToMove = dashboardElement.getWidgets().get(0);
        var expectedTitle = widgetToMove.getTitle();
        // Select and move the widget
        widgetToMove.sendKeys(Keys.ENTER, Keys.RIGHT);
        widgetToMove = dashboardElement.getWidgets().get(1);
        Assert.assertEquals(expectedTitle, widgetToMove.getTitle());
        // Move the widget back
        widgetToMove.sendKeys(Keys.LEFT);
        Assert.assertEquals(expectedTitle,
                dashboardElement.getWidgets().get(0).getTitle());
    }

    @Test
    public void keyboardMoveSection_itemIsMovedCorrectly() {
        var sectionToMove = dashboardElement.getSections().get(0);
        var expectedTitle = sectionToMove.getTitle();
        // Select and move the section
        sectionToMove.sendKeys(Keys.ENTER, Keys.RIGHT);
        sectionToMove = dashboardElement.getSections().get(1);
        Assert.assertEquals(expectedTitle, sectionToMove.getTitle());
        // Move the section back
        sectionToMove.sendKeys(Keys.LEFT);
        Assert.assertEquals(expectedTitle,
                dashboardElement.getSections().get(0).getTitle());
    }

    @Test
    public void keyboardMoveWidgetInSection_itemIsMovedCorrectly() {
        var widgetToMove = dashboardElement.getWidgets().get(2);
        var expectedTitle = widgetToMove.getTitle();
        // Select and move the widget
        widgetToMove.sendKeys(Keys.ENTER, Keys.RIGHT);
        widgetToMove = dashboardElement.getWidgets().get(3);
        Assert.assertEquals(expectedTitle, widgetToMove.getTitle());
        // Move the widget back
        widgetToMove.sendKeys(Keys.LEFT);
        Assert.assertEquals(expectedTitle,
                dashboardElement.getWidgets().get(2).getTitle());
    }

    @Test
    public void detachReattach_dragMoveWidget_itemIsMovedCorrectly() {
        clickElementWithJs("toggle-attached");
        clickElementWithJs("toggle-attached");
        dashboardElement = $(DashboardElement.class).waitForFirst();
        dragMoveWidget_itemIsMovedCorrectly();
    }

    private void dragMoveElement(TestBenchElement draggedElement,
            TestBenchElement targetElement) {
        var dragHandle = getDragHandle(draggedElement);

        var yOffset = draggedElement.getLocation().getY() < targetElement
                .getLocation().getY() ? 10 : -10;
        var xOffset = draggedElement.getLocation().getX() < targetElement
                .getLocation().getX() ? 10 : -10;

        new Actions(getDriver()).clickAndHold(dragHandle)
                .moveToElement(targetElement, xOffset, yOffset)
                .release(targetElement).build().perform();
    }

    private static TestBenchElement getDragHandle(TestBenchElement element) {
        return element.$("*").withClassName("drag-handle").first();
    }
}
