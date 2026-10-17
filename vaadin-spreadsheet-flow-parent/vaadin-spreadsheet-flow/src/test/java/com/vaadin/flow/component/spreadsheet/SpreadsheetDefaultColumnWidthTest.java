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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * Columns without a width of their own are shown at the default width of the
 * component, whatever the default width of their sheet, and showing a workbook
 * leaves the default width of its sheets as it was. Loading a sheet set its
 * default width to that of the component, so a workbook written back had
 * another, and HSSF read its columns wider than the component's default.
 */
class SpreadsheetDefaultColumnWidthTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    /** The default width of the component, ten characters. */
    private static final int DEFAULT_WIDTH = 70;

    static Stream<Supplier<Workbook>> formats() {
        return Stream.of(XSSFWorkbook::new, HSSFWorkbook::new);
    }

    /** Shows a sheet of a default width other than the component's. */
    private static Spreadsheet show(Supplier<Workbook> format) {
        Workbook workbook = format.get();
        workbook.createSheet().setDefaultColumnWidth(12);
        return new Spreadsheet(workbook);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void showingAWorkbook_leavesTheDefaultWidthOfItsSheets(
            Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);

        assertEquals(12, spreadsheet.getActiveSheet().getDefaultColumnWidth());
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aColumnWithoutAWidth_isShownAtTheDefaultWidth(
            Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);

        assertEquals(DEFAULT_WIDTH, spreadsheet.getDefaultColumnWidth());
        assertEquals(DEFAULT_WIDTH, spreadsheet.getColW()[3]);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aDefaultWidthSetByTheApplication_leavesTheSheetsAsItIs(
            Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);

        spreadsheet.setDefaultColumnWidth(90);

        assertEquals(90, spreadsheet.getDefaultColumnWidth());
        assertEquals(12, spreadsheet.getActiveSheet().getDefaultColumnWidth());
    }

    @Test
    void textRunningOverColumnsWithoutAWidth_runsOverAsFarAsTheDefaultWidthSet() {
        // As the client reports how wide a character of each style is, which
        // the cells need to know how far their text runs over.
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(0, 7f);
        Consumer<Spreadsheet> configure = spreadsheet -> {
            spreadsheet.setLocale(Locale.US);
            spreadsheet.getCellValueManager()
                    .onCellStyleWidthRatioUpdate(ratios);
        };
        FreshLoadHarness harness = new FreshLoadHarness(ui, configure,
                new FreshLoadHarness.Viewport(1, 1, 10, 10));
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).createCell(0)
                .setCellValue("a text long enough to run over some columns");
        harness.show(workbook);
        Consumer<Spreadsheet> wider = spreadsheet -> spreadsheet
                .setDefaultColumnWidth(200);

        assertEquals(List.of(), harness.check(wider, configure.andThen(wider)));
    }

    @ParameterizedTest
    @MethodSource("formats")
    void columnsWithoutAWidth_areShownAtTheDefaultWidthSet(
            Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);
        spreadsheet.setColumnWidth(4, 100);

        spreadsheet.setDefaultColumnWidth(90);

        assertEquals(90, spreadsheet.getColW()[3]);
        assertEquals(100, spreadsheet.getColW()[4]);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void theDefaultWidthSet_staysWhenAnotherSheetIsShown(
            Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);
        spreadsheet.setDefaultColumnWidth(90);

        spreadsheet.createNewSheet("Second", 20, 10);

        assertEquals(90, spreadsheet.getDefaultColumnWidth());
        assertEquals(90, spreadsheet.getColW()[3]);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aColumnWithAWidth_isShownAtIt(Supplier<Workbook> format) {
        Workbook workbook = format.get();
        Sheet sheet = workbook.createSheet();
        sheet.setColumnWidth(3, SheetSizes.columnWidthUnits(sheet, 100));

        assertEquals(100, new Spreadsheet(workbook).getColW()[3]);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aHiddenColumnWithoutAWidth_isShownAgainAtTheDefaultWidth(
            Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);
        spreadsheet.setColumnHidden(3, true);

        spreadsheet.setColumnHidden(3, false);

        assertEquals(DEFAULT_WIDTH, spreadsheet.getColW()[3]);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aHiddenColumnWithAWidth_isShownAgainAtIt(Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);
        spreadsheet.setColumnWidth(3, 100);
        spreadsheet.setColumnHidden(3, true);

        spreadsheet.setColumnHidden(3, false);

        assertEquals(100, spreadsheet.getColW()[3]);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aColumnAddedLaterWithoutAWidth_isShownAtTheDefaultWidth(
            Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);
        int columns = spreadsheet.getColW().length;
        spreadsheet.setMaxColumns(columns + 3);

        spreadsheet.setColumnWidth(columns + 2, 120);

        assertEquals(DEFAULT_WIDTH, spreadsheet.getColW()[columns]);
    }

    @ParameterizedTest
    @MethodSource("formats")
    void aColumnWithoutAWidthResized_isUndoneToTheDefaultWidth(
            Supplier<Workbook> format) {
        Spreadsheet spreadsheet = show(format);
        // The client numbers the columns from 1.
        TestHelper.fireClientEvent(spreadsheet, "columnResized",
                "{\"colIndexes\": [4], \"newWidths\": [150]}");

        TestHelper.fireClientEvent(spreadsheet, "onUndo", "{}");

        assertEquals(DEFAULT_WIDTH, spreadsheet.getColW()[3]);
    }
}
