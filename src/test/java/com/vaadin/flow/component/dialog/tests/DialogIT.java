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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.dialog.demo.DialogView;
import com.vaadin.flow.demo.ComponentDemoTest;

/**
 * Integration tests for the {@link DialogView}.
 */
public class DialogIT extends ComponentDemoTest {

    private static final String DIALOG_OVERLAY_TAG = "vaadin-dialog-overlay";

    @Test
    public void openAndCloseBasicDialog_labelRendered() {
        findElement(By.id("basic-dialog-button")).click();

        WebElement container = getOverlayContent().findElement(By.tagName("div"));
        Assert.assertEquals("400px", container.getCssValue("width"));
        Assert.assertEquals("150px", container.getCssValue("height"));

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        verifyDialogClosed();
    }

    @Test
    public void openAndCloseConfirmationDialog_buttonsRenderedWithClickListeners() {
        WebElement messageLabel = findElement(
                By.id("confirmation-dialog-label"));

        findElement(By.id("confirmation-dialog-button")).click();
        getOverlayContent().findElements(By.tagName("button")).get(0).click();
        verifyDialogClosed();
        Assert.assertEquals("Confirmed!", messageLabel.getText());

        findElement(By.id("confirmation-dialog-button")).click();
        getOverlayContent().findElements(By.tagName("button")).get(1).click();
        verifyDialogClosed();
        Assert.assertEquals("Cancelled...", messageLabel.getText());
    }

    @Test
    public void validateClosingFromServerSide() {
        findElement(By.id("server-side-close-dialog-button")).click();
        verifyDialogOpened();

        executeScript("document.body.click()");
        verifyDialogOpened();

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        verifyDialogClosed();

        Assert.assertEquals("Closed from server-side",
                findElement(By.id("server-side-close-dialog-label")).getText());
    }

    private WebElement getOverlayContent() {
        return findElement(By.tagName(DIALOG_OVERLAY_TAG));
    }

    private void verifyDialogClosed() {
        waitForElementNotPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    private void verifyDialogOpened() {
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-dialog");
    }
}
