package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.component.html.testbench.DivElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-spreadsheet/sizing")
public class SizingIT extends AbstractComponentIT {

    private SpreadsheetElement spreadsheet;
    private WebElement layout;

    @Before
    public void init() {
        open();
        spreadsheet = $(SpreadsheetElement.class).first();
        layout = findElement(By.id("layout"));
        waitUntil(e -> spreadsheet.$(DivElement.class).exists());
    }

    @Test
    public void spreadsheetHeightDefault_layoutHeightDefault() {
        assertSpreadsheetHeight(100);
        assertLayoutHeight(100);
    }

    @Test
    public void spreadsheetHeightDefault_layoutHeight200() {
        findElement(By.id("layoutHeight200")).click();

        assertSpreadsheetHeight(200);
        assertLayoutHeight(200);
    }

    @Test
    public void spreadsheetHeightDefault_layoutHeight600() {
        findElement(By.id("layoutHeight600")).click();

        assertSpreadsheetHeight(600);
        assertLayoutHeight(600);
    }

    @Test
    public void spreadsheetHeight200_layoutHeightDefault() {
        findElement(By.id("spreadsheetHeight200")).click();

        assertSpreadsheetHeight(200);
        assertLayoutHeight(200);
    }

    @Test
    public void spreadsheetHeight600_layoutHeightDefault() {
        findElement(By.id("spreadsheetHeight600")).click();

        assertSpreadsheetHeight(600);
        assertLayoutHeight(600);
    }

    @Test
    public void spreadsheetHeight200_layoutHeight600() {
        findElement(By.id("spreadsheetHeight200")).click();
        findElement(By.id("layoutHeight600")).click();

        assertSpreadsheetHeight(200);
        assertLayoutHeight(600);
    }

    @Test
    public void spreadsheetHeight600_layoutHeight200() {
        findElement(By.id("spreadsheetHeight600")).click();
        findElement(By.id("layoutHeight200")).click();

        assertSpreadsheetHeight(600);
        assertLayoutHeight(200);
    }

    @Test
    public void spreadsheetHeightDefault_layoutHeightDefault_layoutDisplayFlex() {
        findElement(By.id("layoutDisplayFlex")).click();

        var layoutWidth = layout.getSize().getWidth();
        var internal = spreadsheet.$(DivElement.class).first();
        Assert.assertEquals(layoutWidth, internal.getSize().getWidth());
        Assert.assertEquals(layoutWidth, spreadsheet.getSize().getWidth());
    }

    @Test
    public void layoutFlexColumnStart_spreadsheetFullWidth() {
        findElement(By.id("layoutDisplayFlex")).click();
        findElement(By.id("layoutFlexColumnStart")).click();

        var layoutWidth = layout.getSize().getWidth();
        var internal = spreadsheet.$(DivElement.class).first();
        Assert.assertEquals(layoutWidth, internal.getSize().getWidth());
        Assert.assertEquals(layoutWidth, spreadsheet.getSize().getWidth());
    }

    @Test
    public void toggleSpreadsheetAttached_noMissingRows() {
        // Detach spreadsheet
        findElement(By.id("spreadsheetAttachedToggle")).click();
        // Re-attach spreadsheet
        findElement(By.id("spreadsheetAttachedToggle")).click();
        // Get reference to the new spreadsheet
        var spreadsheet = $(SpreadsheetElement.class).first();

        // Increase the spreadsheet height
        findElement(By.id("spreadsheetHeight600")).click();

        // New rows should have been added on resize
        waitUntil(e -> spreadsheet.getCellAt("A20") != null);
    }

    private void assertSpreadsheetHeight(int height) {
        var internal = spreadsheet.$(DivElement.class).first();
        Assert.assertEquals(height, internal.getSize().getHeight());
        Assert.assertEquals(height, spreadsheet.getSize().getHeight());
    }

    private void assertLayoutHeight(int height) {
        Assert.assertEquals(height, layout.getSize().getHeight());
    }
}
