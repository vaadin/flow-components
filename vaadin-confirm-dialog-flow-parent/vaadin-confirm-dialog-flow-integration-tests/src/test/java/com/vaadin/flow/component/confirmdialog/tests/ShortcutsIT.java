package com.vaadin.flow.component.confirmdialog.tests;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

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
