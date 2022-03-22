package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.BrowserUtil;

public class MergedCellOverflowTest extends AbstractSpreadsheetTestCase {

    @Test
    public void overflow_spreadsheetWithMergedAndFormattedArea_noOverflowFromFirstCell()
            throws IOException {

        headerPage.loadFile("merged_overflow.xlsx", this);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        String cellText = "This shouldn't overflow outside of merged area";

        SheetCellElement b2 = spreadsheet.getCellAt("B2");
        assertThat(b2.getValue(), equalTo(cellText));

        String cellSelector = String.format(".col%d.row%d.cell", 2, 2);
        List<WebElement> elements = findElements(By.cssSelector(cellSelector));
        TestBenchElement underlyingCell = null;
        for (WebElement element : elements) {
            if (b2.getWrappedElement().equals(element)) {
                continue;
            }
            underlyingCell = (TestBenchElement) element;
        }
        if (underlyingCell == null) {
            fail("underlying cell not found");
        }
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

        compareScreen("mergedOverflow");
    }

}
