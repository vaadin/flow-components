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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.POIXMLException;
import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFGraphicFrame;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOutlinePr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.shared.GroupingData;
import com.vaadin.addon.spreadsheet.shared.SpreadsheetState;

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
        final Sheet sheet = createNewSheet(newWorkbook);
        spreadsheet.clearSheetServerSide();
        spreadsheet.setInternalWorkbook(newWorkbook);
        generateNewSpreadsheet(spreadsheet, sheet, DEFAULT_ROWS,
                DEFAULT_COLUMNS);
        setDefaultRowHeight(spreadsheet, sheet);
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
            Workbook workbook = WorkbookFactory.create(spreadsheetFile);
            reloadSpreadsheetComponent(spreadsheet, workbook);
        } catch (POIXMLException e) {
            throw new IOException(e);
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
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        finally {
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
        setDefaultRowHeight(spreadsheet, sheet);
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
            setDefaultRowHeight(spreadsheet, sheet);
            setDefaultColumnWidth(spreadsheet, sheet);
            calculateSheetSizes(spreadsheet, sheet);
            loadSheetOverlays(spreadsheet);
            loadMergedRegions(spreadsheet);
            loadFreezePane(spreadsheet);
            loadGrouping(spreadsheet);
        } catch (NullPointerException npe) {
            LOGGER.log(Level.WARNING, npe.getMessage(), npe);
        }
        logMemoryUsage();
    }

    /**
     * Calculate size-related values for the sheet. Includes row and column
     * counts, actual row heights and column widths, and hidden row and column
     * indexes.
     * 
     * @param spreadsheet
     * @param sheet
     */
    static void calculateSheetSizes(final Spreadsheet spreadsheet,
            final Sheet sheet) {
        // Always have at least the default amount of rows
        int rows = sheet.getLastRowNum() + 1;
        if (rows < spreadsheet.getDefaultRowCount()) {
            rows = spreadsheet.getDefaultRowCount();
        }
        spreadsheet.getState().rows = rows;

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
            float defaultRowHeightInPoints = sheet
                    .getDefaultRowHeightInPoints();

            int lastRowNum = sheet.getLastRowNum();
            // if sheet is empty, also set height for 'last row' (index
            // zero)
            if (lastRowNum == 0) {
                rowHeights[0] = defaultRowHeightInPoints;
            }

            // set default height for the rest
            for (int i = lastRowNum + 1; i < rows; i++) {
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
    }

    /**
     * Loads all data relating to grouping if the current sheet is a
     * {@link XSSFSheet}.
     */
    static void loadGrouping(Spreadsheet spreadsheet) {

        if (spreadsheet.getActiveSheet() instanceof HSSFSheet) {
            // API not available
            return;
        }

        CTWorksheet ctWorksheet = ((XSSFSheet) spreadsheet.getActiveSheet())
                .getCTWorksheet();
        CTSheetProtection sheetProtection = ctWorksheet.getSheetProtection();
        if (sheetProtection != null) {
            spreadsheet.getState().lockFormatColumns = sheetProtection
                    .getFormatColumns();
            spreadsheet.getState().lockFormatRows = sheetProtection
                    .getFormatRows();
        }

        spreadsheet.getState().colGroupingMax = 0;
        spreadsheet.getState().rowGroupingMax = 0;

        if (ctWorksheet.getSheetPr() != null
                && ctWorksheet.getSheetPr().getOutlinePr() != null) {
            CTOutlinePr outlinePr = ctWorksheet.getSheetPr().getOutlinePr();
            spreadsheet.getState().colGroupingInversed = !outlinePr
                    .getSummaryRight();
            spreadsheet.getState().rowGroupingInversed = !outlinePr
                    .getSummaryBelow();
        } else {
            spreadsheet.getState().colGroupingInversed = false;
            spreadsheet.getState().rowGroupingInversed = false;
        }

        // COLS

        CTCols colsArray = ctWorksheet.getColsArray(0);

        /*
         * Columns are grouped so that columns that are beside each other and
         * share properties have a single CTCol with a min and max index.
         * 
         * A column that is part of a group has an outline level. Each col also
         * has a property called 'collapsed', which doesn't appear to be used
         * for anything. If a group is collapsed, each col in the group has its
         * 'visibility' property set to false.
         */

        List<GroupingData> data = new ArrayList<GroupingData>();

        short lastlevel = 0;
        CTCol prev = null;
        for (CTCol col : colsArray.getColList()) {

            if (prev != null && prev.getMax() + 1 < col.getMin()) {
                // break in cols, reset level
                lastlevel = 0;
            }

            if (col.getOutlineLevel() > lastlevel) {

                // new group starts

                // multiple groups might start on the same column, go through
                // each in order
                while (lastlevel != col.getOutlineLevel()) {

                    lastlevel++;
                    if (spreadsheet.getState(false).colGroupingMax < lastlevel) {
                        spreadsheet.getState().colGroupingMax = lastlevel;
                    }

                    // do not add children of collapsed groups
                    if (!data.isEmpty()) {
                        GroupingData previous = data.get(data.size() - 1);
                        if (previous.collapsed
                                && previous.endIndex >= col.getMin()
                                && previous.level < col.getOutlineLevel()) {

                            continue;
                        }
                    }

                    boolean columnHidden = GroupingUtil.checkHidden(colsArray,
                            col, lastlevel);

                    long end = GroupingUtil.findEndOfColGroup(colsArray, col,
                            lastlevel) - 1;
                    long unique = GroupingUtil.findUniqueColIndex(colsArray,
                            col, lastlevel) - 1;
                    GroupingData d = new GroupingData(col.getMin() - 1, end,
                            lastlevel, unique, columnHidden);
                    data.add(d);

                }

            } else if (col.getOutlineLevel() < lastlevel) {
                // groups end
                lastlevel = col.getOutlineLevel();
            }

            prev = col;
        }

        /*
         * There is a Excel data model inconsistency here. Technically, multiple
         * groups can start or end on the same column. However, the
         * collapse/expanded property is stored only as a boolean on the column;
         * if there are multiple groups in one column, there is no way to know
         * which of the groups is collapsed and which isn't, since there is only
         * one boolean value. The way Excel 'solves' this is to not render the
         * lower level groups fully in this particular case (the line and expand
         * button are not visible). So, let's not display them here either.
         */
        Set<GroupingData> toRemove = new HashSet<GroupingData>();
        for (int i = 0; i < data.size(); i++) {
            for (int j = i + 1; j < data.size(); j++) {
                GroupingData d1 = data.get(i);
                GroupingData d2 = data.get(j);

                if (spreadsheet.getState().colGroupingInversed) {
                    if (d1.startIndex == d2.startIndex) {
                        toRemove.add(d2);
                    }
                } else {
                    if (d1.endIndex == d2.endIndex) {
                        toRemove.add(d2);
                    }
                }
            }
        }

        data.removeAll(toRemove);

        spreadsheet.getState().colGroupingData = data;

        // ROWS

        data = new ArrayList<GroupingData>();

        /*
         * Each row that has data (or grouping props) exists separately, they
         * are not grouped like columns.
         * 
         * Each row that is part of a group has a set outline level. Unlike
         * cols, the 'collapse' property is actually used for rows, in
         * conjuction with the 'hidden' prop. If a group is collapsed, each row
         * in the group has its 'hidden' prop set to true. Also, the column
         * after the group (or before, if inverted) has its 'collapsed' property
         * set to true.
         */

        Stack<GroupingData> rows = new Stack<GroupingData>();
        lastlevel = 0;
        for (int i = 0; i <= spreadsheet.getRows(); i++) {

            XSSFRow row = (XSSFRow) spreadsheet.getActiveSheet().getRow(i);
            if (row == null || row.getCTRow().getOutlineLevel() < lastlevel) {
                // end any groups

                short level;
                if (row == null) {
                    level = 0;
                } else {
                    level = row.getCTRow().getOutlineLevel();
                }

                GroupingData g = null;
                while (level != lastlevel) {
                    g = rows.pop();
                    lastlevel--;

                    boolean collapsed = false;
                    if (spreadsheet.getState().rowGroupingInversed) {
                        // marker is before group
                        XSSFRow r = (XSSFRow) spreadsheet.getActiveSheet()
                                .getRow(g.startIndex - 1);
                        if (r != null) {
                            collapsed = r.getCTRow().getCollapsed();
                        }
                    } else if (row != null) {
                        // collapse marker is after group, so it is on this
                        // row
                        collapsed = row.getCTRow().getCollapsed();
                    }

                    g.collapsed = collapsed;

                    // remove children of collapsed parent
                    if (collapsed) {
                        toRemove = new HashSet<GroupingData>();
                        for (GroupingData d : data) {
                            if (d.startIndex >= g.startIndex
                                    && d.endIndex <= g.endIndex
                                    && d.level > g.level) {
                                toRemove.add(d);
                            }
                        }
                        data.removeAll(toRemove);
                    }
                    data.add(g);
                }
                continue;
            }

            short level = row.getCTRow().getOutlineLevel();

            if (level > lastlevel) {
                // group start

                // possibly many groups start here
                while (level != lastlevel) {
                    lastlevel++;

                    int end = (int) GroupingUtil.findEndOfRowGroup(spreadsheet,
                            i, row, lastlevel);
                    long uniqueIndex = GroupingUtil.findUniqueRowIndex(
                            spreadsheet, i, end, lastlevel);

                    GroupingData d = new GroupingData(i, end, lastlevel,
                            uniqueIndex, false);

                    rows.push(d);

                    if (spreadsheet.getState(false).rowGroupingMax < d.level) {
                        spreadsheet.getState().rowGroupingMax = d.level;
                    }

                }
            }

        }

        /*
         * Same issue as with groups starting or ending on same row, only
         * process top level one.
         */
        toRemove = new HashSet<GroupingData>();
        for (int i = 0; i < data.size(); i++) {
            for (int j = i + 1; j < data.size(); j++) {
                GroupingData d1 = data.get(i);
                GroupingData d2 = data.get(j);

                if (spreadsheet.getState().rowGroupingInversed) {
                    if (d1.startIndex == d2.startIndex) {
                        toRemove.add(d2);
                    }
                } else {
                    if (d1.endIndex == d2.endIndex) {
                        toRemove.add(d2);
                    }
                }
            }
        }

        data.removeAll(toRemove);

        spreadsheet.getState().rowGroupingData = data;
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
        Drawing drawing = getDrawingPatriarch(sheet);

        if (drawing instanceof XSSFDrawing) {
            for (XSSFShape shape : ((XSSFDrawing) drawing).getShapes()) {
                SheetOverlayWrapper overlayWrapper = null;

                if (spreadsheet.isChartsEnabled()
                        && shape instanceof XSSFGraphicFrame) {
                        overlayWrapper = tryLoadChart(spreadsheet, drawing,
                                (XSSFGraphicFrame) shape);
                }
                if (shape instanceof XSSFPicture) {
                    overlayWrapper = loadXSSFPicture((XSSFPicture) shape);
                }

                if (overlayWrapper != null) {
                    if (overlayWrapper.getAnchor() != null) {
                        spreadsheet.addSheetOverlay(overlayWrapper);
                    } else {
                        LOGGER.log(Level.FINE, "IMAGE WITHOUT ANCHOR: "
                                + overlayWrapper);

                        // FIXME seems like there is a POI bug, images that have
                        // in Excel (XLSX) been se as a certain type (type==3)
                        // will get a null anchor.
                        // Achor types:
                        // 0 = Move and size with Cells,
                        // 2 = Move but don't size with cells,
                        // 3 = Don't move or size with cells.

                        // Michael: maybe it's okay, if they are not moved or
                        // sized with cells, how can there be an anchor? Their
                        // position is probably defined somehow else.
                    }
                }

            }
        } else if (drawing instanceof HSSFPatriarch) {
            for (HSSFShape shape : ((HSSFPatriarch) drawing).getChildren()) {
                if (shape instanceof HSSFPicture) {
                    loadHSSFPicture(spreadsheet, shape);
                }
            }
        }
    }

    private static void loadHSSFPicture(Spreadsheet spreadsheet, HSSFShape shape) {
        HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
        HSSFPictureData pictureData = ((HSSFPicture) shape).getPictureData();
        if (anchor != null) {
            SheetImageWrapper image = new SheetImageWrapper(anchor,
                    pictureData.getMimeType(), pictureData.getData());
            spreadsheet.addSheetOverlay(image);
        } else {
            LOGGER.log(Level.FINE,
                    "IMAGE WITHOUT ANCHOR: " + pictureData.toString());
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
    private static SheetChartWrapper tryLoadChart(
            final Spreadsheet spreadsheet, final Drawing drawing,
            final XSSFGraphicFrame frame) {
        try {
            XSSFChart chartXml = getChartForFrame((XSSFDrawing) drawing, frame);

            if (chartXml != null) {
                XSSFClientAnchor anchor = getAnchorFromParent(frame
                        .getCTGraphicalObjectFrame());

                return new SheetChartWrapper(anchor, chartXml, spreadsheet);
            }
        } catch (NullPointerException e) {
            // means we did not find any chart for this drawing (not an error,
            // normal situation) or we could not load it (corrupt file?
            // unrecognized format?), nothing to do.
        }

        return null;
    }

    /**
     * Copy-pasted from XSSFDrawing (private there) with slight modifications.
     * Used to get anchors from an XSSFShape's parent.
     */
    private static XSSFClientAnchor getAnchorFromParent(XmlObject obj) {
        XSSFClientAnchor anchor = null;

        XmlObject parentXbean = null;
        XmlCursor cursor = obj.newCursor();
        if (cursor.toParent()) {
            parentXbean = cursor.getObject();
        }
        cursor.dispose();
        if (parentXbean != null) {
            if (parentXbean instanceof CTTwoCellAnchor) {
                CTTwoCellAnchor ct = (CTTwoCellAnchor) parentXbean;
                anchor = new XSSFClientAnchor((int) ct.getFrom().getColOff(),
                        (int) ct.getFrom().getRowOff(), (int) ct.getTo()
                                .getColOff(), (int) ct.getTo().getRowOff(), ct
                                .getFrom().getCol(), ct.getFrom().getRow(), ct
                                .getTo().getCol(), ct.getTo().getRow());
            } else if (parentXbean instanceof CTOneCellAnchor) {
                CTOneCellAnchor ct = (CTOneCellAnchor) parentXbean;
                anchor = new XSSFClientAnchor((int) ct.getFrom().getColOff(),
                        (int) ct.getFrom().getRowOff(), 0, 0, ct.getFrom()
                                .getCol(), ct.getFrom().getRow(), 0, 0);
            }
        }
        return anchor;
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
    private static Drawing getDrawingPatriarch(Sheet sheet) {
        if (sheet instanceof XSSFSheet) {
            return ((XSSFSheet) sheet).getDrawingPatriarch();
        } else if (sheet instanceof HSSFSheet) {
            return ((HSSFSheet) sheet).getDrawingPatriarch();
        } else {
            return null;
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

            /*
             * In POI, HorizontalSplit means rows and VerticalSplit means columns.
             *
             * In Spreadsheet the meaning is the opposite.
             */
            spreadsheet.getState().horizontalSplitPosition = paneInformation
                .getVerticalSplitPosition() + sheet.getLeftCol();

            spreadsheet.getState().verticalSplitPosition = paneInformation
                .getHorizontalSplitPosition() + sheet.getTopRow();

            /*
             * If the view was scrolled down / right when panes were frozen, the
             * invisible frozen rows/columns are effectively hidden in Excel. We
             * mimic this behavior here.
             */
            for (int col = 0; col < sheet.getLeftCol(); col++) {
                spreadsheet.setColumnHidden(col, true);
            }
            for (int row = 0; row < sheet.getTopRow(); row++) {
                spreadsheet.setRowHidden(row, true);
            }
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

    private static void setDefaultRowHeight(Spreadsheet spreadsheet,
            final Sheet sheet) {
        float defaultRowHeightInPoints = sheet.getDefaultRowHeightInPoints();
        if (defaultRowHeightInPoints <= 0) {
            sheet.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT_POINTS);
            spreadsheet.getState().defRowH = DEFAULT_ROW_HEIGHT_POINTS;
        } else {
            spreadsheet.getState().defRowH = defaultRowHeightInPoints;
        }
    }

    private static void setDefaultColumnWidth(Spreadsheet spreadsheet,
            final Sheet sheet) {
        int charactersToPixels = ExcelToHtmlUtils.getColumnWidthInPx(sheet
                .getDefaultColumnWidth() * 256);
        if (charactersToPixels > 0) {
            spreadsheet.getState().defColW = charactersToPixels;
        } else {
            spreadsheet.getState().defColW = SpreadsheetUtil
                    .getDefaultColumnWidthInPx();
            sheet.setDefaultColumnWidth(DEFAULT_COL_WIDTH_UNITS);
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
            LOGGER.log(Level.INFO, "Total: " + tot / 1000000 + " Free: " + free
                    / 1000000 + " Usage: " + (tot - free) / 1000000);
        }
    }
}
