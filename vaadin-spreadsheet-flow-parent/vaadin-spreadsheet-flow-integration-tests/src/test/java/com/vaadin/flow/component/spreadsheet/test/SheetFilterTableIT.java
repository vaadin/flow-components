/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class SheetFilterTableIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void filter_changeSheet_hideFilter() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        loadTestFixture(TestFixtures.SpreadsheetTable);
        spreadsheet.addSheet();
        spreadsheet.selectSheetAt(1);
        assertFalse("Cell B2 should not have a filter",
                spreadsheet.getCellAt(2, 2).hasPopupButton());
    }

    @Test
    public void filter_removeTable_hideFilter() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        loadTestFixture(TestFixtures.SpreadsheetTable);
        final SheetCellElement cell = spreadsheet.getCellAt("B2");
        cell.contextClick();
        spreadsheet.getContextMenu().getItem("Delete Table B2:F6").click();
        waitUntil(arg0 -> !cell.hasPopupButton());
    }

    @Test
    public void sheetWithFilterTable_rowIsRemoved_filterOptionsAvailable() {
        loadTestFixture(TestFixtures.SpreadsheetTable);
        final var cell = getSpreadsheet().getCellAt("B2");

        assertSelectAll(cell);

        contextClickOnRowHeader(4);
        clickItem("Delete row 4");

        assertSelectAll(cell);
    }

    @Test
    public void filterColumn_otherColumnOmitsValuesOfHiddenRows() {
        loadTestFixture(TestFixtures.SpreadsheetTable);
        final SpreadsheetElement spreadsheet = getSpreadsheet();

        // Before filtering, the first column offers all of its values
        spreadsheet.getCellAt("B2").popupButtonClick();
        Assert.assertEquals(
                List.of("Cell 1:0", "Cell 2:0", "Cell 3:0", "Cell 4:0"),
                getFilterPopup().getOptions());
        closeFilterPopup();

        // Filter the second column so that row 1 gets hidden
        spreadsheet.getCellAt("C2").popupButtonClick();
        getFilterPopup().deselectByText("Cell 1:1");
        closeFilterPopup();

        // The first column no longer offers "Cell 1:0", as its row is hidden
        // by the second column
        spreadsheet.getCellAt("B2").popupButtonClick();
        Assert.assertEquals(List.of("Cell 2:0", "Cell 3:0", "Cell 4:0"),
                getFilterPopup().getOptions());
    }

    @Test
    public void filterTwoColumns_eachColumnRetainsOwnFilteredValues() {
        loadTestFixture(TestFixtures.SpreadsheetTable);
        final SpreadsheetElement spreadsheet = getSpreadsheet();

        // Filter the first column so that row 1 gets hidden
        spreadsheet.getCellAt("B2").popupButtonClick();
        getFilterPopup().deselectByText("Cell 1:0");
        closeFilterPopup();

        // Filter the second column so that row 2 gets hidden
        spreadsheet.getCellAt("C2").popupButtonClick();
        getFilterPopup().deselectByText("Cell 2:1");
        closeFilterPopup();

        // The first column still offers "Cell 1:0" as an unchecked option, so
        // its hidden row can be brought back independently of the second
        // column's filter. Row 2, hidden by the second column, is excluded.
        spreadsheet.getCellAt("B2").popupButtonClick();
        Assert.assertEquals(List.of("Cell 1:0", "Cell 3:0", "Cell 4:0"),
                getFilterPopup().getOptions());
        Assert.assertEquals(List.of("Cell 3:0", "Cell 4:0"),
                getFilterPopup().getSelectedTexts());
    }

    private void assertSelectAll(SheetCellElement cell) {
        cell.popupButtonClick();
        Assert.assertTrue(hasOption("(Select All)"));
    }

    private CheckboxGroupElement getFilterPopup() {
        return $(CheckboxGroupElement.class).single();
    }

    private void closeFilterPopup() {
        findElement(By.className("v-window-closebox")).click();
        // The overlay fades out, so wait until it is actually gone before
        // opening the next one, otherwise single() would match two overlays.
        waitUntil(driver -> $(CheckboxGroupElement.class).all().isEmpty());
    }
}
