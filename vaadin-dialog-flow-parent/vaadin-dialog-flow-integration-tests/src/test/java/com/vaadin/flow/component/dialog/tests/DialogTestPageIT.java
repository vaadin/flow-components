/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-dialog/dialog-test")
public class DialogTestPageIT extends AbstractDialogIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void dialogWithOpenedChangeListener() {
        WebElement message = findElement(By.id("message"));
        WebElement eventCounterMessage = findElement(
                By.id("event-counter-message"));
        WebElement eventSourceMessage = findElement(
                By.id("event-source-message"));

        Assert.assertEquals("The open state of the dialog is false",
                message.getText());

        findElement(By.id("dialog-open")).click();
        verifyOpened();

        Assert.assertEquals("The open state of the dialog is true",
                message.getText());
        Assert.assertEquals("There should one event from opening the dialog",
                "Number of events is 1", eventCounterMessage.getText());
        Assert.assertEquals("The event came from server",
                eventSourceMessage.getText());

        assertDialogContent(
                "There is an opened change listener for this dialog");

        executeScript("document.body.click()");
        verifyClosed();
        Assert.assertEquals("The open state of the dialog is false",
                message.getText());
        Assert.assertEquals("Number of events is 2",
                eventCounterMessage.getText());
        Assert.assertEquals("The event came from server",
                eventSourceMessage.getText());

