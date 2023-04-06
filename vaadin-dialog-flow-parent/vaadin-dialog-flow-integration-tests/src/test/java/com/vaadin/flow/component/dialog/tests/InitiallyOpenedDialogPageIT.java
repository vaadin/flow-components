
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-dialog/initial-dialog-open")
public class InitiallyOpenedDialogPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openDialogDuringPageLoad() {
        waitForElementPresent(By.tagName(DialogTestPageIT.DIALOG_OVERLAY_TAG));
        WebElement overlay = findElement(
                By.tagName(DialogTestPageIT.DIALOG_OVERLAY_TAG));
        Assert.assertTrue(isElementPresent(By.id("nested-component")));
    }
}
