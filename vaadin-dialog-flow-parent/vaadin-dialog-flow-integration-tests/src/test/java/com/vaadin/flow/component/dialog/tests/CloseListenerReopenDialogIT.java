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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-dialog/close-listener-reopen-dialog")
public class CloseListenerReopenDialogIT extends AbstractDialogIT {

    @Test
    public void reopenDialog_closeActionListenerIsCalled() {
        open();

        waitForElementPresent(By.id("open"));
        findElement(By.id("open")).click();
        verifyOpened();

        // try to close dialog
        closeDialog();

        Assert.assertTrue("Close dialog is not handled on the server side",
                isElementPresent(By.className("main")));

        // close dialog via button
        $("button").id("close").click();
        verifyClosedAndRemoved();

        // reopen
        findElement(By.id("open")).click();

        // try to close dialog
        closeDialog();

        Assert.assertEquals(
                "Close dialog after reopen is not handled on the server side",
                2, findElements(By.className("main")).size());
    }

    @Test
    public void reopenDialog_removeCloseListener_dialogIsClosed() {
        open();

        findElement(By.id("open")).click();
        verifyOpened();

        // try to close dialog
        closeDialog();

        // close dialog via button
        $("button").id("close").click();
        verifyClosedAndRemoved();

        // remove close listener
        findElement(By.id("remove")).click();

        // reopen
        findElement(By.id("open")).click();

        // try to close dialog
        closeDialog();
        verifyClosedAndRemoved();
    }

    @Test
    public void openSubDialog_closeListenerIsCalled() {
        open();

        findElement(By.id("open")).click();
        verifyOpened();

        findElement(By.id("open-sub")).click();

        verifyNumberOfDialogs(2);

        closeDialog();

        verifyNumberOfDialogs(1);

        // close action listener prints its info message
        Assert.assertEquals(
                "No expected info message from close action listener for the subdialog",
                1, findElements(By.className("sub")).size());
    }

    @Test
    public void openDialog_shouldNotThrow() {
        open();

        findElement(By.id("open")).click();

        checkLogsForErrors();
    }

    private void closeDialog() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
    }
}
