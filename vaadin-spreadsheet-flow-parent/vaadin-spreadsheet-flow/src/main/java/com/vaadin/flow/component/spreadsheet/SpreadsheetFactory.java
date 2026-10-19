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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFGraphicFrame;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpreadsheetFactory is an utility class of the Spreadsheet component. It is
 * used for operations related to loading and saving a workbook and related
 * data.
 *
 * @author Vaadin Ltd.
 * @since 23.1
 */
public class SpreadsheetFactory implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpreadsheetFactory.class);

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
        if (file.exists()) {
            // If the file exists beforehand, it needs to be deleted first
            file.delete();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        if (workbook instanceof SXSSFWorkbook sxssf) {
            // Writing uses a streaming workbook up: the one shown is read
            // back from the file. Any other stays as it is, and so does what
            // the application set on it, such as pop-up buttons.
            dispose(sxssf);
            spreadsheet.loader().show(WorkbookFactory.create(file));
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
            if (workbook instanceof SXSSFWorkbook sxssf) {
                dispose(sxssf);
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    static void loadNamedRanges(Spreadsheet spreadsheet) {
        final List<? extends Name> namedRanges = spreadsheet.getWorkbook()
                .getAllNames();

        final List<String> names = new ArrayList<String>();

        for (Name name : namedRanges) {
            if (!isNameSelectable(name)) {
                continue;
            }

            final int nameLocalTo = name.getSheetIndex();
            final int activeSheet = spreadsheet.getWorkbook()
                    .getActiveSheetIndex();

            if (nameLocalTo == -1 || nameLocalTo == activeSheet) {
                names.add(name.getNameName());
            }
        }

        spreadsheet.sheetModel().namedRanges.set(names);
    }

    private static boolean isNameSelectable(Name name) {
        if (name.isFunctionName()) {
            return false;
        }

        if (!AreaReference.isContiguous(name.getRefersToFormula())) {
            return false;
        }

        // a workaround for https://bz.apache.org/bugzilla/show_bug.cgi?id=61701
        try {
            name.getSheetName();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Load the sheet filter and tables in the given sheet
     *
     * @param spreadsheet
     *            Target Spreadsheet
     */
    static void loadSheetTables(Spreadsheet spreadsheet) {
        if (!(spreadsheet.getActiveSheet() instanceof XSSFSheet sheet)) {
            return;
        }
        CTAutoFilter autoFilter = sheet.getCTWorksheet().getAutoFilter();

        if (autoFilter != null
                && !tableForCTAutoFilterAlreadyLoaded(spreadsheet,
                        autoFilter)) {
            CellRangeAddress range = CellRangeAddress
                    .valueOf(autoFilter.getRef());
            if (hasRowsToFilter(sheet, range)) {
                SpreadsheetTable sheetFilterTable = new SpreadsheetFilterTable(
                        spreadsheet, spreadsheet.getActiveSheet(), range,
                        autoFilter, null);

                spreadsheet.registerTable(sheetFilterTable);

                markActiveButtons(sheetFilterTable, autoFilter);
            }
        }

        for (XSSFTable table : sheet.getTables()) {
            if (!tableForXSSFTableAlreadyLoaded(spreadsheet, table)) {
                CellRangeAddress range = CellRangeAddress
                        .valueOf(table.getCTTable().getRef());
                if (hasRowsToFilter(sheet, range)) {
                    SpreadsheetTable spreadsheetTable = new SpreadsheetFilterTable(
                            spreadsheet, spreadsheet.getActiveSheet(), range,
                            null, table);

                    spreadsheet.registerTable(spreadsheetTable);
                }
            }
        }
    }

    /**
     * Whether a filter or a table has rows under its header row, which a filter
     * table filters. One of its header row alone, as a workbook with headers
     * and no data yet has, is not shown; the workbook keeps it.
     */
    private static boolean hasRowsToFilter(Sheet sheet,
            CellRangeAddress range) {
        if (range.getFirstRow() < range.getLastRow()) {
            return true;
        }
        LOGGER.warn(
                "The filter of {} on sheet {} has no rows under its header"
                        + " row, and is not shown",
                range.formatAsString(), sheet.getSheetName());
        return false;
    }

    private static boolean tableForXSSFTableAlreadyLoaded(
            Spreadsheet spreadsheet, XSSFTable table) {
        return spreadsheet.getTables().stream()
                .anyMatch(it -> it.getXssfTable() == table);
    }

    private static boolean tableForCTAutoFilterAlreadyLoaded(
            Spreadsheet spreadsheet, CTAutoFilter autoFilter) {
        return spreadsheet.getTables().stream()
                .anyMatch(it -> it.getCtWorksheetAutoFilter() == autoFilter);
    }

    private static void markActiveButtons(SpreadsheetTable sheetFilterTable,
            CTAutoFilter autoFilter) {

        final int offset = sheetFilterTable.getFullTableRegion()
                .getFirstColumn();

        for (CTFilterColumn column : autoFilter.getFilterColumnList()) {
            final int colId = offset + (int) column.getColId();
            sheetFilterTable.getPopupButton(colId).markActive(true);
        }
    }

    /**
     * Gives the client what the protection of the sheet shown locks: resizing
     * its columns and its rows. Nothing is locked on a sheet not protected, nor
     * on an HSSF one, whose protection POI does not read that far.
     */
    static void loadSheetProtection(Spreadsheet spreadsheet) {
        CTSheetProtection protection = spreadsheet
                .getActiveSheet() instanceof XSSFSheet xssfSheet
                        ? xssfSheet.getCTWorksheet().getSheetProtection()
                        : null;
        boolean isProtected = protection != null && protection.getSheet();
        spreadsheet.sheetModel().lockFormatColumns
                .set(isProtected && protection.getFormatColumns());
        spreadsheet.sheetModel().lockFormatRows
                .set(isProtected && protection.getFormatRows());
    }

    /**
     * Loads overlays for the currently active sheet and adds them to the target
     * Spreadsheet.
     *
     * @param spreadsheet
     *            Target Spreadsheet
     */
    static void loadSheetOverlays(Spreadsheet spreadsheet) {
        final Sheet sheet = spreadsheet.getActiveSheet();
        Drawing<?> drawing = getDrawingPatriarch(sheet);

        if (drawing instanceof XSSFDrawing xssfDrawing) {
            for (XSSFShape shape : xssfDrawing.getShapes()) {
                SheetOverlayWrapper overlayWrapper = null;

                if (spreadsheet.isChartsEnabled()
                        && shape instanceof XSSFGraphicFrame frame) {
                    overlayWrapper = tryLoadChart(spreadsheet, drawing, frame);
                }
                if (shape instanceof XSSFPicture picture) {
                    overlayWrapper = loadXSSFPicture(picture);
                }

                if (overlayWrapper != null) {
                    if (overlayWrapper.getAnchor() != null) {
                        spreadsheet.addSheetOverlay(overlayWrapper);
                    } else {
                        LOGGER.debug("IMAGE WITHOUT ANCHOR: " + overlayWrapper);

                        // FIXME seems like there is a POI bug, images that have
                        // in Excel (XLSX) been set as a certain type (type==3)
                        // will get a null anchor.
                        // Anchor types:
                        // 0 = Move and size with Cells,
                        // 2 = Move but don't size with cells,
                        // 3 = Don't move or size with cells.

                        // Michael: maybe it's okay, if they are not moved or
                        // sized with cells, how can there be an anchor? Their
                        // position is probably defined somehow else.
                    }
                }

            }
        } else if (drawing instanceof HSSFPatriarch patriarch) {
            for (HSSFShape shape : patriarch.getChildren()) {
                if (shape instanceof HSSFPicture picture) {
                    loadHSSFPicture(spreadsheet, picture);
                }
            }
        }
    }

    private static void loadHSSFPicture(Spreadsheet spreadsheet,
            HSSFPicture picture) {
        HSSFClientAnchor anchor = (HSSFClientAnchor) picture.getAnchor();
        HSSFPictureData pictureData = picture.getPictureData();
        if (anchor != null) {
            SheetImageWrapper image = new SheetImageWrapper(anchor,
                    pictureData.getMimeType(), pictureData.getData());
            spreadsheet.addSheetOverlay(image);
        } else {
            LOGGER.debug("IMAGE WITHOUT ANCHOR: " + pictureData.toString());
        }
    }

    private static SheetImageWrapper loadXSSFPicture(XSSFPicture shape) {
        // in XSSFPicture.getPreferredSize(double) POI presumes that
        // XSSFAnchor is always of type XSSFClientAnchor
        XSSFClientAnchor anchor = (XSSFClientAnchor) shape.getAnchor();

        XSSFPictureData pictureData = shape.getPictureData();

        SheetImageWrapper image = new SheetImageWrapper(anchor,
                pictureData.getMimeType(), pictureData.getData());

        return image;
    }

    /**
     * Returns a chart wrapper if this drawing has a chart, otherwise null.
     */
    private static SheetChartWrapper tryLoadChart(final Spreadsheet spreadsheet,
            final Drawing<?> drawing, final XSSFGraphicFrame frame) {
        try {
            XSSFChart chartXml = getChartForFrame((XSSFDrawing) drawing, frame);

            if (chartXml != null) {
                // removed old anchor lookup, as it was wrong for some Excel
                // files.
                // anchor can be referenced directly from XSSFChart.
                return new SheetChartWrapper(chartXml, spreadsheet);
            }
        } catch (NullPointerException e) {
            // means we did not find any chart for this drawing (not an error,
            // normal situation) or we could not load it (corrupt file?
            // unrecognized format?), nothing to do.
        }

        return null;
    }

    /**
     * Returns a chart or null if this frame doesn't have one.
     */
    private static XSSFChart getChartForFrame(XSSFDrawing drawing,
            XSSFGraphicFrame frame) {
        // the chart is supposed to be there if an ID is found
        return (XSSFChart) drawing.getRelationById(getChartId(frame));
    }

    private static String getChartId(XSSFGraphicFrame frame) {
        return frame.getCTGraphicalObjectFrame().getGraphic().getGraphicData()
                .getDomNode().getChildNodes().item(0).getAttributes()
                .getNamedItem("r:id").getNodeValue();
    }

    /*
     * The getDrawingPatriarch() method is missing from the interface, so we
     * have to check each implementation. SXSSFSheet is unsupported.
     */
    private static Drawing<?> getDrawingPatriarch(Sheet sheet) {
        if (sheet instanceof XSSFSheet xssf) {
            return xssf.getDrawingPatriarch();
        } else if (sheet instanceof HSSFSheet hssf) {
            return hssf.getDrawingPatriarch();
        }
        return null;
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

            // With a large sheet, getTopRow could become negative.
            var topRow = Math.max(0, sheet.getTopRow());
            var leftCol = Math.max(0, sheet.getLeftCol());
            var vSplit = paneInformation.getVerticalSplitPosition();
            var hSplit = paneInformation.getHorizontalSplitPosition();

            /*
             * In POI, HorizontalSplit means rows and VerticalSplit means
             * columns.
             *
             * In Spreadsheet the meaning is the opposite.
             *
             * POI uses a split position of 0 to mean "no split", so we should
             * not be setting split positions or hiding anything in the case
             * this position is 0.
             *
             * If the view was scrolled down / right when panes were frozen, the
             * invisible frozen rows/columns are effectively hidden in Excel. We
             * attempt to mimic this behavior here by actually hiding the
             * rows/columns, but only if there actually was a split.
             */
            if (vSplit > 0) {
                spreadsheet.sheetModel().horizontalSplitPosition
                        .set(vSplit + leftCol);
                if (leftCol > 0) {
                    spreadsheet.setColumnsHidden(IntStream.range(0, leftCol)
                            .boxed().collect(Collectors.toMap(
                                    Function.identity(), index -> true)));
                }
            } else {
                spreadsheet.sheetModel().horizontalSplitPosition.set(0);
                if (leftCol > 0) {
                    // TODO: should scroll horizontally to restore viewport
                    // state. This needs API on the Spreadsheet side
                }
            }

            if (hSplit > 0) {
                spreadsheet.sheetModel().verticalSplitPosition
                        .set(hSplit + topRow);
                if (topRow > 0) {
                    spreadsheet.setRowsHidden(IntStream.range(0, topRow).boxed()
                            .collect(Collectors.toMap(Function.identity(),
                                    index -> true)));
                }
            } else {
                spreadsheet.sheetModel().verticalSplitPosition.set(0);
                if (topRow > 0) {
                    // TODO: should scroll vertically to restore viewport
                    // state. This needs API on the Spreadsheet side
                }
            }
        } else {
            spreadsheet.sheetModel().verticalSplitPosition.set(0);
            spreadsheet.sheetModel().horizontalSplitPosition.set(0);
        }

    }

    /**
     * Deletes the temporary files a streaming workbook keeps on disk, which
     * leaves it unusable. Not with {@code close()}, which POI suggests in its
     * place: that also closes the workbook the streaming one wraps, which can
     * be the application's own, and a workbook opened from a file is written
     * back to the file as it closes.
     */
    @SuppressWarnings("deprecation")
    static void dispose(SXSSFWorkbook workbook) {
        workbook.dispose();
    }

    /**
     * Does nothing.
     *
     * @deprecated Does nothing, and no replacement is planned.
     */
    @Deprecated(since = "25.4")
    public static void logMemoryUsage() {
        // The flag that turned the logging on was never set.
    }
}
