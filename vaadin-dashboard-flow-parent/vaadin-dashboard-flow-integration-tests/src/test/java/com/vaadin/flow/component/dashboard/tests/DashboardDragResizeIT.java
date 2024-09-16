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
import com.vaadin.flow.component.dashboard.testbench.DashboardWidgetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-dashboard/drag-resize")
public class DashboardDragResizeIT extends AbstractComponentIT {

    private DashboardElement dashboardElement;

    @Before
    public void init() {
        open();
        dashboardElement = $(DashboardElement.class).waitForFirst();
    }

    @Test
    public void enlargeWidgetHorizontally_widgetIsEnlargedCorrectly() {
        assertWidgetResized(0, 2, 1);
    }

    @Test
    public void enlargeWidgetVertically_widgetIsEnlargedCorrectly() {
        assertWidgetResized(0, 1, 2);
    }

    @Test
    public void enlargeWidgetBothHorizontallyAndVertically_widgetIsEnlargedCorrectly() {
        assertWidgetResized(0, 2, 2);
    }

    @Test
    public void shrinkWidgetHorizontally_widgetIsShrunkCorrectly() {
        assertWidgetResized(1, 0.5, 1);
    }

    @Test
    public void shrinkWidgetVertically_widgetIsShrunkCorrectly() {
        assertWidgetResized(1, 1, 0.5);
    }

    @Test
    public void shrinkWidgetBothHorizontallyAndVertically_widgetIsShrunkCorrectly() {
        assertWidgetResized(1, 0.5, 0.5);
    }

    @Test
    public void enlargeWidgetInSectionHorizontally_widgetIsEnlargedCorrectly() {
        assertWidgetResized(2, 2, 1);
    }

    @Test
    public void enlargeWidgetInSectionVertically_widgetIsEnlargedCorrectly() {
        assertWidgetResized(2, 1, 2);
    }

    @Test
    public void enlargeWidgetInSectionBothHorizontallyAndVertically_widgetIsEnlargedCorrectly() {
        assertWidgetResized(2, 2, 2);
    }

    @Test
    public void shrinkWidgetInSectionHorizontally_widgetIsShrunkCorrectly() {
        assertWidgetResized(3, 0.5, 1);
    }

    @Test
    public void shrinkWidgetInSectionVertically_widgetIsShrunkCorrectly() {
        assertWidgetResized(3, 1, 0.5);
    }

    @Test
    public void shrinkWidgetInSectionBothHorizontallyAndVertically_widgetIsShrunkCorrectly() {
        assertWidgetResized(3, 0.5, 0.5);
    }

    @Test
    public void setDashboardNotEditable_resizeHandleNotVisible() {
        var widget = dashboardElement.getWidgets().get(0);
        Assert.assertTrue(isResizeHandleVisible(widget));
        clickElementWithJs("toggle-editable");
        Assert.assertFalse(isResizeHandleVisible(widget));
    }

    @Test
    public void setDashboardEditable_resizeHandleNotVisible() {
        clickElementWithJs("toggle-editable");
        clickElementWithJs("toggle-editable");
        Assert.assertTrue(
                isResizeHandleVisible(dashboardElement.getWidgets().get(0)));
    }

    private void assertWidgetResized(int widgetIndexToResize,
            double xResizeRatio, double yResizeRatio) {
        var widgetToResize = dashboardElement.getWidgets()
                .get(widgetIndexToResize);
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
        int trackStartOffset = 5;
        new Actions(driver).moveToElement(resizeHandle).clickAndHold()
                // This is necessary for the Polymer track event to be fired.
                .moveByOffset(trackStartOffset, trackStartOffset)
                .moveByOffset(xOffset - trackStartOffset,
                        yOffset - trackStartOffset)
                .release().build().perform();
    }

    private boolean isResizeHandleVisible(
            DashboardWidgetElement widgetElement) {
        return !"none"
                .equals(getResizeHandle(widgetElement).getCssValue("display"));
    }

    private static TestBenchElement getResizeHandle(
            DashboardWidgetElement widgetElement) {
        return widgetElement.$("*").withClassName("resize-handle").first();
    }
}
