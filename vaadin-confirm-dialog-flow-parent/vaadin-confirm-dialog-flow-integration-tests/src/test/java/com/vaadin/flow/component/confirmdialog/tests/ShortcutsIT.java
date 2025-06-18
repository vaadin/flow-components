/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
