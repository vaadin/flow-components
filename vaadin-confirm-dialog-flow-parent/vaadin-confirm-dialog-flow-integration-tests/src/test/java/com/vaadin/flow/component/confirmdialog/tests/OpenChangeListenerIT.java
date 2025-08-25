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
package com.vaadin.flow.component.confirmdialog.tests;

import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-confirm-dialog/open-change")
public class OpenChangeListenerIT extends AbstractComponentIT {

    private ConfirmDialogElement dialog;


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
        dialog = $(ConfirmDialogElement.class).first();

        Assert.assertEquals("The open state of the dialog is true",
                message.getText());
        Assert.assertEquals("There should one event from opening the dialog",
                "Number of events is 1", eventCounterMessage.getText());
        Assert.assertEquals("The event came from server",
                eventSourceMessage.getText());

        Assert.assertEquals("There is an opened change listener for this dialog", dialog.getMessageText());

        dialog.getCancelButton().click();
        verifyClosed();
        Assert.assertEquals("The open state of the dialog is false",
                message.getText());
        Assert.assertEquals("Number of events is 2",
                eventCounterMessage.getText());
        Assert.assertEquals("The event came from client",
                eventSourceMessage.getText());

        findElement(By.id("dialog-open")).click();
        verifyOpened();
        dialog = $(ConfirmDialogElement.class).first();

        executeScript("arguments[0].opened = false",
                findElement(By.id("dialog")));
        Assert.assertEquals("The event came from client",
                eventSourceMessage.getText());
    }

    protected void verifyOpened() {
        waitForElementPresent(By.cssSelector("vaadin-confirm-dialog[opened]"));
    }

    protected void verifyClosed() {
        waitForElementNotPresent(By.cssSelector("vaadin-confirm-dialog[opened], vaadin-confirm-dialog[closing]"));
    }


}
