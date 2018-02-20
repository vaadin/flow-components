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

import static org.junit.Assert.assertTrue;

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

    static final String DIALOG_TAG = "vaadin-dialog-overlay";

    @Before
    public void init() {
        open();
        waitUntil(
                driver -> findElements(By.tagName("vaadin-dialog")).size() > 0);
    }

    @Test
    public void dialogWithOpenedChangeListener() {
        assertTrue("The open state of the dialog is false"
                .equals(findElement(By.id("message")).getText()));

        findElement(By.id("dialog-open")).click();
        checkDialogIsOpen();
        assertTrue("The open state of the dialog is true"
                .equals(findElement(By.id("message")).getText()));
        assertDialogContent(
                "There is a opened change listener for this dialog");
        executeScript("document.body.click()");
        checkDialogIsClose();
        assertTrue("The open state of the dialog is false"
                .equals(findElement(By.id("message")).getText()));
    }

    private void checkDialogIsClose() {
        waitForElementNotPresent(By.tagName(DIALOG_TAG));
    }

    private void checkDialogIsOpen() {
        waitForElementPresent(By.tagName(DIALOG_TAG));
    }

    private void assertDialogContent(String expected) {
        List<WebElement> dialogs = getDialogs();
        String content = dialogs.iterator().next().getText();
        Assert.assertThat(content, CoreMatchers.containsString(expected));
    }

    private List<WebElement> getDialogs() {
        return findElements(By.tagName(DIALOG_TAG));
    }
}
