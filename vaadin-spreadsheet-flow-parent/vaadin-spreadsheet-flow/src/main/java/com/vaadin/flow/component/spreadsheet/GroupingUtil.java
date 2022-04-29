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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that contains modified {@link XSSFSheet} methods regarding grouping.
 * The public methods here are entrypoints, other methods are copied only if
 * they needed changes.
 *
 * @author Thomas Mattsson / Vaadin Ltd.
 */
class GroupingUtil implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GroupingUtil.class);

    private GroupingUtil() {
    }

    public static void expandRow(XSSFSheet sheet, int rowNumber) {
        if (rowNumber == -1) {
            return;
        }
        XSSFRow row = sheet.getRow(rowNumber);
        // If it is already expanded do nothing.
        if (!row.getCTRow().isSetHidden()) {
            return;
        }

        // Find the start of the group.
        int startIdx = findStartOfRowOutlineGroup(sheet, rowNumber);

        // Find the end of the group.
        int endIdx = findEndOfRowOutlineGroup(sheet, rowNumber);

        // expand:
        // collapsed must be unset
        // hidden bit gets unset _if_ surrounding groups are expanded you can
        // determine
        // this by looking at the hidden bit of the enclosing group. You will
        // have
        // to look at the start and the end of the current group to determine
        // which
        // is the enclosing group
        // hidden bit only is altered for this outline level. ie. don't
        // un-collapse contained groups
        short level = row.getCTRow().getOutlineLevel();
        if (!isRowGroupHiddenByParent(sheet, rowNumber)) {

            /** change start */
            // start and end are off by one because POI did edge detection. Move
            // start back to correct pos:
            startIdx++;
            // end is already correct because of another bug (using '<' instead
            // of '<=' below)
            /** change end */

            for (int i = startIdx; i < endIdx; i++) {
                XSSFRow r = sheet.getRow(i);
                if (level == r.getCTRow().getOutlineLevel()) {
                    r.getCTRow().unsetHidden();
                } else if (!isRowGroupOrParentCollapsed(sheet, i, level)) {
                    r.getCTRow().unsetHidden();
                }
            }

        }

        // Write collapse field
        /** start */
        if (isRowsInverted(sheet)) {
            XSSFRow r = sheet.getRow(startIdx - 1);
            if (r != null && r.getCTRow().getCollapsed()) {
                r.getCTRow().unsetCollapsed();
            }
        } else {
            CTRow ctRow = sheet.getRow(endIdx).getCTRow();
            // This avoids an IndexOutOfBounds if multiple nested groups are
            // collapsed/expanded
            if (ctRow.getCollapsed()) {
                ctRow.unsetCollapsed();
            }
        }
        /** end */
    }

    private static int findEndOfRowOutlineGroup(XSSFSheet sheet, int row) {
        short level = sheet.getRow(row).getCTRow().getOutlineLevel();
        int currentRow;
        /** start */
        int lastRowNum = sheet.getLastRowNum() + 1;
        /** end */
        for (currentRow = row; currentRow < lastRowNum; currentRow++) {
            XSSFRow row2 = sheet.getRow(currentRow);
            if (row2 == null || row2.getCTRow().getOutlineLevel() < level) {
                break;
            }
        }
        return currentRow;
    }

    /**
     * Replaces {@link XSSFSheet#isRowGroupCollapsed(XSSFSheet, int)}, which
     * doesn't account for intermediary levels being collapsed or not.
     */
    private static boolean isRowGroupOrParentCollapsed(XSSFSheet sheet, int row,
            int originalLevel) {

        int level = sheet.getRow(row).getCTRow().getOutlineLevel();

        // start from row level and work upwards to original level
        while (level > originalLevel) {

            int collapseRow;
            if (isRowsInverted(sheet)) {
                collapseRow = findStartOfRowOutlineGroup(sheet, row);
                row--;
            } else {
                collapseRow = findEndOfRowOutlineGroup(sheet, row);
                row++;
            }

            if (sheet.getRow(collapseRow) != null) {

                CTRow ctRow = sheet.getRow(collapseRow).getCTRow();

                level = ctRow.getOutlineLevel();

                boolean collapsed = ctRow.getCollapsed();
                if (collapsed && ctRow.getOutlineLevel() >= originalLevel) {
                    // this parent is collapsed
                    return true;
                }
            }

        }
        return false;

    }

    private static boolean isRowGroupHiddenByParent(XSSFSheet sheet, int row) {
        // Look out outline details of end
        int endLevel;
        boolean endHidden;
        int endOfOutlineGroupIdx = findEndOfRowOutlineGroup(sheet, row);
        if (sheet.getRow(endOfOutlineGroupIdx) == null) {
            endLevel = 0;
            endHidden = false;
        } else {
            endLevel = sheet.getRow(endOfOutlineGroupIdx).getCTRow()
                    .getOutlineLevel();
            endHidden = sheet.getRow(endOfOutlineGroupIdx).getCTRow()
                    .getHidden();
        }

        // Look out outline details of start
        int startLevel;
        boolean startHidden;
        /** start */
        int startOfOutlineGroupIdx = findStartOfRowOutlineGroup(sheet, row);
        /** end */
        if (startOfOutlineGroupIdx < 0
                || sheet.getRow(startOfOutlineGroupIdx) == null) {
            startLevel = 0;
            startHidden = false;
        } else {
            startLevel = sheet.getRow(startOfOutlineGroupIdx).getCTRow()
                    .getOutlineLevel();
            startHidden = sheet.getRow(startOfOutlineGroupIdx).getCTRow()
                    .getHidden();
        }
        if (endLevel > startLevel) {
            return endHidden;
        }
        return startHidden;
    }

    @SuppressWarnings("deprecation")
    private static boolean isColumnGroupHiddenByParent(XSSFSheet sheet,
            int idx) {
        CTCols cols = sheet.getCTWorksheet().getColsArray(0);
        // Look out outline details of end
        int endLevel = 0;
        boolean endHidden = false;
        // int endOfOutlineGroupIdx = findEndOfColumnOutlineGroup(sheet,idx);
        int endOfOutlineGroupIdx = (Integer) callSheetMethod(
                "findEndOfColumnOutlineGroup", sheet, idx);
        CTCol[] colArray = cols.getColArray();
        /** start */
        if (endOfOutlineGroupIdx + 1 < colArray.length) {
            /** end */
            CTCol nextInfo = colArray[endOfOutlineGroupIdx + 1];
            if ((Boolean) callSheetMethod("isAdjacentBefore", sheet,
                    colArray[endOfOutlineGroupIdx], nextInfo)) {
                endLevel = nextInfo.getOutlineLevel();
                endHidden = nextInfo.getHidden();
            }
        }
        // Look out outline details of start
        int startLevel = 0;
        boolean startHidden = false;
        // int startOfOutlineGroupIdx = findStartOfColumnOutlineGroup(idx);
        int startOfOutlineGroupIdx = (Integer) callSheetMethod(
                "findStartOfColumnOutlineGroup", sheet, idx);
        if (startOfOutlineGroupIdx > 0) {
            CTCol prevInfo = colArray[startOfOutlineGroupIdx - 1];

            if ((Boolean) callSheetMethod("isAdjacentBefore", sheet, prevInfo,
                    colArray[startOfOutlineGroupIdx])) {
                startLevel = prevInfo.getOutlineLevel();
                startHidden = prevInfo.getHidden();
            }

        }
        if (endLevel > startLevel) {
            return endHidden;
        }
        return startHidden;
    }

    private static int findStartOfRowOutlineGroup(XSSFSheet sheet,
            int rowIndex) {
        // Find the start of the group.
        short level = sheet.getRow(rowIndex).getCTRow().getOutlineLevel();
        int currentRow = rowIndex;
        while (sheet.getRow(currentRow) != null) {
            if (sheet.getRow(currentRow).getCTRow().getOutlineLevel() < level) {
                /** start */
                return currentRow;
                /** end */
            }
            currentRow--;
        }
        return currentRow;
    }

    public static void collapseRow(XSSFSheet sheet, int rowIndex) {
        XSSFRow row = sheet.getRow(rowIndex);
        if (row != null) {
            int startRow = findStartOfRowOutlineGroup(sheet, rowIndex);

            // Hide all the columns until the end of the group
            int lastRow = writeHidden(sheet, row, startRow, true);

            /** start */

            if (isRowsInverted(sheet)) {
                if (sheet.getRow(startRow) != null) {
                    sheet.getRow(startRow).getCTRow().setCollapsed(true);
                } else if (startRow < 0) {
                    // happens when inverted group starts at 0; Excel does not
                    // write a collapsed prop for this case.
                } else {
                    XSSFRow newRow = sheet.createRow(startRow);
                    newRow.getCTRow().setCollapsed(true);
                }

            } else {
                if (sheet.getRow(lastRow) != null) {
                    sheet.getRow(lastRow).getCTRow().setCollapsed(true);
                } else {
                    XSSFRow newRow = sheet.createRow(lastRow);
                    newRow.getCTRow().setCollapsed(true);
                }
            }
            /** end */
        }
    }

    private static boolean isRowsInverted(XSSFSheet sheet) {
        boolean inverted = false;
        try {
            inverted = sheet.getCTWorksheet().getSheetPr().getOutlinePr()
                    .isSetSummaryBelow();
        } catch (NullPointerException IGNORE) {
            // fine
        }

        return inverted;
    }

    private static int writeHidden(XSSFSheet sheet, XSSFRow xRow, int rowIndex,
            boolean hidden) {
        short level = xRow.getCTRow().getOutlineLevel();

        /** completely rewritten after this line */
        // row index is the first row BEFORE group, not what we want
        rowIndex++;

        // row will be null at some point, this is safe
        while (true) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }

            short outlineLevel = row.getCTRow().getOutlineLevel();
            if (outlineLevel < level) {
                break;
            }
            row.getCTRow().setHidden(hidden);
            rowIndex++;
        }

        /** old code, for reference */
        // for (Iterator<Row> it = sheet.rowIterator(); it.hasNext();) {
        // xRow = (XSSFRow) it.next();
        //
        // // skip rows before the start of this group
        // if (xRow.getRowNum() < rowIndex) {
        // continue;
        // }
        //
        // if (xRow.getCTRow().getOutlineLevel() >= level) {
        // xRow.getCTRow().setHidden(hidden);
        // rowIndex++;
        // }
        //
        // }
        return rowIndex;
    }

    public static void collapseColumn(XSSFSheet sheet, int columnNumber) {
        CTCols cols = sheet.getCTWorksheet().getColsArray(0);
        CTCol col = sheet.getColumnHelper().getColumn(columnNumber, false);
        int colInfoIx = sheet.getColumnHelper().getIndexOfColumn(cols, col);
        if (colInfoIx == -1) {
            return;
        }
        // Find the start of the group.
        int groupStartColInfoIx = (Integer) callSheetMethod(
                "findStartOfColumnOutlineGroup", sheet, colInfoIx);

        /** START */
        // Hide all the columns until the end of the group
        int lastColMax = (Integer) callSheetMethod("setGroupHidden", sheet,
                new Object[] { groupStartColInfoIx, col.getOutlineLevel(),
                        true });
        /** END */

        // write collapse field
        callSheetMethod("setColumn", sheet,
                new Object[] { lastColMax + 1, 0, null, null, Boolean.TRUE });
    }

    public static short expandColumn(XSSFSheet sheet, int columnIndex) {
        CTCols cols = sheet.getCTWorksheet().getColsArray(0);
        CTCol col = sheet.getColumnHelper().getColumn(columnIndex, false);
        int colInfoIx = sheet.getColumnHelper().getIndexOfColumn(cols, col);

        int idx = (Integer) callSheetMethod("findColInfoIdx", sheet,
                new Object[] { (int) col.getMax(), colInfoIx });
        if (idx == -1) {
            return -1;
        }

        // If it is already expanded do nothing.
        if (!isColumnGroupCollapsed(sheet, idx)) {
            return -1;
        }

        // Find the start/end of the group.
        int startIdx = (Integer) callSheetMethod(
                "findStartOfColumnOutlineGroup", sheet, idx);
        int endIdx = (Integer) callSheetMethod("findEndOfColumnOutlineGroup",
                sheet, idx);

        // expand:
        // colapsed bit must be unset
        // hidden bit gets unset _if_ surrounding groups are expanded you can
        // determine
        // this by looking at the hidden bit of the enclosing group. You will
        // have
        // to look at the start and the end of the current group to determine
        // which
        // is the enclosing group
        // hidden bit only is altered for this outline level. ie. don't
        // uncollapse contained groups
        CTCol[] colArray = cols.getColArray();
        @SuppressWarnings("unused")
        CTCol columnInfo = colArray[endIdx];
        short expandedLevel = -1;
        if (!isColumnGroupHiddenByParent(sheet, idx)) {
            /** Start */
            short outlineLevel = col.getOutlineLevel();
            /** end */
            boolean nestedGroup = false;
            for (int i = startIdx; i <= endIdx; i++) {
                CTCol ci = colArray[i];
                if (outlineLevel == ci.getOutlineLevel()) {
                    ci.unsetHidden();
                    if (nestedGroup) {
                        nestedGroup = false;
                        ci.setCollapsed(true);
                    }
                    expandedLevel = outlineLevel;
                } else {
                    nestedGroup = true;
                }
            }
        }

        /** start */
        // // Write collapse flag (stored in a single col info record after this
        // // outline group)
        // callSheetMethod("setColumn", sheet,
        // new Object[] { (int) columnInfo.getMax() + 1, null, null,
        // Boolean.FALSE, Boolean.FALSE });
        /** end */
        return expandedLevel;
    }

    private static boolean isColumnGroupCollapsed(XSSFSheet sheet, int idx) {

        /**
         * The APIDoc for this method says that cols work as rows, with the
         * 'collapsed' attribute being after the col group. It isn't, the
         * 'hidden' attribute is used instead. Hence, rewrite:
         */

        CTCols cols = sheet.getCTWorksheet().getColsArray(0);

        CTCol col = cols.getColArray(idx);
        return col.isSetHidden();

        /**
         * original code for reference
         */

        // CTCols cols = sheet.getCTWorksheet().getColsArray(0);
        // CTCol[] colArray = cols.getColArray();
        // int endOfOutlineGroupIdx = findEndOfColumnOutlineGroup(sheet, idx);
        // int nextColInfoIx = endOfOutlineGroupIdx + 1;
        // if (nextColInfoIx >= colArray.length) {
        // return false;
        // }
        // CTCol nextColInfo = colArray[nextColInfoIx];
        //
        // CTCol col = colArray[endOfOutlineGroupIdx];
        // if (!isAdjacentBefore(col, nextColInfo)) {
        // return false;
        // }
        //
        // return nextColInfo.getCollapsed();
    }

    /**
     * Util method so that we don't need to copy all private methods from
     * XSSFSheet.
     */
    private static Object callSheetMethod(String methodname, XSSFSheet sheet,
            Object... params) {

        Class<?>[] paramtypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramtypes[i] = params[i] == null ? Object.class
                    : params[i].getClass();
        }

        Method method = null;
        try {

            for (Method m : XSSFSheet.class.getDeclaredMethods()) {
                if (m.getName().equals(methodname)) {
                    method = m;
                }
            }

            // method = XSSFSheet.class.getDeclaredMethod(methodname,
            // paramtypes);
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(sheet, params);
            }

        } catch (Exception e) {
            LOGGER.info("Error accessing method: " + method, e);
        } finally {
            if (method != null) {
                method.setAccessible(false);
            }
        }

        return null;
    }

    /**
     * @return A column index, which can uniquely identify the group that exists
     *         at the given col, and has the given level. (<code>col</code>
     *         might have a level that is higher than we want). 1-based.
     */
    public static long findUniqueColIndex(CTCols colsArray, CTCol col,
            short lastlevel) {
        int index = colsArray.getColList().indexOf(col);
        for (; index < colsArray.sizeOfColArray(); index++) {

            CTCol current = colsArray.getColArray(index);
            if (current.getOutlineLevel() == lastlevel) {
                return current.getMin();
            }
        }
        return -1;
    }

    /**
     * @return A row index, which can uniquely identify the group that exists
     *         between the given indexes, and has the given level. (the row at
     *         <code>start</code> might have a level that is higher than we
     *         want). 0-based.
     */
    public static long findUniqueRowIndex(Spreadsheet sheet, int start, int end,
            int lastlevel) {
        for (int i = start; i <= end; i++) {

            XSSFRow current = (XSSFRow) sheet.getActiveSheet().getRow(i);
            if (current.getCTRow().getOutlineLevel() == lastlevel) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return If the group that spans the given col and has the given level is
     *         hidden or not. (col might have a higher level than the one we
     *         want to check).
     */
    public static boolean checkHidden(CTCols colsArray, CTCol col,
            short lastlevel) {
        int index = colsArray.getColList().indexOf(col);
        for (; index < colsArray.sizeOfColArray(); index++) {
            CTCol current = colsArray.getColArray(index);
            if (current.getOutlineLevel() == lastlevel) {
                return current.isSetHidden();
            }
        }
        return false;
    }

    /**
     * @return The end index of the row group that spans the given row, with the
     *         given level. 0-based.
     */
    public static long findEndOfRowGroup(Spreadsheet sheet, int rowindex,
            XSSFRow row, short level) {

        while (rowindex < sheet.getRows()) {
            XSSFRow r = (XSSFRow) sheet.getActiveSheet().getRow(rowindex);
            if (r == null || r.getCTRow().getOutlineLevel() < level) {
                // end
                return rowindex - 1l;
            }

            rowindex++;
        }
        return -1l;
    }

    /**
     * @return The end index of the col group that spans the given col, with the
     *         given level. 1-based.
     */
    public static long findEndOfColGroup(CTCols colsArray, CTCol col,
            short level) {

        CTCol previous = null;

        int index = colsArray.getColList().indexOf(col);
        for (; index < colsArray.sizeOfColArray(); index++) {

            CTCol c = colsArray.getColArray(index);

            // break in cols or smaller outline
            boolean hasBreak = previous != null
                    && c.getMin() - previous.getMax() > 1;
            if (hasBreak || c.getOutlineLevel() < level) {
                break;
            }
            previous = c;
        }

        // group ends on last
        return previous == null ? 0 : previous.getMax();
    }
}
