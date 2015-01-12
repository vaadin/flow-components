package com.vaadin.addon.spreadsheet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;

import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.SpreadsheetState;

/**
 * Utility class for {@link Spreadsheet} operations.
 */
public class SpreadsheetFactory {

    public static final int DEFAULT_COLUMNS = 52;

    public static final int DEFAULT_ROWS = 200;

    public static boolean LOG_MEMORY = false;

    private static final int DEFAULT_COL_WIDTH_UNITS = 10;

    private static final float DEFAULT_ROW_HEIGHT_POINTS = 12.75f;

    public static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;

    public static final int UNIT_OFFSET_LENGTH = 7;

    public static final int[] UNIT_OFFSET_MAP = new int[] { 0, 36, 73, 109,
            146, 182, 219 };

    /**
     * Column width measured as the number of characters of the maximum digit
     * width of the numbers 0, 1, 2, ..., 9 as rendered in the normal style's
     * font. There are 4 pixels of margin padding (two on each side), plus 1
     * pixel padding for the gridlines.
     * 
     * This value is the same for default font in Office 2007 (Calibri) and
     * Office 2003 and earlier (Arial)
     */
    private static float DEFAULT_COLUMN_WIDTH = 9.140625f;

    /**
     * width of 1px in columns with default width in units of 1/256 of a
     * character width
     */
    private static final float PX_DEFAULT = 32.00f;

    /**
     * width of 1px in columns with overridden width in units of 1/256 of a
     * character width
     */
    private static final float PX_MODIFIED = 36.56f;

    // FIXME investigate why the default column width gets a different value
    // using this compared to ExcelToHtmlUtils.getColumnWidthInPx(widthUnits).
    protected static float getColumnWidthInPixels(Sheet sheet, int columnIndex) {
        if (sheet instanceof XSSFSheet) {
            CTCol col = ((XSSFSheet) sheet).getColumnHelper().getColumn(
                    columnIndex, false);
            double numChars = col == null || !col.isSetWidth() ? DEFAULT_COLUMN_WIDTH
                    : col.getWidth();
            return (float) numChars * XSSFWorkbook.DEFAULT_CHARACTER_WIDTH;
        } else if (sheet instanceof HSSFSheet) {
            int cw = sheet.getColumnWidth(columnIndex);
            int def = sheet.getDefaultColumnWidth() * 256;
            float px = cw == def ? PX_DEFAULT : PX_MODIFIED;
            return cw / px;
        } else {
            return ExcelToHtmlUtils.getColumnWidthInPx(sheet
                    .getColumnWidth(columnIndex));
        }
    }

