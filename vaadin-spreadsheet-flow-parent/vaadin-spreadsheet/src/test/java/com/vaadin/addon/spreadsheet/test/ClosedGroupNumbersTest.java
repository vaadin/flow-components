package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.By;

public class ClosedGroupNumbersTest extends AbstractSpreadsheetTestCase {

    @Test
    public void expandGroup_spreadsheetWithClosedGroupThatContainsNumbers_noPlaceholder()
            throws IOException {

        headerPage.loadFile("closed-group-with-numbers.xlsx", this);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        WebElement rowGrouping = spreadsheet.findElement(By
                .cssSelector(".col-group-pane .grouping.plus"));
        rowGrouping.click();

        waitForElementPresent(By.cssSelector(".col-group-pane .grouping.minus"));

        SheetCellElement c2 = spreadsheet.getCellAt("C2");
        assertThat(c2.getValue(), equalTo("100"));
    }
}
