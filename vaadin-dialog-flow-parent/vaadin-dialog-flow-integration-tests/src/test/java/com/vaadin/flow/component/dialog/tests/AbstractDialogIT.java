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

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractDialogIT extends AbstractComponentIT {
    protected List<DialogElement> getDialogs() {
        return $(DialogElement.class).withAttribute("opened").all();
    }

    protected DialogElement getDialog() {
        List<DialogElement> dialogs = getDialogs();
        if (dialogs.isEmpty()) {
            Assert.fail("No dialogs found");
        }
        return dialogs.get(0);
    }

    protected void verifyNumberOfDialogs(int expected) {
        try {
            waitUntil(driver -> getDialogs().size() == expected);
        } catch (TimeoutException exception) {
            Assert.fail("Expected " + expected + " dialogs, but found "
                    + getDialogs().size());
        }
    }

    protected void verifyOpened() {
        waitForElementPresent(By.cssSelector("vaadin-dialog[opened]"));
    }

    protected void verifyClosed() {
        waitForElementNotPresent(By
                .cssSelector("vaadin-dialog[opened], vaadin-dialog[closing]"));
    }

    protected void verifyClosedAndRemoved() {
        waitForElementNotPresent(By.cssSelector("vaadin-dialog"));
    }

    protected TestBenchElement getOverlayComponent(DialogElement dialog) {
        // returns vaadin-dialog-overlay from vaadin-dialog shadow root, which
        // overlays the whole page
        return dialog.$("*").id("overlay");
    }

    protected TestBenchElement getOverlayPart(DialogElement dialog) {
        // returns the overlay part from vaadin-dialog-overlay shadow root,
        // which is effectively the visible dialog window
        return getOverlayComponent(dialog).$("*").id("overlay");
    }
}
