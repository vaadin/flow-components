package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.assertTrue;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-spreadsheet")
public class PopupButtonIT extends AbstractSpreadsheetIT {

    @Before
    public void init() throws Exception {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void popupButton_addAndShowListSelectPopup_PopupShownCorrectly() {
        loadTestFixture(TestFixtures.PopupButton);
        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class)
                .first();
        final SheetCellElement d1 = spreadsheetElement.getCellAt("D1");
        clickCell("D1");

        waitUntil(webDriver -> d1.hasPopupButton());

        assertTrue(spreadsheetElement.isPopupButtonPopupVisible());
    }

    @Test
    public void popupButton_addAndShowTablePopup_PopupShownCorrectly() {
        loadTestFixture(TestFixtures.TablePopupButton);
        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class)
                .first();
        final SheetCellElement d1 = spreadsheetElement.getCellAt("D1");
        clickCell("D1");

        waitUntil(webDriver -> d1.hasPopupButton());

        d1.popupButtonClick();

        assertTrue("PopupButton popup not visible",
                spreadsheetElement.isPopupButtonPopupVisible());
    }

    @Test
    public void popupButton_showPopupAndScroll_popupRemoved() {
        loadTestFixture(TestFixtures.PopupButton);
        final SpreadsheetElement spreadsheetElement = $(
                SpreadsheetElement.class).first();
        final SheetCellElement d1 = spreadsheetElement.getCellAt("D1");
        clickCell("D1");

        waitUntil(webDriver -> d1.hasPopupButton());

        spreadsheetElement.scroll(1000);

        waitUntil(webDriver -> !spreadsheetElement.isPopupButtonPopupVisible());
    }

    @Test
    public void popupButton_cellHasAPopupButtonAndFreezePaneIsAdded_theCellStillHasAPopupButton() {
        loadTestFixture(TestFixtures.PopupButton);
        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class)
                .first();
        final SheetCellElement d1 = spreadsheetElement.getCellAt("D1");
        clickCell("D1");
        waitUntil(webDriver -> d1.hasPopupButton());

        addFreezePane();

        assertTrue(spreadsheetElement.getCellAt("D1").hasPopupButton());
    }

    @Test
    public void popupButtonCellWidthWideText_changeValues_cellContainsPopupButton() {
        loadTestFixture(TestFixtures.PopupButton);

        final SpreadsheetElement spreadsheetElement = $(
                SpreadsheetElement.class).first();

        final SheetCellElement cell = spreadsheetElement.getCellAt("D1");

        // these actions trigger addition/removal of inner element,
        // which used to accidentally remove the popup button and other overlays

        insertValue_assertPopupButtonPresent(cell, "looooooooooooong text");

        insertValue_assertPopupButtonPresent(cell, "");
    }

    private void insertValue_assertPopupButtonPresent(SheetCellElement cell,
            String newValue) {

        cell.setValue(newValue);

        assertTrue(cell.hasPopupButton());
    }
}
