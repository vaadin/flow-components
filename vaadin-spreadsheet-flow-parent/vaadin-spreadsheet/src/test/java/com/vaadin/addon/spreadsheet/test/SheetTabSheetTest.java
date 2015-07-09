package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

/**
 * Test for sheet SheetTabSheet navigation.
 *
 */
public class SheetTabSheetTest extends AbstractSpreadsheetTestCase {

    SpreadsheetPage spreadsheetPage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        spreadsheetPage = headerPage.createNewSpreadsheet();
    }

    @Test
    public void focus_createTab_sheetIsFocused() {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        SheetCellElement cell = spreadsheet.getCellAt("A1");
        // Force sheet initial focus
        cell.click();
        verifySheetFocused();
        spreadsheet.addSheet("");
        verifySheetFocused();
    }

    @Test
    public void focus_changeTab_sheetIsFocused() {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        SheetCellElement cell = spreadsheet.getCellAt("A1");
        // Force sheet initial focus
        cell.click();
        verifySheetFocused();
        spreadsheet.addSheet("2");
        spreadsheet.addSheet("3");
        verifySheetFocused();
        loadSheet(0);
        verifySheetFocused();
        loadSheet(1);
        verifySheetFocused();
    }

    private void loadSheet(int index) {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.findElements(By.className("sheet-tabsheet-tab")).get(index)
                .click();

    }

    private void verifySheetFocused() {
        assertThat("Sheet lost focus", getFocusedElement()
                .getAttribute("class"), containsString("bottom-right-pane"));
    }
}
