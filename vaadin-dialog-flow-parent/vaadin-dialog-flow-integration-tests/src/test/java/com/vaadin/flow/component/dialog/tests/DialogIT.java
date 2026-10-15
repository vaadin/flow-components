/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog-view")
public class DialogIT extends AbstractComponentIT {

    private static final String DIALOG_OVERLAY_TAG = "vaadin-dialog-overlay";

    @Test
    public void openAndCloseConfirmationDialog_buttonsRenderedWithClickListeners() {
        open();

        WebElement messageLabel = findElement(
                By.id("confirmation-dialog-label"));

        findElement(By.id("confirmation-dialog-button")).click();
        getOverlayContent().findElements(By.tagName("vaadin-button")).get(0)
                .click();
        verifyDialogClosed();
        Assert.assertEquals("Confirmed!", messageLabel.getText());

        findElement(By.id("confirmation-dialog-button")).click();
        getOverlayContent().findElements(By.tagName("vaadin-button")).get(1)
                .click();
        verifyDialogClosed();
        Assert.assertEquals("Cancelled...", messageLabel.getText());
    }

    @Test
    public void validateClosingFromServerSide() {
        open();

        findElement(By.id("server-side-close-dialog-button")).click();
        verifyDialogOpened();

        executeScript("document.body.click()");
        verifyDialogOpened();

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        verifyDialogClosed();

        Assert.assertEquals("Closed from server-side",
                findElement(By.id("server-side-close-dialog-label")).getText());
    }

    @Test
    public void focusElementOnOpen() {
        open();

        findElement(By.id("focus-dialog-button")).click();

        WebElement element = getOverlayContent()
                .findElement(By.tagName("input"));

        Assert.assertTrue(element.equals(driver.switchTo().activeElement()));
    }

    @Test
    public void styleDialogContent() {
        open();

        scrollIntoViewAndClick(
                findElement(By.id("styled-content-dialog-button")));

        WebElement element = getOverlayContent()
                .findElement(By.className("my-style"));

        Assert.assertEquals("rgba(255, 0, 0, 1)", element.getCssValue("color"));
    }

    private TestBenchElement getOverlayContent() {
        return $(DIALOG_OVERLAY_TAG).first();
    }

    private void verifyDialogClosed() {
        waitForElementNotPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    private void verifyDialogOpened() {
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-dialog-view");
    }
}
