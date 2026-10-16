/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * Tables registered with a spreadsheet: deleted, reloaded when their sheet is
 * shown again, and forgotten with the workbook.
 */
class SpreadsheetTablesTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.createNewSheet("Second", 10, 10);
        spreadsheet.createNewSheet("Third", 10, 10);
        spreadsheet.setActiveSheetIndex(0);
        for (int column = 1; column <= 3; column++) {
            spreadsheet.createCell(1, column, "Column " + column);
            for (int row = 2; row <= 4; row++) {
                spreadsheet.createCell(row, column, row * 10 + column);
            }
        }
        ui.add(spreadsheet);
    }

    /** A filter table over B2:D5 of the sheet shown, registered. */
    private SpreadsheetFilterTable filterTable() {
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(spreadsheet,
                new CellRangeAddress(1, 4, 1, 3));
        spreadsheet.registerTable(table);
        return table;
    }

    private boolean shown(PopupButton button) {
        return spreadsheet.getElement().equals(button.getElement().getParent());
    }

    /** As the client reports where it is, which loads the sheet's tables. */
    private void clientReportsWhereItIs() {
        clientReportsRows(1, 20);
    }

    private void clientReportsRows(int first, int last) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + first
                        + ", \"firstColumn\": 1, \"lastRow\": " + last
                        + ", \"lastColumn\": 10}");
    }

    /** Shows another sheet and comes back to the first. */
    private void showAnotherSheetAndBack(int other) {
        spreadsheet.setActiveSheetIndex(other);
        clientReportsWhereItIs();
        spreadsheet.setActiveSheetIndex(0);
        clientReportsWhereItIs();
    }

    @Test
    void deletingATable_takesItsPopupButtonsOffTheSheet_andForgetsIt() {
        SpreadsheetFilterTable table = filterTable();
        clientReportsWhereItIs();
        List<PopupButton> buttons = List.copyOf(table.getPopupButtons());
        assertEquals(3, buttons.size());
        assertTrue(buttons.stream().allMatch(this::shown));

        spreadsheet.deleteTable(table);

        assertTrue(buttons.stream().noneMatch(this::shown));
        assertTrue(table.getPopupButtons().isEmpty());
        assertTrue(spreadsheet.getTables().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deletingAFilterTable_showsTheRowsItsFiltersHid() {
        SpreadsheetFilterTable table = filterTable();
        ItemFilter filter = (ItemFilter) table.getPopupButton(1).getContent()
                .getChildren().findFirst().get();
        CheckboxGroup<String> values = (CheckboxGroup<String>) filter
                .getChildren()
                .filter(component -> component instanceof CheckboxGroup)
                .findFirst().get();
        values.deselect("31");
        assertTrue(spreadsheet.isRowHidden(3));

        spreadsheet.deleteTable(table);

        assertFalse(spreadsheet.isRowHidden(3));
    }

    @Test
    void deletingATableOfASheetNotShown_onlyForgetsIt() {
        SpreadsheetFilterTable table = filterTable();
        spreadsheet.setActiveSheetIndex(1);

        spreadsheet.deleteTable(table);

        assertEquals(3, table.getPopupButtons().size());
        assertTrue(spreadsheet.getTables().isEmpty());
    }

    @Test
    void aTable_putsItsPopupButtonsBackWhenItsSheetIsShownAgain() {
        SpreadsheetFilterTable table = filterTable();
        clientReportsWhereItIs();
        List<PopupButton> buttons = List.copyOf(table.getPopupButtons());
        spreadsheet.setActiveSheetIndex(1);
        assertTrue(buttons.stream().noneMatch(this::shown));

        spreadsheet.setActiveSheetIndex(0);
        clientReportsWhereItIs();

        assertTrue(buttons.stream().allMatch(this::shown));
    }

    /** A table that counts its reloads. */
    private static class CountingTable extends SpreadsheetTable {
        int reloads;

        CountingTable(Spreadsheet spreadsheet, Sheet sheet) {
            super(spreadsheet, sheet, new CellRangeAddress(1, 4, 1, 3));
        }

        @Override
        public void reload() {
            reloads++;
            super.reload();
        }
    }

    @Test
    void aTable_isReloadedOnceEachTimeItsSheetIsShown() {
        CountingTable table = new CountingTable(spreadsheet,
                spreadsheet.getActiveSheet());
        spreadsheet.registerTable(table);

        showAnotherSheetAndBack(1);
        // Scrolled: the cells load again, the tables do not.
        clientReportsRows(5, 25);

        assertEquals(1, table.reloads);
    }

    @Test
    void aTableOfAnotherSheet_isNotReloaded() {
        CountingTable table = new CountingTable(spreadsheet,
                spreadsheet.getWorkbook().getSheetAt(1));
        spreadsheet.registerTable(table);

        showAnotherSheetAndBack(2);

        assertEquals(0, table.reloads);
    }

    @Test
    void theTablesOfTheShownSheet_areThoseOfThatSheet() {
        SpreadsheetFilterTable shown = filterTable();
        spreadsheet.registerTable(new CountingTable(spreadsheet,
                spreadsheet.getWorkbook().getSheetAt(1)));

        assertEquals(List.of(shown), spreadsheet.getTablesForActiveSheet());
        assertEquals(2, spreadsheet.getTables().size());
    }

    @Test
    void anotherWorkbook_forgetsTheTables() {
        filterTable();

        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        spreadsheet.setWorkbook(workbook);

        assertTrue(spreadsheet.getTables().isEmpty());
    }
}
