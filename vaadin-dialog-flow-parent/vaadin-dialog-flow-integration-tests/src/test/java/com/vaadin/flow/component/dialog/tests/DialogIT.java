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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-dialog-view")
public class DialogIT extends AbstractComponentIT {

    private static final String DIALOG_OVERLAY_TAG = "vaadin-dialog-overlay";

    @Test

    public void openAndCloseBasicDialog_labelRendered() {
        open();

        findElement(By.id("basic-dialog-button")).click();

        WebElement overlay = getOverlayContent().$("*").id("overlay");

        WebElement div = getOverlayContent().findElement(By.tagName("div"));
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
    public void openAndCloseConfirmationDialog_buttonsRenderedWithClickListeners() {
        open();

        WebElement messageLabel = findElement(
                By.id("confirmation-dialog-label"));

        findElement(By.id("confirmation-dialog-button")).click();
        getOverlayContent().findElements(By.tagName("vaadin-button")).get(0)
                .click();
        verifyDialogClosed();
        Assert.assertEquals("Confirmed!", messageLabel.getText());

        findElement(By.id("confirmation-dialog-button")).click();
        getOverlayContent().findElements(By.tagName("vaadin-button")).get(1)
                .click();
        verifyDialogClosed();
        Assert.assertEquals("Cancelled...", messageLabel.getText());
    }

    @Test
    public void validateClosingFromServerSide() {
        open();

        findElement(By.id("server-side-close-dialog-button")).click();
        verifyDialogOpened();

        executeScript("document.body.click()");
        verifyDialogOpened();

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        verifyDialogClosed();

        Assert.assertEquals("Closed from server-side",
                findElement(By.id("server-side-close-dialog-label")).getText());
    }

    @Test
    public void focusElementOnOpen() {
        open();

        findElement(By.id("focus-dialog-button")).click();

        WebElement element = getOverlayContent()
                .findElement(By.tagName("input"));

        Assert.assertTrue(element.equals(driver.switchTo().activeElement()));
    }

    @Test
    public void styleDialogContent() {
        open();

        scrollIntoViewAndClick(
                findElement(By.id("styled-content-dialog-button")));

        WebElement element = getOverlayContent()
                .findElement(By.className("my-style"));

        Assert.assertEquals("rgba(255, 0, 0, 1)", element.getCssValue("color"));
    }

    private TestBenchElement getOverlayContent() {
        return $(DIALOG_OVERLAY_TAG).first();
    }

    private void verifyDialogClosed() {
        waitForElementNotPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    private void verifyDialogOpened() {
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-dialog-view");
    }
}
