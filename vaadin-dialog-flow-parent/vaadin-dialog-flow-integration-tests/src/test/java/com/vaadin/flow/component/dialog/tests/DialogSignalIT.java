/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-dialog-signal-view")
public class DialogSignalIT extends AbstractDialogIT {

    @Test
    public void bindOpened_initiallyOpen() {
        open();

        String id = "initially-open-dialog";

        verifyOpened(id);

        // close the dialog from the client side
        executeScript("document.getElementById('" + id
                + "').$server.handleClientClose();");

        verifyClosedAndRemoved(id);
    }

    @Test
    public void bindOpened_openAndCloseConfirmationDialog_signalSynced() {
        open();

        String id = "confirmation-dialog";

        WebElement message = findElement(By.id("confirmation-dialog-message"));
        WebElement signalValue = findElement(
                By.id("confirmation-signal-value"));

        Assert.assertEquals("", signalValue.getText());

        findElement(By.id("confirmation-dialog-button")).click();
        verifyOpened(id);
        Assert.assertEquals("[Signal: true]", signalValue.getText());
        $(DialogElement.class).id(id).findElements(By.tagName("vaadin-button"))
                .get(0).click();
        verifyClosedAndRemoved(id);
        Assert.assertEquals("Confirmed!", message.getText());
        Assert.assertEquals("", signalValue.getText());

        findElement(By.id("confirmation-dialog-button")).click();
        verifyOpened(id);
        Assert.assertEquals("[Signal: true]", signalValue.getText());
        $(DialogElement.class).id(id).findElements(By.tagName("vaadin-button"))
                .get(1).click();
        verifyClosedAndRemoved(id);
        Assert.assertEquals("Cancelled...", message.getText());
        Assert.assertEquals("", signalValue.getText());
    }

    @Test
    public void bindOpened_validateClosingFromServerSide() {
        open();

        String id = "server-side-close-dialog";

        findElement(By.id("server-side-close-dialog-button")).click();
        verifyOpened(id);

        executeScript("document.body.click()");
        verifyOpened(id);

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        verifyClosedAndRemoved(id);

        Assert.assertEquals("Closed from server-side",
                findElement(By.id("server-side-close-dialog-message"))
                        .getText());
    }

    private void verifyClosedAndRemoved(String id) {
        waitForElementNotPresent(By.id(id));
    }

    protected void verifyOpened(String id) {
        waitForElementPresent(By.cssSelector("#" + id + "[opened]"));
    }
}
