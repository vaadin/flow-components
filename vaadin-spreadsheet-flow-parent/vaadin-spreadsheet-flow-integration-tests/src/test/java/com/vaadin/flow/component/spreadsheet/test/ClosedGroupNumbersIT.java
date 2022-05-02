package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;

public class ClosedGroupNumbersIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        createNewSpreadsheet();
    }

    @Test
    public void expandGroup_spreadsheetWithClosedGroupThatContainsNumbers_noPlaceholder()
            throws IOException {

        loadFile("closed-group-with-numbers.xlsx");
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        WebElement rowGrouping = spreadsheet
                .findElement(By.cssSelector(".col-group-pane .grouping.plus"));
        rowGrouping.click();

        waitUntil(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".col-group-pane .grouping.minus")));

        SheetCellElement c2 = spreadsheet.getCellAt("C2");
        Assert.assertEquals("100", c2.getValue());
    }
}
