package com.vaadin.flow.component.spreadsheet.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
        assertThat(a2.getValue(), equalTo(cellText));

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

        assertThat(cellElement.getValue(), equalTo(cellText));

        String overFlow = cellElement.getCssValue("overflow");
        assertThat(overFlow, not(equalTo("visible")));
    }

}
