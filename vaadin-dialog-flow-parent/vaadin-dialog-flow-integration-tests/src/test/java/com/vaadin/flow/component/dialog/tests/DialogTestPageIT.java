/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@TestPath("vaadin-dialog/dialog-test")
public class DialogTestPageIT extends AbstractComponentIT {

    static final String DIALOG_OVERLAY_TAG = "vaadin-dialog-overlay";

    @Before
    public void init() {
        open();
        waitUntil(
                driver -> findElements(By.tagName("vaadin-dialog")).size() > 0);
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
        checkDialogIsOpened();

        Assert.assertEquals("The open state of the dialog is true",
                message.getText());
        Assert.assertEquals(
                "There should not be initial events before opening the dialog",
                "Number of event is 0", eventCounterMessage.getText());
        Assert.assertEquals("The event came from server",
                eventSourceMessage.getText());

        assertDialogContent(
                "There is an opened change listener for this dialog");

        executeScript("document.body.click()");
        checkDialogIsClosed();
        Assert.assertEquals("The open state of the dialog is false",
                message.getText());
        Assert.assertEquals("Number of event is 1",
                eventCounterMessage.getText());
        Assert.assertEquals("The event came from server",
                eventSourceMessage.getText());

        findElement(By.id("dialog-open")).click();
        checkDialogIsOpened();
        executeScript("arguments[0].opened = false",
                findElement(By.id("dialog")));
        Assert.assertEquals("The event came from client",
                eventSourceMessage.getText());
    }

    @Test
    public void dialogWithContentMargin_wrapperDoesNotCollapse() {
        findElement(By.id("dialog-open")).click();

        TestBenchElement overlay = $("*").id("overlay");
        TestBenchElement content = overlay.$("*").id("content");

        Assert.assertEquals(content.getProperty("offsetHeight"),
                content.getProperty("scrollHeight"));
    }

    @Test
    public void dialogWithVerticalLayout_noScrollbar() {
        findElement(By.id("dialog-with-vertical-layout")).click();

        TestBenchElement overlay = $("*").id("overlay");
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
        checkDialogIsOpened();
        executeScript("document.body.click()");
        checkDialogIsClosed();
        waitForElementNotPresent(By.id("dialog-outside-ui"));

        open.click();
        waitForElementPresent(By.id("dialog-outside-ui"));
        checkDialogIsOpened();
        WebElement overlay = findElement(By.tagName(DIALOG_OVERLAY_TAG));
        WebElement close = overlay
                .findElement(By.id("dialog-outside-ui-close"));
        close.click();
        checkDialogIsClosed();
        waitForElementNotPresent(By.id("dialog-outside-ui"));
    }

