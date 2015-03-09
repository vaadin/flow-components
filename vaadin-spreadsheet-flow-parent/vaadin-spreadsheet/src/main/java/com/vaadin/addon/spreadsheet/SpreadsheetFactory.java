package com.vaadin.addon.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.SpreadsheetState;

/**
 * SpreadsheetFactory is an utility class of the Spreadsheet component. It is
 * used for operations related to loading and saving a workbook and related
 * data.
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class SpreadsheetFactory implements Serializable {

    private static final Logger LOGGER = Logger
            .getLogger(SpreadsheetFactory.class.getName());

    /**
     * Default column width for new sheets in characters
     */
    public static final int DEFAULT_COL_WIDTH_UNITS = 10;

    /**
     * Default for height for new sheets in points
     */
    public static final float DEFAULT_ROW_HEIGHT_POINTS = 12.75f;

    /**
     * Default column count for new workbooks
     */
    public static final int DEFAULT_COLUMNS = 52;

    /**
     * Default row count for new workbooks
     */
    public static final int DEFAULT_ROWS = 200;

    /**
     * Set to true if Spreadsheet should log its memory usage.
     */
    private static boolean LOG_MEMORY = false;

    /**
     * Clears the given Spreadsheet and loads the given Workbook into it.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     * @param workbook
     *            Workbook to load or null to generate a new workbook with one
     *            sheet.
     * @param rowCount
     *            Number of rows to generate in the first sheet. Only applies
     *            when the workbook parameter is null.
     * @param columnCount
     *            Number of columns to generate in the first sheet. Only applies
     *            when the workbook parameter is null.
     */
    static void loadSpreadsheetWith(Spreadsheet spreadsheet, Workbook workbook,
            int rowCount, int columnCount) {
        spreadsheet.clearSheetServerSide();
        final Sheet sheet;
        if (workbook == null) {
            workbook = new XSSFWorkbook();
            sheet = createNewSheet(workbook);
            spreadsheet.setInternalWorkbook(workbook);
            generateNewSpreadsheet(spreadsheet, sheet, rowCount, columnCount);
        } else {
            int activeSheetIndex = workbook.getActiveSheetIndex();
            if (workbook.isSheetHidden(activeSheetIndex)
                    || workbook.isSheetVeryHidden(activeSheetIndex)) {
                workbook.setActiveSheet(SpreadsheetUtil
                        .getFirstVisibleSheetPOIIndex(workbook));
            }
            sheet = workbook.getSheetAt(activeSheetIndex);
            spreadsheet.setInternalWorkbook(workbook);
            reloadSpreadsheetData(spreadsheet, sheet);
        }
        loadWorkbookStyles(spreadsheet);
    }

    /**
     * Clears the target Spreadsheet, creates a new XLSX Workbook and loads it
     * in the Spreadsheet.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     */
    static void loadNewXLSXSpreadsheet(Spreadsheet spreadsheet) {
        Workbook workbook = spreadsheet.getWorkbook();
        if (workbook != null && workbook instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) workbook).dispose();
        }
        final XSSFWorkbook newWorkbook = new XSSFWorkbook();
        final Sheet sheet = newWorkbook.createSheet();
        spreadsheet.clearSheetServerSide();
        spreadsheet.setInternalWorkbook(newWorkbook);
        generateNewSpreadsheet(spreadsheet, sheet, DEFAULT_ROWS,
                DEFAULT_COLUMNS);
        sheet.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT_POINTS);
        loadWorkbookStyles(spreadsheet);
    }

    /**
     * Adds a new sheet to the given Spreadsheet and Workbook.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     * @param workbook
     *            Target Workbook
     * @param sheetName
     *            Name of the new sheet
     * @param rows
     *            Row count for the new sheet
     * @param columns
     *            Column count for the new sheet
     */
    static void addNewSheet(final Spreadsheet spreadsheet,
            final Workbook workbook, final String sheetName, int rows,
            int columns) {
        final Sheet sheet;
        if (sheetName == null) {
            sheet = createNewSheet(workbook);
        } else {
            sheet = workbook.createSheet(sheetName);
        }
        int sheetIndex = workbook.getSheetIndex(sheet);
        workbook.setActiveSheet(sheetIndex);
        spreadsheet.reloadActiveSheetData();
        spreadsheet.reloadActiveSheetStyles();
        final SpreadsheetState state = spreadsheet.getState();
        int[] verticalScrollPositions = Arrays.copyOf(
                state.verticalScrollPositions, state.sheetNames.length);
        int[] horizontalScrollPositions = Arrays.copyOf(
                state.horizontalScrollPositions, state.sheetNames.length);
        state.verticalScrollPositions = verticalScrollPositions;
        state.horizontalScrollPositions = horizontalScrollPositions;
        generateNewSpreadsheet(spreadsheet, sheet, rows, columns);
    }

    /**
     * Reloads the Spreadsheet component from the given file.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     * @param spreadsheetFile
     *            Source file. Should be of XLS or XLSX format.
     * @throws IOException
     *             If file has invalid format
     */
    static void reloadSpreadsheetComponent(Spreadsheet spreadsheet,
            final File spreadsheetFile) throws IOException {
        try {
            reloadSpreadsheetComponent(spreadsheet,
                    WorkbookFactory.create(spreadsheetFile));
        } catch (InvalidFormatException e) {
            throw new IOException("Invalid file format.", e);
        }
    }

    /**
     * Reloads the Spreadsheet component from the given InputStream.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     * @param inputStream
     *            Source stream. Stream content be of XLS or XLSX format.
     * @throws IOException
     *             If data in the stream has invalid format
     */
    static void reloadSpreadsheetComponent(Spreadsheet spreadsheet,
            final InputStream inputStream) throws IOException {
        try {
            reloadSpreadsheetComponent(spreadsheet,
                    WorkbookFactory.create(inputStream));
        } catch (InvalidFormatException e) {
            throw new IOException("Invalid file format.", e);
        }
    }

    /**
     * Reloads the Spreadsheet component using the given Workbook as data
     * source.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     * @param workbook
     *            Source Workbook
     */
    static void reloadSpreadsheetComponent(Spreadsheet spreadsheet,
            final Workbook workbook) {
        Workbook oldWorkbook = spreadsheet.getWorkbook();
        if (oldWorkbook != null) {
            spreadsheet.clearSheetServerSide();
            if (oldWorkbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) oldWorkbook).dispose();
            }
        }
        final Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
        spreadsheet.setInternalWorkbook(workbook);
        reloadSpreadsheetData(spreadsheet, sheet);
        loadWorkbookStyles(spreadsheet);
    }

    /**
     * Writes the current Workbook state from the given Spreadsheet to the given
     * file.
     * 
     * @param spreadsheet
     *            Source Spreadsheet
     * @param fileName
     *            Target file name
     * @return File handle to the written file
     * @throws FileNotFoundException
     *             If file was not found
     * @throws IOException
     *             If some other IO error happened
     */
    static File write(Spreadsheet spreadsheet, String fileName)
            throws FileNotFoundException, IOException {
        final Workbook workbook = spreadsheet.getWorkbook();
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            if (workbook instanceof HSSFWorkbook) {
                fileName += ".xls";
            } else {
                fileName += ".xlsx";
            }
        }
        final File file = new File(fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        try {
            Workbook wb = WorkbookFactory.create(file);
            spreadsheet.setInternalWorkbook(wb);
        } catch (InvalidFormatException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return file;
    }

    /**
     * Writes the current Workbook state from the given Spreadsheet to the given
     * output stream. The stream will be closed after writing.
     * 
     * @param spreadsheet
     *            Source Spreadsheet
     * @param stream
     *            Output stream to write to
     * @throws IOException
     *             If there was an error handling the stream.
     */
    static void write(Spreadsheet spreadsheet, OutputStream stream)
            throws IOException {
        final Workbook workbook = spreadsheet.getWorkbook();
        try {
            workbook.write(stream);
            stream.close();
            stream = null;
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * Loads styles for the Workbook and the currently active sheet.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     */
    static void loadWorkbookStyles(Spreadsheet spreadsheet) {
        spreadsheet.getSpreadsheetStyleFactory().reloadWorkbookStyles();
        spreadsheet.getSpreadsheetStyleFactory().reloadActiveSheetCellStyles();
    }

    /**
     * Sets the size, default row height and default column width for the given
     * new Sheet in the target Spreadsheet. Finally loads the sheet.
     * 
     * @param spreadsheet
     *            Target spreadsheet
     * @param sheet
     *            Target sheet
     * @param rows
     *            Amount of rows
     * @param columns
     *            Amount of columns
     */
    static void generateNewSpreadsheet(final Spreadsheet spreadsheet,
            final Sheet sheet, int rows, int columns) {
        sheet.createRow(rows - 1).createCell(columns - 1);
        final float defaultRowHeightInPoints = sheet
                .getDefaultRowHeightInPoints();
        if (defaultRowHeightInPoints <= 0) {
            sheet.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT_POINTS);
        }
        // use excel default column width instead of Apache POI default (8)
        sheet.setDefaultColumnWidth(DEFAULT_COL_WIDTH_UNITS);
        reloadSpreadsheetData(spreadsheet, sheet);
    }

    /**
     * Reloads all data for the given Sheet within the target Spreadsheet
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     * @param sheet
     *            Target sheet within the Spreadsheet
     */
    static void reloadSpreadsheetData(final Spreadsheet spreadsheet,
            final Sheet sheet) {
        logMemoryUsage();
        try {
            float defaultRowHeightInPoints = sheet
                    .getDefaultRowHeightInPoints();
            if (defaultRowHeightInPoints <= 0) {
                sheet.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT_POINTS);
                spreadsheet.getState().defRowH = DEFAULT_ROW_HEIGHT_POINTS;
            } else {
                spreadsheet.getState().defRowH = defaultRowHeightInPoints;
            }

            // Always have at least the default amount of rows
            int rows = sheet.getLastRowNum() + 1;
            if (rows < spreadsheet.getDefaultRowCount()) {
                rows = spreadsheet.getDefaultRowCount();
            }
            spreadsheet.getState().rows = rows;

            int charactersToPixels = ExcelToHtmlUtils.getColumnWidthInPx(sheet
                    .getDefaultColumnWidth() * 256);
            if (charactersToPixels > 0) {
                spreadsheet.getState().defColW = charactersToPixels;
            } else {
                spreadsheet.getState().defColW = SpreadsheetUtil
                        .getDefaultColumnWidthInPx();
                sheet.setDefaultColumnWidth(DEFAULT_COL_WIDTH_UNITS);
            }
            final float[] rowHeights = new float[rows];
            int cols = 0;
            int tempRowIndex = -1;
            final ArrayList<Integer> hiddenRowIndexes = new ArrayList<Integer>();
            for (Row row : sheet) {
                int rIndex = row.getRowNum();
                // set the empty rows to have the default row width
                while (++tempRowIndex != rIndex) {
                    rowHeights[tempRowIndex] = spreadsheet.getState().defRowH;
                }
                if (row.getZeroHeight()) {
                    rowHeights[rIndex] = 0.0F;
                    hiddenRowIndexes.add(rIndex + 1);
                } else {
                    rowHeights[rIndex] = row.getHeightInPoints();
                }
                int c = row.getLastCellNum();
                if (c > cols) {
                    cols = c;
                }
            }
            if (rows > sheet.getLastRowNum() + 1) {
                for (int i = sheet.getLastRowNum(); i < rows; i++) {
                    rowHeights[i] = defaultRowHeightInPoints;
                }
            }
            spreadsheet.getState().hiddenRowIndexes = hiddenRowIndexes;
            spreadsheet.getState().rowH = rowHeights;

            // Always have at least the default amount of columns
            if (cols < spreadsheet.getDefaultColumnCount()) {
                cols = spreadsheet.getDefaultColumnCount();
            }
            spreadsheet.getState().cols = cols;

            final int[] colWidths = new int[cols];
            final ArrayList<Integer> hiddenColumnIndexes = new ArrayList<Integer>();
            for (int i = 0; i < cols; i++) {
                if (sheet.isColumnHidden(i)) {
                    colWidths[i] = 0;
                    hiddenColumnIndexes.add(i + 1);
                } else {
                    colWidths[i] = ExcelToHtmlUtils.getColumnWidthInPx(sheet
                            .getColumnWidth(i));
                }
            }
            spreadsheet.getState().hiddenColumnIndexes = hiddenColumnIndexes;
            spreadsheet.getState().colW = colWidths;

            loadSheetImages(spreadsheet);
            loadMergedRegions(spreadsheet);
            loadFreezePane(spreadsheet);
        } catch (NullPointerException npe) {
            LOGGER.log(Level.WARNING, npe.getMessage(), npe);
        }
        logMemoryUsage();
    }

    /**
     * Loads images for the currently active sheet and adds them to the target
     * Spreadsheet.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     */
    static void loadSheetImages(Spreadsheet spreadsheet) {
        final Sheet sheet = spreadsheet.getActiveSheet();
        Drawing drawing = sheet.createDrawingPatriarch();
        if (drawing instanceof XSSFDrawing) {
            for (XSSFShape shape : ((XSSFDrawing) drawing).getShapes()) {
                if (shape instanceof XSSFPicture) {
                    // in XSSFPicture.getPreferredSize(double) POI presumes that
                    // XSSFAnchor is always of type XSSFClientAnchor
                    XSSFClientAnchor anchor = (XSSFClientAnchor) shape
                            .getAnchor();
                    XSSFPictureData pictureData = ((XSSFPicture) shape)
                            .getPictureData();
                    SheetImageWrapper image = new SheetImageWrapper();
                    image.setAnchor(anchor);
                    image.setMIMEType(pictureData.getMimeType());
                    image.setData(pictureData.getData());
                    if (anchor != null) {
                        spreadsheet.sheetImages.add(image);
                    } else {
                        LOGGER.log(Level.FINE, "IMAGE WITHOUT ANCHOR: "
                                + pictureData.toString());
                        // FIXME seems like there is a POI bug, images that have
                        // in Excel (XLSX) been se as a certain type (type==3)
                        // will get a null anchor.
                        // Achor types:
                        // 0 = Move and size with Cells,
                        // 2 = Move but don't size with cells,
                        // 3 = Don't move or size with cells.
                    }
                }
            }
        } else if (drawing instanceof HSSFPatriarch) {
            for (HSSFShape shape : ((HSSFPatriarch) drawing).getChildren()) {
                if (shape instanceof HSSFPicture) {
                    HSSFClientAnchor anchor = (HSSFClientAnchor) shape
                            .getAnchor();
                    HSSFPictureData pictureData = ((HSSFPicture) shape)
                            .getPictureData();
                    SheetImageWrapper image = new SheetImageWrapper();
                    image.setAnchor(anchor);
                    image.setMIMEType(pictureData.getMimeType());
                    image.setData(pictureData.getData());
                    if (anchor != null) {
                        spreadsheet.sheetImages.add(image);
                    } else {
                        LOGGER.log(Level.FINE, "IMAGE WITHOUT ANCHOR: "
                                + pictureData.toString());
                    }
                }
            }
        }
    }

    /**
     * Loads merged region(s) configuration for the currently active sheet and
     * sets it into the shared state.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     */
    static void loadMergedRegions(Spreadsheet spreadsheet) {
        final Sheet sheet = spreadsheet.getActiveSheet();
        spreadsheet.getState().mergedRegions = null;
        spreadsheet.mergedRegionCounter = 0;
        int numMergedRegions = sheet.getNumMergedRegions();
        if (numMergedRegions > 0) {
            spreadsheet.getState().mergedRegions = new ArrayList<MergedRegion>(
                    numMergedRegions);
            for (int i = 0; i < numMergedRegions; i++) {
                CellRangeAddress cra = sheet.getMergedRegion(i);
                MergedRegion mergedRegion = new MergedRegion();
                mergedRegion.col1 = cra.getFirstColumn() + 1;
                mergedRegion.col2 = cra.getLastColumn() + 1;
                mergedRegion.row1 = cra.getFirstRow() + 1;
                mergedRegion.row2 = cra.getLastRow() + 1;
                mergedRegion.id = spreadsheet.mergedRegionCounter++;
                spreadsheet.getState().mergedRegions.add(mergedRegion);
            }
        }
    }

    /**
     * Loads freeze pane configuration for the currently active sheet and sets
     * it into the shared state.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     */
    static void loadFreezePane(Spreadsheet spreadsheet) {
        final Sheet sheet = spreadsheet.getActiveSheet();
        PaneInformation paneInformation = sheet.getPaneInformation();
        // only freeze panes supported
        if (paneInformation != null && paneInformation.isFreezePane()) {
            // Apparently in POI HorizontalSplitPosition means rows and
            // VerticalSplitPosition means columns. Changed the meaning for the
            // component internals
            spreadsheet.getState().horizontalSplitPosition = paneInformation
                    .getVerticalSplitPosition();
            spreadsheet.getState().verticalSplitPosition = paneInformation
                    .getHorizontalSplitPosition();
        } else {
            spreadsheet.getState().verticalSplitPosition = 0;
            spreadsheet.getState().horizontalSplitPosition = 0;
        }
    }

    private static Sheet createNewSheet(Workbook workbook) {
        int idx = workbook.getNumberOfSheets() + 1;
        String sheetname = "Sheet" + idx;
        while (workbook.getSheet(sheetname) != null) {
            idx++;
            sheetname = "Sheet" + idx;
        }
        return workbook.createSheet(sheetname);
    }

    /**
     * Runs garbage collection and outputs current memory usage to console.
     */
    public static void logMemoryUsage() {
        // TODO make this a more comprehensive solution (output logging
        // automatically if set?)
        if (LOG_MEMORY) {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long tot = runtime.totalMemory();
            long free = runtime.freeMemory();
            LOGGER.log(Level.INFO, "Total: " + tot / 1000000 + " Free: " + free
                    / 1000000 + " Usage: " + (tot - free) / 1000000);
        }
    }
}
