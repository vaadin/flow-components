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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.dialog.demo.DialogView;
import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.By;

/**
 * Integration tests for the {@link DialogView}.
 */
public class DialogIT extends ComponentDemoTest {

    private static final String DIALOG_OVERLAY_TAG = "vaadin-dialog-overlay";

    @Test
    public void openAndCloseBasicDialog_labelRendered() {
        findElement(By.id("basic-dialog-button")).click();
        getOverlayContent().findElement(By.tagName("label"));

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        verifyDialogClosed();
    }

    @Test
    public void openAndCloseConfirmationDialog_buttonsRenderedWithClickListeners() {
        WebElement messageLabel = findElement(By.tagName("label"));

        findElement(By.id("confirmation-dialog-button")).click();
        getOverlayContent().findElements(By.tagName("button")).get(0).click();
        verifyDialogClosed();
        Assert.assertEquals("Confirmed!", messageLabel.getText());

        findElement(By.id("confirmation-dialog-button")).click();
        getOverlayContent().findElements(By.tagName("button")).get(1).click();
        verifyDialogClosed();
        Assert.assertEquals("Cancelled...", messageLabel.getText());
    }

    private WebElement getOverlayContent() {
        WebElement overlay = findElement(By.tagName(DIALOG_OVERLAY_TAG));
        return getInShadowRoot(overlay, By.id("content"));
    }

    private void verifyDialogClosed() {
        waitForElementNotPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-dialog");
    }
}
