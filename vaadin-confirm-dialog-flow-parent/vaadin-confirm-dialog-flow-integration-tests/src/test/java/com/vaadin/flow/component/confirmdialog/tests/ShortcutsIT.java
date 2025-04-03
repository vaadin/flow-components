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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-confirm-dialog/shortcuts")
public class ShortcutsIT extends AbstractComponentIT {

    private ButtonElement openDialog;
    private ButtonElement shortcutButton;

    @Before
    public void init() {
        open();
        openDialog = $(ButtonElement.class).id("open-dialog-button");
    }

    @Test
    public void clickShortcut_dialogClosed() {
        openDialog.click();

        waitForElementPresent(By.tagName("vaadin-confirm-dialog-overlay"));

        ConfirmDialogElement confirmDialog = getConfirmDialog();

        shortcutButton = confirmDialog.$(ButtonElement.class)
                .id("shortcut-button");
        shortcutButton.focus();
        shortcutButton.sendKeys("x");

        waitForElementNotPresent(By.tagName("vaadin-confirm-dialog-overlay"));
    }

    private ConfirmDialogElement getConfirmDialog() {
        return $(ConfirmDialogElement.class).waitForFirst();
    }
}