    /**
     * pixel units to excel width units(units of 1/256th of a character width)
     * 
     * @param pxs
     * @return
     */
    protected static short pixel2WidthUnits(int pxs) {
        short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH));

        widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];

        return widthUnits;
    }

    protected static void loadSpreadsheetWith(Spreadsheet spreadsheet,
            Workbook workbook) {
        spreadsheet.clearSheetServerSide();
        final Sheet sheet;
        if (workbook == null) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet();
            spreadsheet.setInternalWorkbook(workbook);
            generateNewSpreadsheet(spreadsheet, sheet, DEFAULT_ROWS,
                    DEFAULT_COLUMNS);
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

    protected static void loadNewXLSXSpreadsheet(Spreadsheet spreadsheet) {
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
        loadWorkbookStyles(spreadsheet);
    }

    protected static void addNewSheet(final Spreadsheet spreadsheet,
            final Workbook workbook, final String sheetName, int rows,
            int columns) {
        final Sheet sheet;
        if (sheetName == null) {
            sheet = workbook.createSheet();
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

    protected static void reloadSpreadsheetComponent(Spreadsheet spreadsheet,
            final File spreadsheetFile) throws InvalidFormatException,
            IOException {
        reloadSpreadsheetComponent(spreadsheet,
                WorkbookFactory.create(spreadsheetFile));
    }

    protected static void reloadSpreadsheetComponent(Spreadsheet spreadsheet,
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

    protected static File write(Spreadsheet spreadsheet, String fileName)
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }

    protected static void loadWorkbookStyles(Spreadsheet component) {
        component.getSpreadsheetStyleFactory().reloadWorkbookStyles();
        component.getSpreadsheetStyleFactory().reloadActiveSheetCellStyles();
    }

    protected static void generateNewSpreadsheet(final Spreadsheet component,
            final Sheet sheet, int rows, int columns) {
        sheet.createRow(rows - 1).createCell(columns - 1);
        final float defaultRowHeightInPoints = sheet
                .getDefaultRowHeightInPoints();
        if (defaultRowHeightInPoints <= 0) {
            sheet.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT_POINTS);
        }
        // use excel default column width instead of Apache POI default (8)
        sheet.setDefaultColumnWidth(DEFAULT_COL_WIDTH_UNITS);
        reloadSpreadsheetData(component, sheet);
    }

    protected static void reloadSpreadsheetData(final Spreadsheet component,
            final Sheet sheet) {
        logMemoryUsage();
        try {
            float defaultRowHeightInPoints = sheet
                    .getDefaultRowHeightInPoints();
            if (defaultRowHeightInPoints <= 0) {
                sheet.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT_POINTS);
                component.getState().defRowH = DEFAULT_ROW_HEIGHT_POINTS;
            } else {
                component.getState().defRowH = defaultRowHeightInPoints;
            }
            component.getState().rows = (sheet.getLastRowNum() + 1);
            int charactersToPixels = ExcelToHtmlUtils.getColumnWidthInPx(sheet
                    .getDefaultColumnWidth() * 256);
            if (charactersToPixels > 0) {
                component.getState().defColW = charactersToPixels;
            } else {
                component.getState().defColW = DEFAULT_COL_WIDTH_UNITS;
                sheet.setDefaultColumnWidth(DEFAULT_COL_WIDTH_UNITS / 8);
            }
            final float[] rowHeights = new float[component.getRows()];
            int cols = 0;
            int tempRowIndex = -1;
            final ArrayList<Integer> hiddenRowIndexes = new ArrayList<Integer>();
            for (Row row : sheet) {
                int rIndex = row.getRowNum();
                // set the empty rows to have the default row width
                while (++tempRowIndex != rIndex) {
                    rowHeights[tempRowIndex] = component.getState().defRowH;
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
            component.getState().hiddenRowIndexes = hiddenRowIndexes;
            component.getState().rowH = rowHeights;
            component.getState().cols = cols;
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
            component.getState().hiddenColumnIndexes = hiddenColumnIndexes;
            component.getState().colW = colWidths;

            loadSheetImages(component);
            loadMergedRegions(component);
            loadFreezePane(component);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        logMemoryUsage();
    }

    protected static void loadSheetImages(Spreadsheet spreadsheet) {
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
                    image.anchor = anchor;
                    image.MIMEType = pictureData.getMimeType();
                    image.data = pictureData.getData();
                    if (anchor != null) {
                        spreadsheet.sheetImages.add(image);
                    } else {
                        System.err.println("IMAGE WITHOUT ANCHOR: "
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
                    image.anchor = anchor;
                    image.MIMEType = pictureData.getMimeType();
                    image.data = pictureData.getData();
                    if (anchor != null) {
                        spreadsheet.sheetImages.add(image);
                    } else {
                        System.err.println("IMAGE WITHOUT ANCHOR: "
                                + pictureData.toString());
                    }
                }
            }
        }
    }

    protected static void loadMergedRegions(Spreadsheet spreadsheet) {
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

    protected static void loadFreezePane(Spreadsheet component) {
        final Sheet sheet = component.getActiveSheet();
        PaneInformation paneInformation = sheet.getPaneInformation();
        // only freeze panes supported
        if (paneInformation != null && paneInformation.isFreezePane()) {
            // XXX WTF POI? HorizontalSplitPosition means rows and
            // VerticalSplitPosition means columns. Changed the meaning for the
            // component internals
            component.getState().horizontalSplitPosition = paneInformation
                    .getVerticalSplitPosition();
            component.getState().verticalSplitPosition = paneInformation
                    .getHorizontalSplitPosition();
        } else {
            component.getState().verticalSplitPosition = 0;
            component.getState().horizontalSplitPosition = 0;
        }
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
            System.out.println("Total: " + tot / 1000000 + " Free: " + free
                    / 1000000 + " Usage: " + (tot - free) / 1000000);
        }
    }
}
