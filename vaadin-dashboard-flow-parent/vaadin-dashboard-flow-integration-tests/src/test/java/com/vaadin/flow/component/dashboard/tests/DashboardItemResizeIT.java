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
import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard/item-resize")
public class DashboardItemResizeIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        getDriver().manage().window().setSize(new Dimension(1920, 1080));
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void dragResizeWidget_widgetIsResizedCorrectly() {
        assertWidgetResized(0);
    }

    @Test
    public void dragResizeWidgetInSection_widgetIsResizedCorrectly() {
        assertWidgetResized(1);
    }

    @Test
    public void keyboardResizeWidget_widgetIsResizedCorrectly() {
        assertWidgetKeyboardResized(0);
    }

    @Test
    public void keyboardResizeWidgetInSection_widgetIsResizedCorrectly() {
        assertWidgetKeyboardResized(1);
    }

    private void assertWidgetKeyboardResized(int widgetIndexToResize) {
        var widgetToResize = dashboardElement.getWidgets()
                .get(widgetIndexToResize);
        var initialWidth = widgetToResize.getSize().getWidth();
        // Select widget
        widgetToResize.sendKeys(Keys.ENTER);
        // Grow the widget
        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.RIGHT)
                .build().perform();
        var delta = 20;
        Assert.assertEquals(initialWidth * 2,
                widgetToResize.getSize().getWidth(), delta);
        // Shrink the widget back
        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.LEFT).build()
                .perform();
        Assert.assertEquals(initialWidth, widgetToResize.getSize().getWidth(),
                delta);
    }

    private void assertWidgetResized(int widgetIndexToResize) {
        var widgetToResize = dashboardElement.getWidgets()
                .get(widgetIndexToResize);
        var xResizeRatio = 2;
        var yResizeRatio = 2;
        var expectedWidth = widgetToResize.getSize().getWidth() * xResizeRatio;
        var expectedHeight = widgetToResize.getSize().getHeight()
                * yResizeRatio;
        resizeWidget(widgetIndexToResize, xResizeRatio, yResizeRatio);
        var resizedWidget = dashboardElement.getWidgets()
                .get(widgetIndexToResize);
        var delta = 20;
        Assert.assertEquals(expectedWidth, resizedWidget.getSize().getWidth(),
                delta);
        Assert.assertEquals(expectedHeight, resizedWidget.getSize().getHeight(),
                delta);
    }

    private void resizeWidget(int widgetIndexToResize, double xResizeRatio,
            double yResizeRatio) {
        var widgetToResize = dashboardElement.getWidgets()
                .get(widgetIndexToResize);
        var xOffset = (int) (widgetToResize.getSize().getWidth()
                * (xResizeRatio - 1));
        var yOffset = (int) (widgetToResize.getSize().getHeight()
                * (yResizeRatio - 1));
        TestBenchElement resizeHandle = getResizeHandle(widgetToResize);
        var trackStartOffset = 5;
        new Actions(driver).moveToElement(resizeHandle).clickAndHold()
                // This is necessary for the Polymer track event to be fired.
                .moveByOffset(trackStartOffset, trackStartOffset)
                .moveByOffset(xOffset - trackStartOffset,
                        yOffset - trackStartOffset)
                .release().build().perform();
    }

    private static TestBenchElement getResizeHandle(
            DashboardWidgetElement widgetElement) {
        return widgetElement.$("*").withClassName("resize-handle").first();
    }
}
