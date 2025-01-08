/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-spreadsheet/new-spreadsheet-edit-page")
public class NewSpreadsheetEditIT extends AbstractComponentIT {

    SpreadsheetElement spreadsheetElement;

    @Before
    public void init() {
        open();
        spreadsheetElement = $(SpreadsheetElement.class).first();
    }

    @Test
    public void spreadsheetLoaded_cellEdited_valueIsCorrectlySet() {
        final var inputValue = "input";
        final var cellAddress = "B2";

        Assert.assertTrue(spreadsheetElement.isDisplayed());
        spreadsheetElement.getCellAt(cellAddress).setValue(inputValue);
        Assert.assertEquals(inputValue,
                spreadsheetElement.getCellAt(cellAddress).getText());
    }

    @Test
    public void spreadsheetLoaded_freezePaneCreated_cellMaintainWidth() {
        final var cellAddress = "F1";

        // The cell width shouldn't change after freeze pane is called
        final var expectedWidth = getCellWidth(cellAddress);
        findElement(By.id("freeze-pane-button")).click();
        final var actualWidth = getCellWidth(cellAddress);

        Assert.assertEquals(expectedWidth, actualWidth);
    }

    private String getCellWidth(String cellAddress) {
        return spreadsheetElement.getCellAt(cellAddress).getCssValue("width");
    }
}
