/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@TestPath("dialog-test")
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
                "There is a opened change listener for this dialog");

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
        //Verify that element is not auto removed and that parent node is div
        waitForElementPresent(By.cssSelector("div > #dialog-in-ui-after-opened"));
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

        WebElement element = findElement(By.id("overlay"));
        List<WebElement> content = findInShadowRoot(element, By.id("content"));

        Assert.assertFalse("Couldn't find content for dialog",
                content.isEmpty());

        Long contentPadding = getLongValue(
                content.get(0).getCssValue("padding-left"))
                + getLongValue(content.get(0).getCssValue("padding-right"));

        Long contentMargin = getLongValue(
                content.get(0).getCssValue("margin-left"))
                + getLongValue(content.get(0).getCssValue("margin-right"));

        Long endpoint = contentPadding + contentMargin;

        assertThat("Content didn't have a with over the padding and margin",
                getLongValue(content.get(0).getCssValue("width")),
                greaterThan(endpoint));
    }

    @Test
    public void verifyDialogFullSize() {
        findElement(By.id("button-for-dialog-with-div")).click();
        WebElement overlay = getInShadowRoot(getOverlayContent(),
                By.id("overlay"));
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

    private WebElement getOverlayContent() {
        return findElement(By.tagName(DIALOG_OVERLAY_TAG));
    }
}
