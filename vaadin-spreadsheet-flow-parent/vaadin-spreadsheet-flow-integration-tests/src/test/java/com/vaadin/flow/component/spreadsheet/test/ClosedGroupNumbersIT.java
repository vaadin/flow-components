/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class ClosedGroupNumbersIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

        createNewSpreadsheet();
    }

    @Test
    public void expandGroup_spreadsheetWithClosedGroupThatContainsNumbers_noPlaceholder()
            throws IOException {

        loadFile("closed-group-with-numbers.xlsx");
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        WebElement rowGrouping = findElementInShadowRoot(
                By.cssSelector(".col-group-pane .grouping.plus"));
        rowGrouping.click();

        waitUntil(e -> findElementInShadowRoot(
                By.cssSelector(".col-group-pane .grouping.minus")) != null);

        SheetCellElement c2 = spreadsheet.getCellAt("C2");
        Assert.assertEquals("100", c2.getValue());
    }
}