    @Test
    public void dialogNotAttachedToThePage_openAndAttach_dialogIsAttachedAndNotRemoved() {
        waitForElementNotPresent(By.id("dialog-in-ui-after-opened"));
        findElement(By.id("dialog-in-ui-after-opened-open")).click();
        waitForElementPresent(By.id("dialog-in-ui-after-opened"));
        checkDialogIsOpened();
        executeScript("document.body.click()");
        checkDialogIsClosed();
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
                findElements(By.cssSelector(DIALOG_OVERLAY_TAG)).get(0)
                        .findElements(By.cssSelector("button")).get(index)
                        .getText());
    }

    private void verifyInitialDialog(int initialNumber) {
        waitForElementNotPresent(By.id("dialog-add-component-at-index"));
        findElement(By.id("open-dialog-add-component-at-index")).click();
        waitForElementPresent(By.id("dialog-add-component-at-index"));
        assertButtonNumberInDialog(initialNumber);
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        waitForElementNotPresent(By.id("dialog-add-component-at-index"));
        waitForElementNotPresent(By.cssSelector(DIALOG_OVERLAY_TAG));
    }

    private void assertButtonNumberInDialog(int expectedButtonNumber) {
        waitUntil(driver -> ExpectedConditions.numberOfElementsToBe(
                By.cssSelector(DIALOG_OVERLAY_TAG), expectedButtonNumber));
    }

    private void checkDialogIsClosed() {
        waitForElementNotPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    private void checkDialogIsOpened() {
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    private void assertDialogContent(String expected) {
        List<WebElement> dialogs = getDialogs();
        String content = dialogs.iterator().next().getText();
        Assert.assertThat(content, CoreMatchers.containsString(expected));
    }

    private List<WebElement> getDialogs() {
        return findElements(By.tagName(DIALOG_OVERLAY_TAG));
    }

    @Test
    public void openEmptyDialog_dialogContentHasWidth() {
        findElement(By.id("open-button")).click();

        waitForElementPresent(By.id("empty-dialog"));

        TestBenchElement element = $("*").id("overlay");
        WebElement content = element.$("*").id("content");

        Assert.assertNotNull("Couldn't find content for dialog", content);

        Long contentPadding = getLongValue(content.getCssValue("padding-left"))
                + getLongValue(content.getCssValue("padding-right"));

        Long contentMargin = getLongValue(content.getCssValue("margin-left"))
                + getLongValue(content.getCssValue("margin-right"));

        Long endpoint = contentPadding + contentMargin;

        assertThat("Content didn't have a with over the padding and margin",
                getLongValue(content.getCssValue("width")),
                greaterThan(endpoint));
    }

    @Test
    public void verifyDialogFullSize() {
        findElement(By.id("button-for-dialog-with-div")).click();
        WebElement overlay = getOverlayContent().$("*").id("overlay");
        Assert.assertTrue(
                overlay.getAttribute("style").contains("width: 100%;"));
        Assert.assertTrue(
                overlay.getAttribute("style").contains("height: 100%;"));

        WebElement div = findElement(By.id("div-in-dialog"));
        WebElement content = overlay.findElement(By.id("content"));

        String overLayWidth = overlay.getCssValue(ElementConstants.STYLE_WIDTH);
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

        String overLayHeight = overlay
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

        TestBenchElement overlayContent = getOverlayContent();
        WebElement overlay = overlayContent.$("*").id("overlay");

        Long overLayHeightBeforeResize = getSizeFromElement(overlay,
                ElementConstants.STYLE_HEIGHT);
        Long overLayWidthBeforeResize = getSizeFromElement(overlay,
                ElementConstants.STYLE_WIDTH);

        resizeDialog(overlayContent, 50, 50);

        Long overLayHeightAfterResize = getSizeFromElement(overlay,
                ElementConstants.STYLE_HEIGHT);
        Long overLayWidthAfterResize = getSizeFromElement(overlay,
                ElementConstants.STYLE_WIDTH);

        Assert.assertNotEquals(overLayHeightBeforeResize,
                overLayHeightAfterResize);
        Assert.assertNotEquals(overLayWidthBeforeResize,
                overLayWidthAfterResize);

        findElement(By.id("dialog-resizable-draggable-close-button")).click();
        waitForElementNotPresent(By.tagName(DIALOG_OVERLAY_TAG));
        findElement(By.id("dialog-resizable-draggable-open-button")).click();

        overlay = getOverlayContent().$("*").id("overlay");

        Long overLayHeightAfterReopen = getSizeFromElement(overlay,
                ElementConstants.STYLE_HEIGHT);
        Long overLayWidthAfterReopen = getSizeFromElement(overlay,
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

        TestBenchElement overlayContent = getOverlayContent();
        WebElement overlay = overlayContent.$("*").id("overlay");

        resizeDialog(overlayContent, 50, 50);

        Long overLayHeightAfterResize = getSizeFromElement(overlay,
                ElementConstants.STYLE_HEIGHT);
        Long overLayWidthAfterResize = getSizeFromElement(overlay,
                ElementConstants.STYLE_WIDTH);

        Assert.assertEquals(overLayHeightAfterResize, maxValue);
        Assert.assertEquals(overLayWidthAfterResize, maxValue);

        resizeDialog(overlayContent, -75, -75);

        overLayHeightAfterResize = getSizeFromElement(overlay,
                ElementConstants.STYLE_HEIGHT);
        overLayWidthAfterResize = getSizeFromElement(overlay,
                ElementConstants.STYLE_WIDTH);

        Assert.assertEquals(overLayHeightAfterResize, minValue);
        Assert.assertEquals(overLayWidthAfterResize, minValue);
    }

    @Test
    public void resizableDialogListenerIsCalled() {
        findElement(By.id("dialog-resizable-draggable-open-button")).click();
        WebElement message = findElement(
                By.id("dialog-resizable-draggable-message"));

        Assert.assertEquals(
                "Initial size with width (200px) and height (200px)",
                message.getText());

        TestBenchElement overlayContent = getOverlayContent();

        resizeDialog(overlayContent, 50, 50);

        Assert.assertEquals(
                "Rezise listener called with width (250px) and height (250px)",
                message.getText());
    }

    private void resizeDialog(TestBenchElement overlayContent, int xOffset,
            int yOffset) {
        WebElement resizerSE = overlayContent.$(".resizer.se").first();

        Actions resizeAction = new Actions(getDriver());
        resizeAction.dragAndDropBy(resizerSE, xOffset, yOffset);
        resizeAction.perform();
    }

    private Long getSizeFromElement(WebElement element, String cssProperty) {
        return getLongValue(element.getCssValue(cssProperty));
    }

    public void notAttachedDialog_opened_changeDimension() {
        findElement(By.id("dimension-open-self-attached-button")).click();
        findElement(By.id("dimension-change-size-self-attached")).click();

        WebElement overlay = getOverlayContent().$("*").id("overlay");
        String overlayWidth = overlay.getCssValue(ElementConstants.STYLE_WIDTH);
        String overlayHeight = overlay
                .getCssValue(ElementConstants.STYLE_HEIGHT);

        Assert.assertEquals(overlayWidth, "500px");
        Assert.assertEquals(overlayHeight, "500px");

        getOverlayContent().findElement(By.tagName("button")).click();
        findElement(By.id("dimension-open-self-attached-button")).click();
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        overlay = getOverlayContent().$("*").id("overlay");
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

        WebElement overlay = getOverlayContent().$("*").id("overlay");
        String overlayWidth = overlay.getCssValue(ElementConstants.STYLE_WIDTH);
        String overlayHeight = overlay
                .getCssValue(ElementConstants.STYLE_HEIGHT);

        Assert.assertEquals(overlayWidth, "500px");
        Assert.assertEquals(overlayHeight, "500px");

        getOverlayContent().findElement(By.tagName("button")).click();
        findElement(By.id("dimension-open-attached-button")).click();
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        overlay = getOverlayContent().$("*").id("overlay");
        overlayWidth = overlay.getCssValue(ElementConstants.STYLE_WIDTH);
        overlayHeight = overlay.getCssValue(ElementConstants.STYLE_HEIGHT);

        Assert.assertEquals(overlayWidth, "500px");
        Assert.assertEquals(overlayHeight, "500px");
    }

    @Test
    public void draggableDialog_shouldAllowDraggingFromDivContainer() {
        findElement(By.id("dialog-resizable-draggable-open-button")).click();

        TestBenchElement overlayContent = getOverlayContent();
        TestBenchElement container = overlayContent.$("div").first();
        WebElement overlay = overlayContent.$("*").id("overlay");

        // resizing only to force component to set top/left values
        resizeDialog(overlayContent, 50, 50);
        String overlayLeft = overlay.getCssValue("left");
        String overlayTop = overlay.getCssValue("top");

        Actions dragAction = new Actions(getDriver());
        dragAction.dragAndDropBy(container, 50, 50);
        dragAction.perform();

        Assert.assertNotEquals(overlayLeft, overlay.getCssValue("left"));
        Assert.assertNotEquals(overlayTop, overlay.getCssValue("top"));
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

    private TestBenchElement getOverlayContent() {
        return $(DIALOG_OVERLAY_TAG).first();
    }
}
