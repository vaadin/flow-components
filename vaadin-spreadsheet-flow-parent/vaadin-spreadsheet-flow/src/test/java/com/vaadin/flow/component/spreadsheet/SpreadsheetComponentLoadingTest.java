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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The components and editors a factory gives the cells shown: in every pane,
 * kept while their cells are, given anew when the cells are reloaded, taken off
 * when the factory or the sheet goes, and the rows made tall enough for them
 * and given their heights back.
 */
class SpreadsheetComponentLoadingTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final Sheet sheet = workbook.createSheet("First");
    private Spreadsheet spreadsheet;

    /** The cells with components, and with editors, as "First!B12". */
    private final Set<String> components = new HashSet<>();
    private final Set<String> editors = new HashSet<>();

    private final SpreadsheetComponentFactory factory = new SpreadsheetComponentFactory() {
        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return components.contains(name(sheet, rowIndex, columnIndex))
                    ? new Span()
                    : null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return editors.contains(name(sheet, rowIndex, columnIndex))
                    ? new Span()
                    : null;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
        }
    };

    private static String name(Sheet sheet, int row, int column) {
        return sheet.getSheetName() + "!"
                + new CellReference(row, column).formatAsString();
    }

    /** Shows the sheet, as the client shows rows 10-20 of columns E-H. */
    private void show() {
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSpreadsheetComponentFactory(factory);
        ui.add(spreadsheet);
        showRows(10, 20);
    }

    private void showRows(int first, int last) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + first
                        + ", \"firstColumn\": 5, \"lastRow\": " + last
                        + ", \"lastColumn\": 8}");
    }

    private Component editorOf(String cell) {
        CellReference reference = new CellReference(cell);
        return spreadsheet.getCellKeyToEditorMap().get(SpreadsheetUtil
                .toKey(reference.getCol() + 1, reference.getRow() + 1));
    }

    /** The cells whose components the client has, by their keys. */
    private List<String> cellsWithComponents() {
        return spreadsheet.getComponentIDtoCellKeysMap().values().stream()
                .sorted().toList();
    }

    private static String key(String cell) {
        CellReference reference = new CellReference(cell);
        return SpreadsheetUtil.toKey(reference.getCol() + 1,
                reference.getRow() + 1);
    }

    private float heightOfRow(int row) {
        return spreadsheet.getRowH()[row - 1];
    }

    @Test
    void theComponentsOfEveryPane_areShown_eachKnownByItsCell() {
        sheet.createFreezePane(2, 2);
        components.addAll(Set.of("First!A1", "First!F1", "First!A12",
                "First!F12", "First!C5"));

        show();

        assertEquals(List.of(key("A1"), key("A12"), key("F1"), key("F12")),
                cellsWithComponents());
    }

    @Test
    void theCellsUnderAMergedCell_haveNoComponents_butTheMergedCellHas() {
        sheet.addMergedRegion(CellRangeAddress.valueOf("E12:G13"));
        components.addAll(
                Set.of("First!E12", "First!F12", "First!E13", "First!H12"));

        show();

        assertEquals(List.of(key("E12"), key("H12")), cellsWithComponents());
    }

    @Test
    void theFactory_isAskedOnlyOfCellsOfTheSheet_beforeTheClientSaysWhichItShows() {
        // The frozen corner is known; the rest is not.
        sheet.createFreezePane(2, 2);
        List<String> asked = new java.util.ArrayList<>();
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {
                    @Override
                    public Component getCustomComponentForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        asked.add(rowIndex + "," + columnIndex);
                        return null;
                    }

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return null;
                    }

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                    }
                });
        ui.add(spreadsheet);

        spreadsheet.reloadVisibleCellContents();

        assertEquals(List.of("0,0", "0,1", "1,0", "1,1"),
                asked.stream().sorted().toList());
    }

    @Test
    void reloadingTheCellsShown_asksTheFactoryForNewEditors() {
        editors.add("First!F12");
        show();
        Component editor = editorOf("F12");
        assertNotNull(editor);

        spreadsheet.reloadVisibleCellContents();

        assertNotSame(editor, editorOf("F12"));
    }

    @Test
    void anEditorOfAFrozenColumn_isKeptWhileItsRowIsShown() {
        sheet.createFreezePane(2, 0);
        editors.add("First!A12");
        show();
        Component editor = editorOf("A12");
        assertNotNull(editor);

        showRows(11, 21);

        assertSame(editor, editorOf("A12"));
    }

    @Test
    void takingTheFactoryAway_takesTheComponentsAndEditorsOff() {
        components.add("First!F12");
        editors.add("First!F13");
        show();

        spreadsheet.setSpreadsheetComponentFactory(null);

        assertTrue(spreadsheet.getComponentIDtoCellKeysMap().isEmpty());
        assertTrue(spreadsheet.getCellKeyToEditorMap().isEmpty());
        assertEquals(sheet.getDefaultRowHeightInPoints(), heightOfRow(12));
    }

    @Test
    void anotherSheetShown_takesTheComponentsOffBeforeItsOwnAreShown() {
        workbook.createSheet("Second");
        components.add("First!F12");
        editors.add("First!F13");
        show();
        Component editor = editorOf("F13");

        spreadsheet.setActiveSheetIndex(1);

        assertNull(editor.getElement().getParent());
        assertTrue(spreadsheet.getCellKeyToEditorMap().isEmpty());
    }

    @Test
    void aRow_getsTallEnoughForItsComponentOrEditor() {
        components.add("First!F12");
        editors.add("First!F14");

        show();

        int minimum = spreadsheet.getMinimumRowHeightForComponents();
        assertEquals(minimum, heightOfRow(12));
        assertEquals(minimum, heightOfRow(14));
    }

    @Test
    void aHiddenRow_staysHiddenWithAComponent() {
        sheet.createRow(11).setZeroHeight(true);
        components.add("First!F12");

        show();

        assertEquals(0f, heightOfRow(12));
    }

    @Test
    void aRowWhoseComponentGoes_getsItsHeightBack() {
        components.addAll(Set.of("First!F12", "First!F13", "First!F14"));
        sheet.createRow(12).setHeightInPoints(20);
        show();

        components.removeAll(Set.of("First!F12", "First!F13"));
        spreadsheet.reloadVisibleCellContents();

        assertEquals(sheet.getDefaultRowHeightInPoints(), heightOfRow(12));
        assertEquals(20f, heightOfRow(13));
        assertEquals(spreadsheet.getMinimumRowHeightForComponents(),
                heightOfRow(14));
    }

    @Test
    void aHiddenRowWhoseComponentGoes_staysHidden() {
        components.addAll(Set.of("First!F12", "First!F13"));
        show();
        spreadsheet.setRowHidden(11, true);

        components.remove("First!F12");
        spreadsheet.reloadVisibleCellContents();

        assertEquals(0f, heightOfRow(12));
    }

    @Test
    void theRowsOfTheComponents_keepTheirHeights_whenNoComponentIsShown() {
        components.add("First!F12");
        show();

        showRows(30, 40);

        assertEquals(spreadsheet.getMinimumRowHeightForComponents(),
                heightOfRow(12));
    }

    @Test
    void aComponentOnARowAddedPastTheLoadedOnes_isLetGoOfOnAnotherSheet() {
        workbook.createSheet("Second");
        components.addAll(Set.of("First!F501", "Second!F12"));
        show();
        spreadsheet.setMaxRows(1000);
        showRows(490, 510);
        assertEquals(spreadsheet.getMinimumRowHeightForComponents(),
                heightOfRow(501));

        spreadsheet.setActiveSheetIndex(1);
        showRows(10, 20);

        assertEquals(spreadsheet.getMinimumRowHeightForComponents(),
                heightOfRow(12));
    }

    @Test
    void aRowWithAComponent_keepsItsRoom_whenAnotherRowIsHidden() {
        components.add("First!F12");
        show();

        spreadsheet.setRowHidden(10, true);

        assertEquals(spreadsheet.getMinimumRowHeightForComponents(),
                heightOfRow(12));
    }

    @Test
    void aRowWithAComponent_keepsItsRoom_whenAnotherRowGetsAHeight() {
        components.add("First!F12");
        show();

        spreadsheet.setRowHeight(14, 30f);

        assertEquals(spreadsheet.getMinimumRowHeightForComponents(),
                heightOfRow(12));
        assertEquals(30f, heightOfRow(15));
    }

    @Test
    void aRowWithAComponent_givenLessHeightThanItNeeds_keepsItsRoom() {
        components.add("First!F12");
        show();

        spreadsheet.setRowHeight(11, 10f);

        assertEquals(spreadsheet.getMinimumRowHeightForComponents(),
                heightOfRow(12));
    }

    @Test
    void theRowsGrownOnASheet_areNotGrownOnTheNext() {
        workbook.createSheet("Second");
        components.add("First!F12");
        show();
        spreadsheet.setActiveSheetIndex(1);

        spreadsheet.setRowHidden(10, true);

        assertEquals(workbook.getSheet("Second").getDefaultRowHeightInPoints(),
                heightOfRow(12));
    }
}
