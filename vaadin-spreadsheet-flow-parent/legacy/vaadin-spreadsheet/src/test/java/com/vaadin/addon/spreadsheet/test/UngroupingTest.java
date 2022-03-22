package com.vaadin.addon.spreadsheet.test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertFalse;

public class UngroupingTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetPage spreadsheetPage;

    /**
     * Ticket 599#
     */
    @Test
    public void grouping_expandColumnGroup_groupingElementsHaveCorrectValues() throws Exception {
        spreadsheetPage = headerPage.loadFile("ungrouping_cellUpdating.xlsx", this);
        List<WebElement> groupings = spreadsheetPage.getGroupings();
        groupings.get(1).click();
        String cellValue = spreadsheetPage.getCellValue(3, 1);
        assertFalse(cellValue.contains("#"));
    }

}
