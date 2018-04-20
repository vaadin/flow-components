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

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

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
        executeScript("arguments[0].opened = false", findElement(By.id("dialog")));
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
}
