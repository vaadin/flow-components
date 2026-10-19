/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * How a workbook and its sheets are shown. A workbook is shown with a scope of
 * its own, and so is a sheet, everything of it loaded: what the last one left
 * on the client goes with its scope. Every way of showing a workbook comes here
 * - a new one, one the application sets, one read from a file or a stream, the
 * one read back after writing - and every way of showing a sheet, through
 * {@link #showSheet(int)} or the component's {@code reloadActiveSheetData()}.
 */
final class SheetLoader implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SheetLoader.class);

    private final Spreadsheet spreadsheet;

    SheetLoader(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Forgets the workbook shown, and shows the given one, or a new XLSX
     * workbook with one sheet of the given size for {@code null}.
     */
    void load(Workbook workbook, int rowCount, int columnCount) {
        spreadsheet.clearSheetServerSide();
        if (workbook == null) {
            workbook = new XSSFWorkbook();
            prepareNewSheet(createNewSheet(workbook), rowCount, columnCount);
        }
        show(workbook);
    }

    /**
     * Disposes of the workbook shown, if it is a streaming one, and shows a new
     * XLSX workbook of the default size.
     */
    void loadNew() {
        Workbook workbook = spreadsheet.getWorkbook();
        if (workbook instanceof SXSSFWorkbook sxssf) {
            sxssf.dispose();
        }
        final XSSFWorkbook newWorkbook = new XSSFWorkbook();
        prepareNewSheet(createNewSheet(newWorkbook),
                SpreadsheetFactory.DEFAULT_ROWS,
                SpreadsheetFactory.DEFAULT_COLUMNS);
        spreadsheet.clearSheetServerSide();
        show(newWorkbook);
    }

    /**
     * Shows the workbook of the file, an XLS or XLSX one, in place of the one
     * shown.
     *
     * @throws IOException
     *             if the file has another format
     */
    void read(File file) throws IOException {
        try {
            replace(WorkbookFactory.create(file));
        } catch (POIXMLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Shows the workbook of the stream, an XLS or XLSX one, in place of the one
     * shown.
     *
     * @throws IOException
     *             if the data in the stream has another format
     */
    void read(InputStream inputStream) throws IOException {
        replace(WorkbookFactory.create(inputStream));
    }

    /**
     * Shows the workbook in place of the one shown, which is forgotten, and
     * disposed of if it is a streaming one.
     */
    void replace(Workbook workbook) {
        Workbook oldWorkbook = spreadsheet.getWorkbook();
        if (oldWorkbook != null) {
            spreadsheet.clearSheetServerSide();
            if (oldWorkbook instanceof SXSSFWorkbook sxssf) {
                sxssf.dispose();
            }
        }
        show(workbook);
    }

    /**
     * Shows the workbook, at its active sheet, in a scope of its own: what the
     * server keeps of the last one is the caller's to forget.
     */
    void show(Workbook workbook) {
        spreadsheet.openWorkbookScope(workbook);
        setUpWorkbook(workbook);
        showSheet(workbook.getActiveSheetIndex());
    }

    /**
     * The workbook just put in its scope: its style factory and conditional
     * formatter made, its styles loaded, its protection read, and the client
     * told it is another workbook.
     */
    private void setUpWorkbook(Workbook workbook) {
        WorkbookScope scope = spreadsheet.workbookScope();
        // Through the methods an application may override, once the scope is
        // in place for them to read, and before the sheet, which reads them,
        // is loaded.
        scope.styler = spreadsheet.createSpreadsheetStyleFactory();
        scope.conditionalFormatter = spreadsheet.createConditionalFormatter();
        // The styles are the workbook's: loaded with it, for the sheet shown
        // to be styled with them.
        scope.styler.reloadWorkbookStyles();

        WorkbookClientModel client = spreadsheet.workbookModel();
        if (workbook instanceof HSSFWorkbook hssf) {
            client.workbookProtected.set(hssf.isWriteProtected());
        } else if (workbook instanceof XSSFWorkbook xssf) {
            client.workbookProtected.set(xssf.isStructureLocked());
        }

        spreadsheet.toggleWorkbookChange();
    }

    /**
     * Adds a sheet to the workbook shown, named for its place among the sheets
     * when the name is {@code null}, and shows it.
     */
    void addSheet(String sheetName, int rows, int columns) {
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet sheet;
        if (sheetName == null) {
            sheet = createNewSheet(workbook);
        } else {
            sheet = workbook.createSheet(sheetName);
        }
        prepareNewSheet(sheet, rows, columns);
        showSheet(workbook.getSheetIndex(sheet));
    }

    /**
     * Shows the sheet at the POI index, or the nearest one shown if it is
     * hidden: the client has nothing of the last sheet any more, and everything
     * of this one is loaded, by the component's
     * {@code reloadActiveSheetData()}. Every way of showing a sheet comes here:
     * switching to it, adding it, and loading a workbook. Freezing panes
     * reloads the sheet shown through {@code reloadActiveSheetData()} alone.
     */
    void showSheet(int poiIndex) {
        Workbook workbook = spreadsheet.getWorkbook();
        int shown = SheetOperations.nearestShown(workbook, poiIndex);
        workbook.setActiveSheet(shown);
        // The UI selects one sheet at a time: the active one.
        workbook.setSelectedTab(shown);

        // formulas defined relative to the sheet may need recalculation
        spreadsheet.getFormulaEvaluator().clearAllCachedResultValues();

        // Through the component, so an override of it applies.
        spreadsheet.reloadActiveSheetData();
    }

    /**
     * Shows the active sheet anew, in a scope of its own: the client is left
     * nothing of the sheet shown before, and is given everything of this one.
     *
     * @see Spreadsheet#reloadActiveSheetData()
     */
    void reloadActiveSheet() {
        // What the client has of the last sheet goes with its scope.
        spreadsheet.openSheetScope();

        spreadsheet.clientChanges().sheetShown();

        Workbook workbook = spreadsheet.getWorkbook();
        Sheet sheet = spreadsheet.getActiveSheet();
        spreadsheet.workbookModel().sheetIndex.set(spreadsheet
                .getSpreadsheetSheetIndex(workbook.getActiveSheetIndex()) + 1);
        spreadsheet.sheetModel().sheetProtected.set(sheet.getProtect());

        spreadsheet.popupButtons().detachAll();

        // clear all tables, possible tables for new/changed sheet are added
        // after first round trip.
        spreadsheet.workbookScope().tables.markNeedsReload();

        reloadSheetNames();
        spreadsheet.updateMergedRegions();
        spreadsheet.sheetModel().displayGridlines
                .set(sheet.isDisplayGridlines());
        spreadsheet.sheetModel().displayRowColHeadings
                .set(sheet.isDisplayRowColHeadings());

        // Everything of the sheet is loaded again: what the client had of it
        // went with the scope.
        loadParts(sheet);
        spreadsheet.reloadActiveSheetStyles();
        spreadsheet.loadPopupButtons();
    }

    /**
     * Gives the client the names of the sheets shown as tabs, the hidden ones
     * left out, and where each was scrolled to.
     */
    void reloadSheetNames() {
        Workbook workbook = spreadsheet.getWorkbook();
        final ArrayList<String> sheetNamesList = new ArrayList<String>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetVeryHidden(i) && !workbook.isSheetHidden(i)) {
                sheetNamesList.add(workbook.getSheetName(i));
            }
        }
        spreadsheet.workbookModel().sheetNames
                .set(sheetNamesList.toArray(new String[sheetNamesList.size()]));
        spreadsheet.workbookScope().scrollMemory.publish();
    }

    /**
     * Loads everything of the sheet: its sizes, pictures and charts, tables,
     * merged cells, frozen panes, protection, outline and named ranges.
     */
    private void loadParts(final Sheet sheet) {
        SpreadsheetFactory.logMemoryUsage();
        // What POI cannot read of a sheet is left out, and the rest loaded.
        loadPart("row height",
                () -> spreadsheet.sizes().loadDefaultRowHeight(sheet));
        loadPart("column width",
                () -> spreadsheet.sizes().loadDefaultColumnWidth());
        loadPart("sizes", () -> spreadsheet.sizes().load(sheet));
        loadPart("overlays",
                () -> SpreadsheetFactory.loadSheetOverlays(spreadsheet));
        loadPart("tables",
                () -> SpreadsheetFactory.loadSheetTables(spreadsheet));
        loadPart("merged cells", spreadsheet.mergedRegionOperations::load);
        loadPart("frozen panes",
                () -> SpreadsheetFactory.loadFreezePane(spreadsheet));
        loadPart("protection",
                () -> SpreadsheetFactory.loadSheetProtection(spreadsheet));
        loadPart("grouping", () -> spreadsheet.outline().load());
        loadPart("named ranges",
                () -> SpreadsheetFactory.loadNamedRanges(spreadsheet));
        SpreadsheetFactory.logMemoryUsage();
    }

    private static void loadPart(String part, Runnable load) {
        try {
            load.run();
        } catch (RuntimeException failure) {
            LOGGER.warn("Could not load the {} of the sheet", part, failure);
        }
    }

    private static void prepareNewSheet(final Sheet sheet, int rows,
            int columns) {
        sheet.createRow(rows - 1).createCell(columns - 1);
        sheet.setDefaultColumnWidth(SpreadsheetFactory.DEFAULT_COL_WIDTH_UNITS);
    }

    /** A sheet named for its place among the sheets: Sheet1, Sheet2... */
    private static Sheet createNewSheet(Workbook workbook) {
        int idx = workbook.getNumberOfSheets() + 1;
        String sheetname = "Sheet" + idx;
        while (workbook.getSheet(sheetname) != null) {
            idx++;
            sheetname = "Sheet" + idx;
        }
        return workbook.createSheet(sheetname);
    }
}
