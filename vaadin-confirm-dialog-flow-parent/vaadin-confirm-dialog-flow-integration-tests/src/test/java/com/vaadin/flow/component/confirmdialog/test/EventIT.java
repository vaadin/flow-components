package com.vaadin.flow.component.confirmdialog.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-confirm-dialog/events")
public class EventIT extends AbstractComponentIT {

    static final String DIALOG_OVERLAY_TAG = "vaadin-confirm-dialog-overlay";

    private ButtonElement openDialogBtn;
    private ButtonElement toggleCloseOnEscBtn;

    @Before
    public void init() {
        open();
        openDialogBtn = $(ButtonElement.class).id("open-dialog");
        toggleCloseOnEscBtn = $(ButtonElement.class).id("toggle-close-on-esc");
    }

    @Test
    public void openDialog_closeOnEsc() {
        openDialogBtn.click();
        checkDialogIsOpened();

        closeDialogByEscKey();

        checkDialogIsClosed();

        Assert.assertFalse("Dialog must be closed after esc key",
                isElementPresent(By.tagName(DIALOG_OVERLAY_TAG)));
    }

    @Test
    public void openDialog_closeOnEscIsDisallowed() {
        openDialogBtn.click();
        checkDialogIsOpened();

        toggleCloseOnEscBtn.click();

        closeDialogByEscKey();

        Assert.assertTrue("Dialog must be open after esc key",
                isElementPresent(By.tagName(DIALOG_OVERLAY_TAG)));
    }

    @Test
    public void testCloseOnEscDialog_closeOnEscIsRestored() {
        openDialogBtn.click();
        checkDialogIsOpened();

        toggleCloseOnEscBtn.click();
        toggleCloseOnEscBtn.click();

        closeDialogByEscKey();

        checkDialogIsClosed();

        Assert.assertFalse("Dialog must be closed after esc key",
                isElementPresent(By.tagName(DIALOG_OVERLAY_TAG)));
    }

    private void checkDialogIsClosed() {
        waitForElementNotPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    private void checkDialogIsOpened() {
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    private void closeDialogByEscKey() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
    }
}
