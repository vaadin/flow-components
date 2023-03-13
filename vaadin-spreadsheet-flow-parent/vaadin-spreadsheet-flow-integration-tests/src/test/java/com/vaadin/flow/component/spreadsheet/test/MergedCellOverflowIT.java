package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;
import java.util.List;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-spreadsheet")
public class MergedCellOverflowIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        loadFile("merged_overflow.xlsx");
    }

    @Test
    public void overflow_spreadsheetWithMergedAndFormattedArea_noOverflowFromFirstCell()
            throws IOException {

        SpreadsheetElement spreadsheet = getSpreadsheet();

        String cellText = "This shouldn't overflow outside of merged area";

        SheetCellElement b2 = spreadsheet.getCellAt("B2");
        Assert.assertEquals(b2.getValue(), cellText);

        String cellSelector = String.format(".col%d.row%d.cell", 2, 2);
        List<WebElement> elements = findElementsInShadowRoot(
                By.cssSelector(cellSelector));
        TestBenchElement underlyingCell = null;
        for (WebElement element : elements) {
            if (b2.getWrappedElement().equals(element)) {
                continue;
            }
            underlyingCell = (TestBenchElement) element;
        }
        if (underlyingCell == null) {
            Assert.fail("underlying cell not found");
        }
        SheetCellElement cellElement = underlyingCell
                .wrap(SheetCellElement.class);

        Assert.assertEquals(cellElement.getValue(), cellText);

        String overFlow = cellElement.getCssValue("overflow");
        Assert.assertNotEquals(overFlow, "visible");
    }

}
