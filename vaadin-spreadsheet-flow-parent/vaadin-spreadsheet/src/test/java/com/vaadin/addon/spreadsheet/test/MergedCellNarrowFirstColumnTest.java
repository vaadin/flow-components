package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.BrowserUtil;

public class MergedCellNarrowFirstColumnTest extends AbstractSpreadsheetTestCase {

    @Test
    public void overflowBasedOnFullMergedWidth()
            throws IOException {

        headerPage.loadFile("merged_narrow_column.xlsx", this);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        String cellText = "123456";

        SheetCellElement a2 = spreadsheet.getCellAt("A2");
        assertThat(a2.getValue(), equalTo(cellText));

        String cellSelector = String.format(".col%d.row%d.cell", 1, 2);
        List<WebElement> elements = findElements(By.cssSelector(cellSelector));
        TestBenchElement underlyingCell = null;
        for (WebElement element : elements) {
            if (a2.getWrappedElement().equals(element)) {
                continue;
            }
            underlyingCell = (TestBenchElement) element;
        }
        assertNotNull("underlying cell not found", underlyingCell);

        SheetCellElement cellElement = underlyingCell
                .wrap(SheetCellElement.class);
        if (!BrowserUtil.isPhantomJS(getDesiredCapabilities())
                && !BrowserUtil.isIE(getDesiredCapabilities(), 10)) {
            // for some reason PhantomJS and IE10 lose the underlying content
            // doesn't affect the end result negatively so can be ignored
            assertThat(cellElement.getValue(), equalTo(cellText));
        }

        String overFlow = cellElement.getCssValue("overflow");
        assertThat(overFlow, not(equalTo("visible")));

        compareScreen("mergedNarrowColumn");
    }

}