        findElement(By.id("dialog-open")).click();
        verifyOpened();
        executeScript("arguments[0].opened = false",
                findElement(By.id("dialog")));
        Assert.assertEquals("The event came from client",
                eventSourceMessage.getText());
    }

    @Test
    public void dialogWithContentMargin_wrapperDoesNotCollapse() {
        findElement(By.id("dialog-open")).click();

        TestBenchElement overlay = getOverlayComponent(getDialog());
        TestBenchElement content = overlay.$("*").id("content");

        Assert.assertEquals(content.getProperty("offsetHeight"),
                content.getProperty("scrollHeight"));
    }

    @Test
    public void dialogWithVerticalLayout_noScrollbar() {
        findElement(By.id("dialog-with-vertical-layout")).click();

        TestBenchElement overlay = getOverlayComponent(getDialog());
        TestBenchElement content = overlay.$("*").id("content");

        Assert.assertEquals(content.getProperty("offsetHeight"),
                content.getProperty("scrollHeight"));
    }

    @Test
    public void dialogNotAttachedToThePage_openAndClose_dialogIsAttachedAndRemoved() {
        WebElement open = findElement(By.id("dialog-outside-ui-open"));

        waitForElementNotPresent(By.id("dialog-outside-ui"));
        open.click();
        waitForElementPresent(By.id("dialog-outside-ui"));
        verifyOpened();
        executeScript("document.body.click()");
        verifyClosed();
        waitForElementNotPresent(By.id("dialog-outside-ui"));

        open.click();
        waitForElementPresent(By.id("dialog-outside-ui"));
        verifyOpened();
        WebElement close = getDialog()
                .findElement(By.id("dialog-outside-ui-close"));
        close.click();
        verifyClosed();
        waitForElementNotPresent(By.id("dialog-outside-ui"));
    }

    @Test
    public void dialogNotAttachedToThePage_openAndAttach_dialogIsAttachedAndNotRemoved() {
        waitForElementNotPresent(By.id("dialog-in-ui-after-opened"));
        findElement(By.id("dialog-in-ui-after-opened-open")).click();
        waitForElementPresent(By.id("dialog-in-ui-after-opened"));
        verifyOpened();
        executeScript("document.body.click()");
        verifyClosed();
        // Verify that element is not auto removed and that parent node is div
        waitForElementPresent(
                By.cssSelector("div > #dialog-in-ui-after-opened"));
    }

    @Test
    public void openDialogAddComponentAtFirst() {
        verifyInitialDialog(3);
        findElement(By.id("button-to-first")).click();
        assertButtonNumberInDialog(4);
        assertButtonText(0);
    }

    @Test
    public void openDialogAddComponentAtIndex() {
        verifyInitialDialog(3);
        findElement(By.id("button-to-second")).click();
        assertButtonNumberInDialog(4);
        assertButtonText(1);
    }

    private void assertButtonText(int index) {
        Assert.assertEquals("Button Text is not correct", "Added Button",
                getDialog().findElements(By.cssSelector("button")).get(index)
                        .getText());
    }

    private void verifyInitialDialog(int initialNumber) {
        waitForElementNotPresent(By.id("dialog-add-component-at-index"));
        findElement(By.id("open-dialog-add-component-at-index")).click();
        waitForElementPresent(By.id("dialog-add-component-at-index"));
        assertButtonNumberInDialog(initialNumber);
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        waitForElementNotPresent(By.id("dialog-add-component-at-index"));
        verifyClosed();
    }

    private void assertButtonNumberInDialog(int expectedButtonNumber) {
        waitUntil(driver -> ExpectedConditions.numberOfElementsToBe(
                By.cssSelector("vaadin-dialog[opened]"), expectedButtonNumber));
    }

    private void assertDialogContent(String expected) {
        String content = getDialog().getText();
        Assert.assertTrue("Dialog content should contain: " + expected,
                content.contains(expected));
    }

    @Test
    public void openEmptyDialog_dialogContentHasWidth() {
        findElement(By.id("open-button")).click();

        waitForElementPresent(By.id("empty-dialog"));

        TestBenchElement element = getOverlayComponent(getDialog());
        WebElement content = element.$("*").id("content");

        Assert.assertNotNull("Couldn't find content for dialog", content);

        Long contentPadding = getLongValue(content.getCssValue("padding-left"))
                + getLongValue(content.getCssValue("padding-right"));

        Long contentMargin = getLongValue(content.getCssValue("margin-left"))
                + getLongValue(content.getCssValue("margin-right"));

        Long endpoint = contentPadding + contentMargin;

        Long actualWidth = getLongValue(content.getCssValue("width"));
        Assert.assertTrue(
                "Content didn't have a width over the padding and margin",
                actualWidth > endpoint);
    }

    @Test
    public void verifyDialogFullSize() {
        findElement(By.id("button-for-dialog-with-div")).click();
        WebElement overlayPart = getOverlayPart(getDialog());
        Assert.assertTrue(
                overlayPart.getDomAttribute("style").contains("width: 100%;"));
        Assert.assertTrue(
                overlayPart.getDomAttribute("style").contains("height: 100%;"));

        WebElement div = findElement(By.id("div-in-dialog"));
        WebElement content = overlayPart.findElement(By.id("content"));

        String overLayWidth = overlayPart
                .getCssValue(ElementConstants.STYLE_WIDTH);
        int overlayWidthValue = Integer
                .valueOf(overLayWidth.substring(0, overLayWidth.length() - 2));

        String paddingWidth = content.getCssValue("padding");
        int paddingValue = Integer
                .valueOf(paddingWidth.substring(0, paddingWidth.length() - 2));

        String divWidth = div.getCssValue(ElementConstants.STYLE_WIDTH);
        int divWidthValue = Integer
                .valueOf(divWidth.substring(0, divWidth.length() - 2));

        Assert.assertEquals(overlayWidthValue - paddingValue * 2,
                divWidthValue);

        String overLayHeight = overlayPart
                .getCssValue(ElementConstants.STYLE_HEIGHT);
        int overLayHeightValue = Integer.valueOf(
                overLayHeight.substring(0, overLayHeight.length() - 2));

        String divHeight = div.getCssValue(ElementConstants.STYLE_HEIGHT);
        int divHeightValue = Integer
                .valueOf(divHeight.substring(0, divHeight.length() - 2));

        Assert.assertEquals(overLayHeightValue - paddingValue * 2,
                divHeightValue);
    }

    @Test
    public void resizableDialogShouldPreserveWidthAndHeight() {
        findElement(By.id("dialog-resizable-draggable-open-button")).click();

        TestBenchElement overlayPart = getOverlayPart(getDialog());

        Long overLayHeightBeforeResize = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_HEIGHT);
        Long overLayWidthBeforeResize = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_WIDTH);

        resizeDialog(overlayPart, 50, 50);

        Long overLayHeightAfterResize = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_HEIGHT);
        Long overLayWidthAfterResize = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_WIDTH);

        Assert.assertNotEquals(overLayHeightBeforeResize,
                overLayHeightAfterResize);
        Assert.assertNotEquals(overLayWidthBeforeResize,
                overLayWidthAfterResize);

        findElement(By.id("dialog-resizable-draggable-close-button")).click();
        verifyClosed();
        findElement(By.id("dialog-resizable-draggable-open-button")).click();

        overlayPart = getOverlayPart(getDialog());

        Long overLayHeightAfterReopen = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_HEIGHT);
        Long overLayWidthAfterReopen = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_WIDTH);

        Assert.assertEquals(overLayHeightAfterResize, overLayHeightAfterReopen);
        Assert.assertEquals(overLayWidthAfterResize, overLayWidthAfterReopen);
    }

    @Test
    public void verifyResizeLimitsAreRespected() {
        findElement(By.id("dialog-resizing-restrictions-button")).click();
        findElement(By.id("dialog-resizable-draggable-open-button")).click();

        Long maxValue = 225l;
        Long minValue = 175l;

        TestBenchElement overlayPart = getOverlayPart(getDialog());

        resizeDialog(overlayPart, 50, 50);

        Long overLayHeightAfterResize = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_HEIGHT);
        Long overLayWidthAfterResize = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_WIDTH);

        Assert.assertEquals(overLayHeightAfterResize, maxValue);
        Assert.assertEquals(overLayWidthAfterResize, maxValue);

        resizeDialog(overlayPart, -75, -75);

        overLayHeightAfterResize = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_HEIGHT);
        overLayWidthAfterResize = getSizeFromElement(overlayPart,
                ElementConstants.STYLE_WIDTH);

        Assert.assertEquals(overLayHeightAfterResize, minValue);
        Assert.assertEquals(overLayWidthAfterResize, minValue);
    }

    @Test
    public void resizableDialogListenerIsCalled() {
        findElement(
                By.id("dialog-resizable-draggable-set-initial-position-button"))
                .click();
        findElement(By.id("dialog-resizable-draggable-open-button")).click();
        WebElement message = findElement(
                By.id("dialog-resizable-draggable-message"));

        Assert.assertEquals(
                "Initial size with top (50), left (50), width (200) and height (200)",
                message.getText());

        TestBenchElement overlayPart = getOverlayPart(getDialog());

        resizeDialog(overlayPart, 50, 50);

        Assert.assertEquals(
                "Resize listener called with top (50), left (50), width (250) and height (250)",
                message.getText());

        resizeDialog(overlayPart, -50, -50, "nw");

        Assert.assertEquals(
                "Resize listener called with top (0), left (0), width (300) and height (300)",
                message.getText());
    }

    private void resizeDialog(TestBenchElement overlayContent, int xOffset,
            int yOffset) {
        resizeDialog(overlayContent, xOffset, yOffset, "se");
    }

    private void resizeDialog(TestBenchElement overlayContent, int xOffset,
            int yOffset, String direction) {
        WebElement resizer = overlayContent.$(".resizer." + direction).first();

        Actions resizeAction = new Actions(getDriver());
        resizeAction.dragAndDropBy(resizer, xOffset, yOffset);
        resizeAction.perform();
    }

    private Long getSizeFromElement(WebElement element, String cssProperty) {
        return getLongValue(element.getCssValue(cssProperty));
    }

    public void notAttachedDialog_opened_changeDimension() {
        findElement(By.id("dimension-open-self-attached-button")).click();
        findElement(By.id("dimension-change-size-self-attached")).click();

        WebElement overlay = getOverlayComponent(getDialog());
        String overlayWidth = overlay.getCssValue(ElementConstants.STYLE_WIDTH);
        String overlayHeight = overlay
                .getCssValue(ElementConstants.STYLE_HEIGHT);

        Assert.assertEquals(overlayWidth, "500px");
        Assert.assertEquals(overlayHeight, "500px");

        getDialog().findElement(By.tagName("button")).click();
        findElement(By.id("dimension-open-self-attached-button")).click();
        verifyOpened();

        overlay = getOverlayComponent(getDialog());
        overlayWidth = overlay.getCssValue(ElementConstants.STYLE_WIDTH);
        overlayHeight = overlay.getCssValue(ElementConstants.STYLE_HEIGHT);

        Assert.assertEquals(overlayWidth, "500px");
        Assert.assertEquals(overlayHeight, "500px");
    }

    @Test
    public void attachedDialog_beforeOpen_changeDimension() {
        // Change size of attached dialog
        findElement(By.id("dimension-change-size-attached")).click();
        // Open dialog
        findElement(By.id("dimension-open-attached-button")).click();

        WebElement overlayPart = getOverlayPart(getDialog());
        String overlayWidth = overlayPart
                .getCssValue(ElementConstants.STYLE_WIDTH);
        String overlayHeight = overlayPart
                .getCssValue(ElementConstants.STYLE_HEIGHT);

        Assert.assertEquals(overlayWidth, "500px");
        Assert.assertEquals(overlayHeight, "500px");

        getDialog().findElement(By.tagName("button")).click();
        findElement(By.id("dimension-open-attached-button")).click();
        verifyOpened();

        overlayPart = getOverlayPart(getDialog());
        overlayWidth = overlayPart.getCssValue(ElementConstants.STYLE_WIDTH);
        overlayHeight = overlayPart.getCssValue(ElementConstants.STYLE_HEIGHT);

        Assert.assertEquals(overlayWidth, "500px");
        Assert.assertEquals(overlayHeight, "500px");
    }

    @Test
    public void draggableDialog_shouldAllowDraggingFromContentPart() {
        findElement(By.id("dialog-resizable-draggable-open-button")).click();

        TestBenchElement overlayPart = getOverlayPart(getDialog());
        TestBenchElement contentPart = overlayPart.$("div").id("content");

        // resizing only to force component to set top/left values
        resizeDialog(overlayPart, 50, 50);
        String overlayLeft = overlayPart.getCssValue("left");
        String overlayTop = overlayPart.getCssValue("top");

        Actions dragAction = new Actions(getDriver());
        dragAction.dragAndDropBy(contentPart, 50, 50);
        dragAction.perform();

        Assert.assertNotEquals(overlayLeft, overlayPart.getCssValue("left"));
        Assert.assertNotEquals(overlayTop, overlayPart.getCssValue("top"));
    }

    @Test
    public void dragDialog_draggedEventFired() {
        findElement(By.id("dialog-resizable-draggable-set-position-button"))
                .click();
        findElement(By.id("dialog-resizable-draggable-open-button")).click();

        TestBenchElement overlayPart = getOverlayPart(getDialog());
        TestBenchElement contentPart = overlayPart.$("div").id("content");

        Actions draggingAction = new Actions(getDriver());
        draggingAction.dragAndDropBy(contentPart, 20, 20);
        draggingAction.perform();
        Assert.assertEquals(
                "Dragged listener called with top (120) and left (220)",
                findElement(By.id("dialog-resizable-draggable-message"))
                        .getText());
    }

    @Test
    public void setDialogPosition() {
        findElement(By.id("dialog-resizable-draggable-set-position-button"))
                .click();
        findElement(By.id("dialog-resizable-draggable-open-button")).click();

        TestBenchElement overlayPart = getOverlayPart(getDialog());
        Assert.assertEquals("100px", overlayPart.getCssValue("top"));
        Assert.assertEquals("200px", overlayPart.getCssValue("left"));
    }

    /**
     * Get the number for a css value with px suffix
     *
     * @param value
     *            css value to get
     * @return numeric value for given string with px suffix
     */
    private Long getLongValue(String value) {
        if (value == null) {
            return 0L;
        }

        StringBuilder number = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                number.append(value.charAt(i));
            } else {
                break;
            }
        }

        return Long.parseLong(number.toString());
    }
}
