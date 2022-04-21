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

    private ButtonElement openDialogBtn;
    private ButtonElement openEscDialogBtn;
    private ButtonElement openNoEscDialogBtn;

    @Before
    public void init() {
        open();
        openDialogBtn = $(ButtonElement.class).id("open-dialog");
        openEscDialogBtn = $(ButtonElement.class).id("open-esc-dialog");
        openNoEscDialogBtn = $(ButtonElement.class).id("open-no-esc-dialog");
    }

    @Test
    public void testRegularDialog_closeOnEsc() {
        openDialogBtn.click();
        waitForElementPresent(By.tagName("vaadin-confirm-dialog-overlay"));

        closeDialogByEscKey();

        waitForElementNotPresent(By.tagName("vaadin-confirm-dialog-overlay"));

        Assert.assertFalse("Dialog must be closed after esc key",
                isElementPresent(By.tagName("vaadin-confirm-dialog-overlay")));
    }

    @Test
    public void testCloseOnEscDialog_closeOnEsc() {
        openEscDialogBtn.click();
        waitForElementPresent(By.tagName("vaadin-confirm-dialog-overlay"));

        closeDialogByEscKey();

        waitForElementNotPresent(By.tagName("vaadin-confirm-dialog-overlay"));

        Assert.assertFalse("Dialog must be closed after esc key",
                isElementPresent(By.tagName("vaadin-confirm-dialog-overlay")));
    }

    @Test
    public void testNoCloseOnEscDialog_NoCloseOnEsc() {
        openNoEscDialogBtn.click();
        waitForElementPresent(By.tagName("vaadin-confirm-dialog-overlay"));

        closeDialogByEscKey();

        Assert.assertTrue("Dialog must be open after esc key",
                isElementPresent(By.tagName("vaadin-confirm-dialog-overlay")));
    }

    private void closeDialogByEscKey() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
    }
}
