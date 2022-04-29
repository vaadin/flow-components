package com.vaadin.flow.component.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.util.Units;
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
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOutlinePr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.shared.GroupingData;

/**
 * SpreadsheetFactory is an utility class of the Spreadsheet component. It is
 * used for operations related to loading and saving a workbook and related
 * data.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
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
                workbook.setActiveSheet(
                        SpreadsheetUtil.getFirstVisibleSheetPOIIndex(workbook));
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
        int[] verticalScrollPositions = Arrays.copyOf(
                spreadsheet.getVerticalScrollPositions(),
                spreadsheet.getSheetNames().length);
        int[] horizontalScrollPositions = Arrays.copyOf(
                spreadsheet.getHorizontalScrollPositions(),
                spreadsheet.getSheetNames().length);
        spreadsheet.setVerticalScrollPositions(verticalScrollPositions);
        spreadsheet.setHorizontalScrollPositions(horizontalScrollPositions);
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
        reloadSpreadsheetComponent(spreadsheet,
                WorkbookFactory.create(inputStream));
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
            LOGGER.warn(e.getMessage(), e);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        Workbook wb = WorkbookFactory.create(file);
        spreadsheet.setInternalWorkbook(wb);
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
            loadSheetTables(spreadsheet);
            loadMergedRegions(spreadsheet);
            loadFreezePane(spreadsheet);
            loadGrouping(spreadsheet);
            loadNamedRanges(spreadsheet);
        } catch (NullPointerException npe) {
            LOGGER.warn(npe.getMessage(), npe);
        }
        logMemoryUsage();
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

        spreadsheet.setNamedRanges(names);
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
    private static void loadSheetTables(Spreadsheet spreadsheet) {
        if (spreadsheet.getActiveSheet() instanceof HSSFSheet)
            return;

        XSSFSheet sheet = (XSSFSheet) spreadsheet.getActiveSheet();
        CTAutoFilter autoFilter = sheet.getCTWorksheet().getAutoFilter();

        if (autoFilter != null) {
            SpreadsheetTable sheetFilterTable = new SpreadsheetFilterTable(
                    spreadsheet, CellRangeAddress.valueOf(autoFilter.getRef()));

            spreadsheet.registerTable(sheetFilterTable);

            markActiveButtons(sheetFilterTable, autoFilter);
        }

        for (XSSFTable table : sheet.getTables()) {
            SpreadsheetTable spreadsheetTable = new SpreadsheetFilterTable(
                    spreadsheet,
                    CellRangeAddress.valueOf(table.getCTTable().getRef()));

            spreadsheet.registerTable(spreadsheetTable);
        }
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
        spreadsheet.setRows(rows);

        final float[] rowHeights = new float[rows];
        int cols = 0;
        int tempRowIndex = -1;
        final ArrayList<Integer> hiddenRowIndexes = new ArrayList<Integer>();
        for (Row row : sheet) {
            int rIndex = row.getRowNum();
            // set the empty rows to have the default row width
            while (++tempRowIndex != rIndex) {
                rowHeights[tempRowIndex] = spreadsheet.getDefRowH();
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
        spreadsheet.setHiddenRowIndexes(hiddenRowIndexes);
        spreadsheet.setRowH(rowHeights);

        // Always have at least the default amount of columns
        if (cols < spreadsheet.getDefaultColumnCount()) {
            cols = spreadsheet.getDefaultColumnCount();
        }
        spreadsheet.setCols(cols);

        final int[] colWidths = new int[cols];
        final ArrayList<Integer> hiddenColumnIndexes = new ArrayList<Integer>();
        for (int i = 0; i < cols; i++) {
            if (sheet.isColumnHidden(i)) {
                colWidths[i] = 0;
                hiddenColumnIndexes.add(i + 1);
            } else {
                colWidths[i] = (int) sheet.getColumnWidthInPixels(i);
            }
        }
        spreadsheet.setHiddenColumnIndexes(hiddenColumnIndexes);
        spreadsheet.setColW(colWidths);
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
            spreadsheet
                    .setLockFormatColumns(sheetProtection.getFormatColumns());
            spreadsheet.setLockFormatRows(sheetProtection.getFormatRows());
        }

        spreadsheet.setColGroupingMax(0);
        spreadsheet.setRowGroupingMax(0);

        if (ctWorksheet.getSheetPr() != null
                && ctWorksheet.getSheetPr().getOutlinePr() != null) {
            CTOutlinePr outlinePr = ctWorksheet.getSheetPr().getOutlinePr();
            spreadsheet.setColGroupingInversed(!outlinePr.getSummaryRight());
            spreadsheet.setRowGroupingInversed(!outlinePr.getSummaryBelow());
        } else {
            spreadsheet.setColGroupingInversed(false);
            spreadsheet.setRowGroupingInversed(false);
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
                    if (spreadsheet.getColGroupingMax() < lastlevel) {
                        spreadsheet.setColGroupingMax(lastlevel);
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

                if (spreadsheet.isColGroupingInversed()) {
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

        spreadsheet.setColGroupingData(data);

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
                    if (spreadsheet.isRowGroupingInversed()) {
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
                    long uniqueIndex = GroupingUtil
                            .findUniqueRowIndex(spreadsheet, i, end, lastlevel);

                    GroupingData d = new GroupingData(i, end, lastlevel,
                            uniqueIndex, false);

                    rows.push(d);

                    if (spreadsheet.getRowGroupingMax() < d.level) {
                        spreadsheet.setRowGroupingMax(d.level);
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

                if (spreadsheet.isRowGroupingInversed()) {
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

        spreadsheet.setRowGroupingData(data);
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
                        LOGGER.debug("IMAGE WITHOUT ANCHOR: " + overlayWrapper);

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

    private static void loadHSSFPicture(Spreadsheet spreadsheet,
            HSSFShape shape) {
        HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
        HSSFPictureData pictureData = ((HSSFPicture) shape).getPictureData();
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
                        (int) ct.getFrom().getRowOff(),
                        (int) ct.getTo().getColOff(),
                        (int) ct.getTo().getRowOff(), ct.getFrom().getCol(),
                        ct.getFrom().getRow(), ct.getTo().getCol(),
                        ct.getTo().getRow());
            } else if (parentXbean instanceof CTOneCellAnchor) {
                CTOneCellAnchor ct = (CTOneCellAnchor) parentXbean;
                anchor = new XSSFClientAnchor((int) ct.getFrom().getColOff(),
                        (int) ct.getFrom().getRowOff(), 0, 0,
                        ct.getFrom().getCol(), ct.getFrom().getRow(), 0, 0);
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
    private static Drawing<?> getDrawingPatriarch(Sheet sheet) {
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
        spreadsheet.setMergedRegions(null);
        spreadsheet.mergedRegionCounter = 0;
        int numMergedRegions = sheet.getNumMergedRegions();
        if (numMergedRegions > 0) {
            ArrayList<MergedRegion> _mergedRegions = new ArrayList<MergedRegion>(
                    numMergedRegions);
            for (int i = 0; i < numMergedRegions; i++) {
                CellRangeAddress cra = sheet.getMergedRegion(i);
                MergedRegion mergedRegion = new MergedRegion();
                mergedRegion.col1 = cra.getFirstColumn() + 1;
                mergedRegion.col2 = cra.getLastColumn() + 1;
                mergedRegion.row1 = cra.getFirstRow() + 1;
                mergedRegion.row2 = cra.getLastRow() + 1;
                mergedRegion.id = spreadsheet.mergedRegionCounter++;
                _mergedRegions.add(mergedRegion);
            }
            spreadsheet.setMergedRegions(_mergedRegions);
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
             * In POI, HorizontalSplit means rows and VerticalSplit means
             * columns.
             *
             * In Spreadsheet the meaning is the opposite.
             */
            spreadsheet.setHorizontalSplitPosition(
                    paneInformation.getVerticalSplitPosition()
                            + sheet.getLeftCol());

            spreadsheet.setVerticalSplitPosition(
                    paneInformation.getHorizontalSplitPosition()
                            + sheet.getTopRow());

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
            spreadsheet.setVerticalSplitPosition(0);
            spreadsheet.setHorizontalSplitPosition(0);
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
            spreadsheet.setDefRowH(DEFAULT_ROW_HEIGHT_POINTS);
        } else {
            spreadsheet.setDefRowH(defaultRowHeightInPoints);
        }
    }

    private static void setDefaultColumnWidth(Spreadsheet spreadsheet,
            final Sheet sheet) {

        // Formula taken from XSSFSheet.getColumnWidthInPixels
        int charactersToPixels = (int) (sheet.getDefaultColumnWidth() / 256.0
                * Units.DEFAULT_CHARACTER_WIDTH);

        if (charactersToPixels > 0) {
            spreadsheet.setDefColW(charactersToPixels);
        } else {
            spreadsheet.setDefColW(SpreadsheetUtil.getDefaultColumnWidthInPx());
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
            LOGGER.info("Total: " + tot / 1000000 + " Free: " + free / 1000000
                    + " Usage: " + (tot - free) / 1000000);
        }
    }
}
