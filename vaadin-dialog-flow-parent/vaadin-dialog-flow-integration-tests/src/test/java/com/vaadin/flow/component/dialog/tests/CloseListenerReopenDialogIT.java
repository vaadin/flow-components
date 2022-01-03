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
import org.openqa.selenium.interactions.Actions;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-dialog/close-listener-reopen-dialog")
public class CloseListenerReopenDialogIT extends AbstractComponentIT {

    @Test
    public void reopenDialog_closeActionListenerIsCalled() {
        open();

        waitForElementPresent(By.id("open"));
        findElement(By.id("open")).click();

        // Dialog is opened
        Assert.assertTrue(
                isElementPresent(By.tagName("vaadin-dialog-overlay")));

        // try to close dialog
        closeDialog();

        Assert.assertTrue("Close dialog is not handled on the server side",
                isElementPresent(By.className("main")));

        // close dialog via button
        $("button").id("close").click();

        // reopen
        findElement(By.id("open")).click();

        // try to close dialog
        closeDialog();

        Assert.assertEquals(
                "Close dialog after reopen is not handled on the server side",
                2, findElements(By.className("main")).size());
    }

    @Test
    public void reopenDialog_removeCloseListener_dialogIsClosed()
            throws InterruptedException {
        open();

        findElement(By.id("open")).click();

        // Dialog is opened
        Assert.assertTrue(
                isElementPresent(By.tagName("vaadin-dialog-overlay")));

        // try to close dialog
        closeDialog();

        // close dialog via button
        $("button").id("close").click();

        // remove close listener
        findElement(By.id("remove")).click();

        // reopen
        findElement(By.id("open")).click();

        // try to close dialog
        closeDialog();

        // Dialog should be closed
        waitUntilNot(driver -> isElementPresent(
                By.tagName("vaadin-dialog-overlay")));
    }

    @Test
    public void openSubDialog_closeListenerIsCalled() {
        open();

        findElement(By.id("open")).click();

        findElement(By.id("open-sub")).click();

        // Two dialogs are opened
        Assert.assertEquals("Expected two dialogs", 2,
                findElements(By.tagName("vaadin-dialog-overlay")).size());

        closeDialog();

        // One dialog is opened
        waitUntil(driver -> findElements(By.tagName("vaadin-dialog-overlay"))
                .size() == 1);

        // close action listener prints its info message
        Assert.assertEquals(
                "No expected info message from close action listener for the subdialog",
                1, findElements(By.className("sub")).size());
    }

    private void closeDialog() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
    }
}
