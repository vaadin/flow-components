/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog/sub-dialog-opened-on-opened-change")
public class SubDialogOpenedOnOpenedChangeIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openMainDialog_openSubDialog_mainDialogGetsDetached() {
        waitForElementPresent(By.id("open-main-dialog"));
        findElement(By.id("open-main-dialog")).click();

        waitForElementPresent(By.id("close-main-dialog-and-open-sub-dialog"));
        findElement(By.id("close-main-dialog-and-open-sub-dialog")).click();

        WebElement output = findElement(By.id("output"));
        Assert.assertEquals("Detached", output.getText());
    }
}
