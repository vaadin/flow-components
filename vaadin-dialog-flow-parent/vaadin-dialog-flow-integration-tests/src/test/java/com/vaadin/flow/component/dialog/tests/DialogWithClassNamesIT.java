/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import static com.vaadin.flow.component.dialog.tests.DialogTestPageIT.DIALOG_OVERLAY_TAG;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog/dialog-class-names-test")
public class DialogWithClassNamesIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openDialog_overlayHasSameClassNames() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));
        DialogElement dialog = $(DialogElement.class).first();

        WebElement overlay = $(DIALOG_OVERLAY_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String dialogClassNames = dialog.getAttribute("class");

        Assert.assertEquals("custom", dialogClassNames);
        Assert.assertEquals("custom", overlayClassNames);
    }

    @Test
    public void openDialog_overlayChangeClassName() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        $(NativeButtonElement.class).id("add").click();

        DialogElement dialog = $(DialogElement.class).first();
        WebElement overlay = $(DIALOG_OVERLAY_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String dialogClassNames = dialog.getAttribute("class");

        Assert.assertEquals("custom added", dialogClassNames);
        Assert.assertEquals("custom added", overlayClassNames);
    }

    @Test
    public void openDialog_overlayNoClassNameAfterClearClassName() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        $(NativeButtonElement.class).id("clear").click();

        DialogElement dialog = $(DialogElement.class).first();
        WebElement overlay = $(DIALOG_OVERLAY_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String dialogClassNames = dialog.getAttribute("class");

        Assert.assertEquals("", dialogClassNames);
        Assert.assertEquals("", overlayClassNames);
    }

    @Test
    public void openDialog_overlayChagedClassNameAfterSecondOpening() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        $(NativeButtonElement.class).id("clear").click();
        $(NativeButtonElement.class).id("add").click();

        $(DIALOG_OVERLAY_TAG).first().click();

        $(NativeButtonElement.class).id("open").click();
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        DialogElement dialog = $(DialogElement.class).first();
        WebElement overlay = $(DIALOG_OVERLAY_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String dialogClassNames = dialog.getAttribute("class");

        Assert.assertEquals("added", dialogClassNames);
        Assert.assertEquals("added", overlayClassNames);
    }
}
