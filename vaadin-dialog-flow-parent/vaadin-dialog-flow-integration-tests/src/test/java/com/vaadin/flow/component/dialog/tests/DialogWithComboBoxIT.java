
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-dialog/dialog-with-combo")
public class DialogWithComboBoxIT extends AbstractComponentIT {

    @Test
    public void openOverlayUsingKeybaord_overlayIsShown() {
        open();

        waitForElementPresent(By.id("open-dialog"));
        findElement(By.id("open-dialog")).click();

        WebElement combo = findElement(By.id("combo"));
        combo.sendKeys(Keys.ARROW_DOWN);

        WebElement info = $("div").id("info");
        waitUntil(driver -> info.getText().equals(Boolean.TRUE.toString()));

        Assert.assertTrue(findElement(By.tagName("vaadin-combo-box-overlay"))
                .isDisplayed());
    }

    @Test
    public void openOverlayUsingMouse_overlayIsShown() {
        open();

        findElement(By.id("open-dialog")).click();

        TestBenchElement combo = $("*").id("combo");
        combo.$("*").id("toggleButton").click();

        WebElement info = $("div").id("info");
        waitUntil(driver -> info.getText().equals(Boolean.TRUE.toString()));

        Assert.assertTrue(findElement(By.tagName("vaadin-combo-box-overlay"))
                .isDisplayed());
    }

}
