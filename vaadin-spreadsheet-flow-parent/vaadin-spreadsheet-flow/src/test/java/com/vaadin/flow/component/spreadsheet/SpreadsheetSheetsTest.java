/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SheetChangeEvent;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The sheets of a workbook: shown by their tabs, which leave out the hidden
 * ones, chosen, added and renamed on the client, created, deleted, renamed,
 * hidden and protected.
 */
class SpreadsheetSheetsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private Spreadsheet spreadsheet;

    private void sheets(String... names) {
        for (String name : names) {
            workbook.createSheet(name);
        }
    }

    private void hide(String name, SheetVisibility visibility) {
        workbook.setSheetVisibility(workbook.getSheetIndex(name), visibility);
    }

    /** Shows the workbook, as the client reports rows 1-20 of columns A-J. */
    private void show() {
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        clientShowsRows();
    }

    private void clientShowsRows() {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20, \"lastColumn\": 10}");
    }

    private String shown() {
        return spreadsheet.getActiveSheet().getSheetName();
    }

    private List<String> tabs() {
        return List.of(spreadsheet.getSheetNames());
    }

    private List<String> sheetNames() {
        List<String> names = new ArrayList<>();
        for (Sheet sheet : workbook) {
            names.add(sheet.getSheetName());
        }
        return names;
    }

    private static void assertRefused(String message, Executable call) {
        assertEquals(message, assertThrows(IllegalArgumentException.class, call)
                .getMessage());
    }

    /** Whether the client was called the given function since the last look. */
    private boolean clientCalled(String function) {
        ui.fakeClientCommunication();
        boolean called = false;
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            called |= invocation.getInvocation().getExpression()
                    .contains(function)
                    || invocation.getInvocation().getParameters()
                            .contains(function);
        }
        return called;
    }

    @Test
    void aTabOutOfRange_isRefused() {
        sheets("A", "B", "C");
        hide("B", SheetVisibility.HIDDEN);
        show();

        assertRefused("Invalid Sheet index given.",
                () -> spreadsheet.setActiveSheetIndex(-1));
        assertRefused("Invalid Sheet index given.",
                () -> spreadsheet.setActiveSheetIndex(2));
        assertEquals("A", shown());
    }

    @Test
    void aSheetOutOfRange_isRefused() {
        sheets("A", "B");
        show();

        assertRefused("Invalid POI Sheet index given.",
                () -> spreadsheet.setActiveSheetWithPOIIndex(-1));
        assertRefused("Invalid POI Sheet index given.",
                () -> spreadsheet.setActiveSheetWithPOIIndex(2));
    }

    @Test
    void aHiddenSheet_cannotBeShown() {
        sheets("A", "B", "C");
        hide("B", SheetVisibility.HIDDEN);
        hide("C", SheetVisibility.VERY_HIDDEN);
        show();

        assertRefused(
                "Cannot set a hidden or very hidden sheet as the active sheet. Given index: 1",
                () -> spreadsheet.setActiveSheetWithPOIIndex(1));
        assertRefused(
                "Cannot set a hidden or very hidden sheet as the active sheet. Given index: 2",
                () -> spreadsheet.setActiveSheetWithPOIIndex(2));
        assertEquals("A", shown());
    }

    @Test
    void theSheetShown_isTheSelectedTabOfTheWorkbook() {
        sheets("A", "B");
        show();

        spreadsheet.setActiveSheetIndex(1);

        assertTrue(workbook.getSheet("B").isSelected());
        assertFalse(workbook.getSheet("A").isSelected());
    }

    @Test
    void aSheetShown_hasItsFormulasWorkedOutAgain() {
        sheets("A", "B");
        workbook.getSheet("A").createRow(0).createCell(0).setCellValue(1);
        Cell formula = workbook.getSheet("B").createRow(0).createCell(0);
        formula.setCellFormula("A!A1*10");
        show();
        spreadsheet.setActiveSheetIndex(1);
        assertEquals("10", spreadsheet.getCellValue(formula));
        spreadsheet.setActiveSheetIndex(0);
        // In POI, so the formula evaluator does not hear of it.
        workbook.getSheet("A").getRow(0).getCell(0).setCellValue(2);

        spreadsheet.setActiveSheetIndex(1);

        assertEquals("20", spreadsheet.getCellValue(formula));
    }

    /** Shows the numbers of A1 over five with two decimals. */
    private void twoDecimalsOverFive(Sheet sheet)
            throws ReflectiveOperationException {
        XSSFSheetConditionalFormatting formatting = (XSSFSheetConditionalFormatting) sheet
                .getSheetConditionalFormatting();
        XSSFConditionalFormattingRule rule = formatting
                .createConditionalFormattingRule(ComparisonOperator.GT, "5");
        // POI gives a rule a font, fill or border, but not a number format.
        Method dxf = XSSFConditionalFormattingRule.class
                .getDeclaredMethod("getDxf", boolean.class);
        dxf.setAccessible(true);
        CTNumFmt format = ((CTDxf) dxf.invoke(rule, true)).addNewNumFmt();
        format.setNumFmtId(200);
        format.setFormatCode("0.00");
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("A1") },
                rule);
    }

    @Test
    void aSheetShown_hasItsConditionalFormatsWorkedOutAgain()
            throws ReflectiveOperationException {
        sheets("A", "B");
        Cell value = workbook.getSheet("B").createRow(0).createCell(0);
        value.setCellValue(1);
        twoDecimalsOverFive(workbook.getSheet("B"));
        show();
        spreadsheet.setLocale(Locale.US);
        spreadsheet.setActiveSheetIndex(1);
        assertEquals("1", spreadsheet.getCellValue(value));
        spreadsheet.setActiveSheetIndex(0);
        // In POI, so the conditional formatting does not hear of it.
        value.setCellValue(10);

        spreadsheet.setActiveSheetIndex(1);

        assertEquals("10.00", spreadsheet.getCellValue(value));
    }

    /** C1 on the sheet has a thick left border, drawn on B1. */
    private void leftBorderOnC1(String sheet) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderLeft(BorderStyle.THICK);
        workbook.getSheet(sheet).createRow(0).createCell(2).setCellStyle(style);
    }

    private boolean borderDrawnOnB1() {
        return spreadsheet.getShiftedCellBorderStyles().stream()
                .anyMatch(rule -> rule.startsWith(".col2.row1{"));
    }

    @Test
    void aSheetShown_hasTheBordersOfItsCellsDrawn() {
        sheets("A", "B");
        leftBorderOnC1("B");
        show();
        assertFalse(borderDrawnOnB1());

        spreadsheet.setActiveSheetIndex(1);

        assertTrue(borderDrawnOnB1());
    }

    @Test
    void aSheetShownAgain_hasThePopupButtonsOfItsFrozenCellsPutOnAtOnce() {
        sheets("A", "B");
        workbook.getSheet("A").createFreezePane(1, 1);
        show();
        PopupButton button = new PopupButton();
        button.setContent(new Span());
        spreadsheet.setPopup("A1", button);
        spreadsheet.setActiveSheetIndex(1);

        spreadsheet.setActiveSheetIndex(0);

        // Before the client has reported which cells it shows.
        assertEquals(spreadsheet.getElement(), button.getElement().getParent());
    }

    @Test
    void aTabChosenOnTheClient_showsItsSheet_andTellsTheListenersWhichSheetWasLeft() {
        sheets("A", "B", "C");
        hide("B", SheetVisibility.HIDDEN);
        show();
        List<SheetChangeEvent> events = new ArrayList<>();
        spreadsheet.addSheetChangeListener(events::add);

        TestHelper.fireClientEvent(spreadsheet, "sheetSelected",
                "{\"sheetIndex\": 1, \"scrollLeft\": 30, \"scrollTop\": 40}");

        assertEquals("C", shown());
        assertEquals(1, events.size());
        assertEquals("A", events.get(0).getPreviousSheet().getSheetName());
        assertEquals("C", events.get(0).getNewSheet().getSheetName());
    }

    @Test
    void aTabChosenOnTheClient_keepsWhereTheSheetLeftWasScrolledTo() {
        sheets("A", "B");
        show();

        TestHelper.fireClientEvent(spreadsheet, "sheetSelected",
                "{\"sheetIndex\": 1, \"scrollLeft\": 30, \"scrollTop\": 40}");

        assertArrayEquals(new int[] { 40, 0 },
                spreadsheet.getVerticalScrollPositions());
        assertArrayEquals(new int[] { 30, 0 },
                spreadsheet.getHorizontalScrollPositions());
    }

    @Test
    void aSheetAddedOnTheClient_hasTheDefaultSize_andKeepsWhereTheSheetLeftWasScrolledTo() {
        sheets("A");
        show();
        spreadsheet.setDefaultRowCount(30);
        spreadsheet.setDefaultColumnCount(15);

        TestHelper.fireClientEvent(spreadsheet, "sheetCreated",
                "{\"scrollLeft\": 30, \"scrollTop\": 40}");

        assertEquals(2, workbook.getNumberOfSheets());
        assertEquals(30, spreadsheet.getRows());
        assertEquals(15, spreadsheet.getColumns());
        assertArrayEquals(new int[] { 40, 0 },
                spreadsheet.getVerticalScrollPositions());
        assertArrayEquals(new int[] { 30, 0 },
                spreadsheet.getHorizontalScrollPositions());
    }

    @Test
    void aTabRenamedOnTheClient_renamesItsSheet() {
        sheets("A", "B", "C");
        hide("B", SheetVisibility.HIDDEN);
        show();

        TestHelper.fireClientEvent(spreadsheet, "sheetRenamed",
                "{\"sheetIndex\": 1, \"newName\": \"D\"}");

        assertEquals(List.of("A", "B", "D"), sheetNames());
        assertEquals(List.of("A", "D"), tabs());
    }

    @Test
    void aNewSheetsName_isChecked() {
        sheets("A");
        show();

        assertRefused("Sheet Name cannot be an empty String.",
                () -> spreadsheet.createNewSheet("", 5, 5));
        assertRefused("Sheet Name cannot be longer than 31 characters",
                () -> spreadsheet.createNewSheet("x".repeat(32), 5, 5));
        assertRefused("Sheet name must be unique within the workbook.",
                () -> spreadsheet.createNewSheet("A", 5, 5));
        spreadsheet.createNewSheet("x".repeat(31), 5, 5);

        assertEquals(List.of("A", "x".repeat(31)), sheetNames());
    }

    @Test
    void theOnlySheetShown_cannotBeDeleted() {
        sheets("A", "B");
        hide("B", SheetVisibility.HIDDEN);
        show();

        assertRefused("A workbook must contain at least one visible worksheet",
                () -> spreadsheet.deleteSheetWithPOIIndex(0));
        assertEquals(List.of("A", "B"), sheetNames());
    }

    @Test
    void aTabDeleted_deletesItsSheet() {
        sheets("A", "B", "C");
        hide("B", SheetVisibility.HIDDEN);
        show();

        spreadsheet.deleteSheet(1);

        assertEquals(List.of("A", "B"), sheetNames());
    }

    @ParameterizedTest
    @EnumSource(names = { "HIDDEN", "VERY_HIDDEN" })
    void theSheetShownDeleted_givesWayToTheNextOneShown(
            SheetVisibility hidden) {
        sheets("A", "B", "C");
        hide("B", hidden);
        show();

        spreadsheet.deleteSheetWithPOIIndex(0);

        assertEquals("C", shown());
        assertEquals(List.of("C"), tabs());
    }

    @ParameterizedTest
    @EnumSource(names = { "HIDDEN", "VERY_HIDDEN" })
    void theLastSheetShownDeleted_givesWayToTheOneBefore(
            SheetVisibility hidden) {
        sheets("A", "B", "C", "D");
        hide("B", hidden);
        hide("D", hidden);
        show();
        spreadsheet.setActiveSheetIndex(1);

        spreadsheet.deleteSheetWithPOIIndex(2);

        assertEquals("A", shown());
    }

    @Test
    void aSheetDeleted_hasTheFormulasOfTheSheetShownWorkedOutAgain() {
        sheets("A", "B");
        workbook.getSheet("A").createRow(0).createCell(0).setCellValue(1);
        Cell formula = workbook.getSheet("B").createRow(0).createCell(0);
        formula.setCellFormula("A!A1*10");
        show();
        spreadsheet.setActiveSheetIndex(1);
        clientShowsRows();
        assertEquals("10", spreadsheet.getCellValue(formula));

        spreadsheet.deleteSheetWithPOIIndex(0);
        clientShowsRows();

        // Its sheet is gone.
        assertTrue(spreadsheet.isMarkedAsInvalidFormula(1, 1));
    }

    @Test
    void aSheetBeforeTheOneShownDeleted_leavesItShown() {
        sheets("A", "B", "C", "D");
        show();
        spreadsheet.setActiveSheetIndex(2);

        spreadsheet.deleteSheetWithPOIIndex(0);

        assertEquals("C", shown());
    }

    @Test
    void theSheetShownDeleted_givesWayToTheNextOne_withItsFrozenPanes() {
        sheets("A", "B");
        workbook.getSheet("B").createFreezePane(1, 2);
        show();

        spreadsheet.deleteSheetWithPOIIndex(0);

        assertEquals("B", shown());
        assertEquals(2, spreadsheet.getLastFrozenRow());
        assertEquals(1, spreadsheet.getLastFrozenColumn());
    }

    @Test
    void aTabRenamed_renamesItsSheet() {
        sheets("A", "B", "C");
        hide("B", SheetVisibility.HIDDEN);
        show();

        spreadsheet.setSheetName(1, "D");

        assertEquals(List.of("A", "B", "D"), sheetNames());
        assertEquals(List.of("A", "D"), tabs());
    }

    @Test
    void aHiddenSheetRenamed_leavesTheTabsAsTheyWere() {
        sheets("A", "B", "C");
        hide("B", SheetVisibility.HIDDEN);
        show();

        spreadsheet.setSheetNameWithPOIIndex(1, "D");

        assertEquals(List.of("A", "D", "C"), sheetNames());
        assertEquals(List.of("A", "C"), tabs());
    }

    @Test
    void aSheetsNewName_isChecked() {
        sheets("A", "B");
        show();
        String noName = "Sheet Name cannot be null or an empty String, or contain backslash \\.";

        assertRefused("Invalid POI Sheet index given.",
                () -> spreadsheet.setSheetNameWithPOIIndex(-1, "X"));
        assertRefused(noName,
                () -> spreadsheet.setSheetNameWithPOIIndex(0, null));
        assertRefused(noName,
                () -> spreadsheet.setSheetNameWithPOIIndex(0, ""));
        assertRefused("Sheet name must be unique within the workbook.",
                () -> spreadsheet.setSheetNameWithPOIIndex(0, "B"));
        assertEquals(List.of("A", "B"), sheetNames());
    }

    @Test
    void aHiddenSheet_canBeMadeVeryHidden_withOnlyOneSheetShown() {
        sheets("A", "B");
        hide("B", SheetVisibility.HIDDEN);
        show();

        spreadsheet.setSheetHidden(1, SheetVisibility.VERY_HIDDEN);

        assertTrue(workbook.isSheetVeryHidden(1));
    }

    @Test
    void theSheetShownMadeVisible_isLeftAsItWas() {
        sheets("A", "B");
        show();
        spreadsheet.setSelection("C3");

        spreadsheet.setSheetHidden(0, SheetVisibility.VISIBLE);

        assertEquals("C3",
                spreadsheet.getSelectedCellReference().formatAsString(false));
    }

    @Test
    void theLastSheetShownHidden_givesWayToTheOneBefore() {
        sheets("A", "B", "C");
        show();
        spreadsheet.setActiveSheetIndex(2);

        spreadsheet.setSheetHidden(2, SheetVisibility.HIDDEN);

        assertEquals("B", shown());
        assertEquals(List.of("A", "B"), tabs());
    }

    @ParameterizedTest
    @EnumSource(names = { "HIDDEN", "VERY_HIDDEN" })
    void theLastTabsSheetHidden_givesWayToTheOneBefore_pastHiddenSheets(
            SheetVisibility hidden) {
        sheets("A", "B", "C");
        hide("C", hidden);
        show();
        spreadsheet.setActiveSheetIndex(1);

        spreadsheet.setSheetHidden(1, SheetVisibility.HIDDEN);

        assertEquals("A", shown());
        assertEquals(List.of("A"), tabs());
    }

    @Test
    void theSheetShownHidden_givesWayToTheNextOne_withItsFrozenPanesAndBorders() {
        sheets("A", "B");
        workbook.getSheet("B").createFreezePane(1, 2);
        leftBorderOnC1("B");
        show();

        spreadsheet.setSheetHidden(0, SheetVisibility.HIDDEN);

        assertEquals("B", shown());
        assertEquals(2, spreadsheet.getLastFrozenRow());
        assertEquals(1, spreadsheet.getLastFrozenColumn());
        assertTrue(borderDrawnOnB1());
    }

    @Test
    void aSheetOutOfRange_cannotBeProtected() {
        sheets("A");
        show();

        assertRefused("Invalid POI Sheet index given.",
                () -> spreadsheet.setSheetProtected(-1, "secret"));
    }

    /**
     * Shows A and B, with an editor for every cell, and B2 selected. C3 of A is
     * not locked.
     */
    private void showWithEditors() {
        sheets("A", "B");
        CellStyle unlocked = workbook.createCellStyle();
        unlocked.setLocked(false);
        workbook.getSheet("A").createRow(2).createCell(2)
                .setCellStyle(unlocked);
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {
                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return new Span();
                    }

                    @Override
                    public Component getCustomComponentForCell(Cell cell,
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
        clientShowsRows();
        spreadsheet.setSelection("B2");
        clientCalled("updateFormulaBar");
    }

    private Component editorOfB2() {
        return editorOf(2, 2);
    }

    private Component editorOf(int column, int row) {
        return spreadsheet.getCellKeyToEditorMap()
                .get(SpreadsheetUtil.toKey(column, row));
    }

    @Test
    void theSheetShownProtected_takesTheEditorsOffItsLockedCellsOnly() {
        showWithEditors();
        Component editor = editorOfB2();
        assertNotNull(editor);
        assertNotNull(editorOf(3, 3));

        spreadsheet.setActiveSheetProtected("secret");

        assertNull(editorOfB2());
        assertNull(editor.getElement().getParent(), "and it is off the sheet");
        assertNotNull(editorOf(3, 3), "C3 is not locked");
    }

    @Test
    void theSheetShownUnprotected_putsTheEditorsBack() {
        showWithEditors();
        spreadsheet.setActiveSheetProtected("secret");

        spreadsheet.setActiveSheetProtected(null);

        assertNotNull(editorOfB2());
    }

    @Test
    void theSheetShownProtected_hasTheClientHearOfTheSelectedCell() {
        showWithEditors();

        spreadsheet.setActiveSheetProtected("secret");

        assertTrue(spreadsheet.isActiveSheetProtected());
        assertTrue(clientCalled("updateFormulaBar"));
    }

    @Test
    void anotherSheetProtected_leavesTheSheetShownAsItWas() {
        showWithEditors();

        spreadsheet.setSheetProtected(1, "secret");

        assertTrue(workbook.getSheetAt(1).getProtect());
        assertFalse(spreadsheet.isActiveSheetProtected());
        assertNotNull(editorOfB2());
        assertFalse(clientCalled("updateFormulaBar"));
    }

    /** A workbook saved with its active sheet hidden, as POI allows. */
    private static XSSFWorkbook activeSheetHidden() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Hidden").setDefaultRowHeightInPoints(30);
        workbook.createSheet("Shown").createRow(0).setHeightInPoints(20);
        workbook.setActiveSheet(0);
        workbook.setSheetHidden(0, true);
        return workbook;
    }

    @Test
    void aWorkbookWhoseActiveSheetIsHidden_showsTheNextOne_withItsRows() {
        Spreadsheet shown = new Spreadsheet(activeSheetHidden());

        assertEquals("Shown", shown.getActiveSheet().getSheetName());
        assertEquals(1, shown.getElement().getProperty("sheetIndex", 0));
        assertEquals(20f, shown.getRowH()[0]);
    }

    @Test
    void aWorkbookWhoseActiveSheetIsHidden_setLater_showsTheNextOne() {
        Spreadsheet shown = new Spreadsheet();

        shown.setWorkbook(activeSheetHidden());

        assertEquals("Shown", shown.getActiveSheet().getSheetName());
        assertEquals(1, shown.getElement().getProperty("sheetIndex", 0));
        assertEquals(20f, shown.getRowH()[0]);
    }
}
