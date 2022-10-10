package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-spreadsheet/new-spreadsheet-edit-page")
public class NewSpreadsheetEditIT extends AbstractComponentIT {

    @Test
    public void spreadsheetLoaded_cellEdited_valueIsCorrectlySet() {
        open();
        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class)
                .first();
        Assert.assertTrue(spreadsheetElement.isDisplayed());
        String inputValue = "input";
        String cellAddress = "B2";
        spreadsheetElement.getCellAt(cellAddress).setValue(inputValue);
        Assert.assertEquals(inputValue,
                spreadsheetElement.getCellAt(cellAddress).getText());
    }
}
