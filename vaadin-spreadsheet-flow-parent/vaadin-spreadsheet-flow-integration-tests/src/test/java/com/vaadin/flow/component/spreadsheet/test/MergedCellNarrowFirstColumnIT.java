/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-spreadsheet")
public class MergedCellNarrowFirstColumnIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void overflowBasedOnFullMergedWidth() {

        loadFile("merged_narrow_column.xlsx");
        var spreadsheet = getSpreadsheet();

        String cellText = "123456";

        var a2 = spreadsheet.getCellAt("A2");
        Assert.assertEquals(cellText, a2.getValue());

        String cellSelector = String.format(".col%d.row%d.cell", 1, 2);
        List<WebElement> elements = findElementsInShadowRoot(
                By.cssSelector(cellSelector));
        TestBenchElement underlyingCell = null;
        for (WebElement element : elements) {
            if (a2.getWrappedElement().equals(element)) {
                continue;
            }
            underlyingCell = (TestBenchElement) element;
        }
        assertNotNull("underlying cell not found", underlyingCell);

        var cellElement = underlyingCell.wrap(SheetCellElement.class);

        Assert.assertEquals(cellText, cellElement.getValue());

        String overFlow = cellElement.getCssValue("overflow");
        Assert.assertNotEquals("visible", overFlow);
    }

}
