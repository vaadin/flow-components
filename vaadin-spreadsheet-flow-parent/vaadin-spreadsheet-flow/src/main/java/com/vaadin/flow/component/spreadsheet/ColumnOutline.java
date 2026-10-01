/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;

/**
 * Column outline model on top of the POI {@code <cols>} element, using only
 * public POI API.
 * <p>
 * Follows the Excel encoding: every column of a group has an outline level, the
 * columns of a collapsed group are hidden, and the column right after a
 * collapsed group carries the {@code collapsed} flag. A group that ends at the
 * last column of the sheet has no column after it, so for such a group the
 * collapsed state is derived from all of its columns being hidden.
 */
class ColumnOutline implements Serializable {

    static final int MAX_LEVEL = 7;

    private static final int LAST_COLUMN = SpreadsheetVersion.EXCEL2007
            .getLastColumnIndex();

    private ColumnOutline() {
    }

    /**
     * Reads all column groups, including nested groups and groups hidden by a
     * collapsed parent. Sorted by first column, then by level.
     */
    static List<ColumnGroup> getGroups(XSSFSheet sheet) {
        List<ColumnGroup> groups = new ArrayList<>();
        int lastColumn = getLastOutlinedColumn(sheet);
        if (lastColumn < 0) {
            return groups;
        }
        int[] levels = new int[lastColumn + 1];
        int maxLevel = 0;
        for (int c = 0; c <= lastColumn; c++) {
            levels[c] = sheet.getColumnOutlineLevel(c);
            maxLevel = Math.max(maxLevel, levels[c]);
        }
        for (int level = 1; level <= maxLevel; level++) {
            int start = -1;
            for (int c = 0; c <= lastColumn + 1; c++) {
                boolean inGroup = c <= lastColumn && levels[c] >= level;
                if (inGroup && start < 0) {
                    start = c;
                } else if (!inGroup && start >= 0) {
                    groups.add(new ColumnGroup(start, c - 1, level,
                            isCollapsed(sheet, start, c - 1)));
                    start = -1;
                }
            }
        }
        groups.sort(Comparator.comparingInt(ColumnGroup::firstColumn)
                .thenComparingInt(ColumnGroup::level));
        return groups;
    }

    static ColumnGroup findGroup(XSSFSheet sheet, int column, int level) {
        return getGroups(sheet).stream()
                .filter(g -> g.level() == level && g.contains(column))
                .findFirst().orElse(null);
    }

    static void group(XSSFSheet sheet, int firstColumn, int lastColumn) {
        validateRange(firstColumn, lastColumn);
        for (int c = firstColumn; c <= lastColumn; c++) {
            if (sheet.getColumnOutlineLevel(c) >= MAX_LEVEL) {
                throw new IllegalArgumentException(
                        "Column groups can be nested at most " + MAX_LEVEL
                                + " levels deep");
            }
        }
        // XSSFSheet.groupColumn writes overlapping <col> ranges when the
        // columns have already been split into single-column elements
        for (int c = firstColumn; c <= lastColumn; c++) {
            int level = sheet.getColumnOutlineLevel(c);
            splitColumn(sheet, c).setOutlineLevel((short) (level + 1));
        }
        updateHiddenColumns(sheet, firstColumn, lastColumn, null);
        sortColumns(sheet);
    }

    static void ungroup(XSSFSheet sheet, int firstColumn, int lastColumn) {
        validateRange(firstColumn, lastColumn);
        // Drop the markers of groups that are removed or shrink, then let
        // visibility follow the remaining groups
        for (ColumnGroup group : getGroups(sheet)) {
            if (group.firstColumn() <= lastColumn
                    && group.lastColumn() >= firstColumn) {
                setMarker(sheet, group, false);
            }
        }
        List<ColumnGroup> before = getGroups(sheet);
        // XSSFSheet.ungroupColumn skips columns once the range has been split
        for (int c = firstColumn; c <= lastColumn; c++) {
            int level = sheet.getColumnOutlineLevel(c);
            if (level > 0) {
                CTCol col = splitColumn(sheet, c);
                if (level == 1) {
                    col.unsetOutlineLevel();
                } else {
                    col.setOutlineLevel((short) (level - 1));
                }
            }
        }
        int from = before.stream().mapToInt(ColumnGroup::firstColumn).min()
                .orElse(firstColumn);
        int to = before.stream().mapToInt(ColumnGroup::lastColumn).max()
                .orElse(lastColumn);
        updateHiddenColumns(sheet, Math.min(from, firstColumn),
                Math.max(to, lastColumn), null);
        sortColumns(sheet);
    }

    static void clear(XSSFSheet sheet) {
        List<ColumnGroup> groups = getGroups(sheet);
        for (ColumnGroup group : groups) {
            setMarker(sheet, group, false);
        }
        for (ColumnGroup group : groups) {
            if (group.level() == 1) {
                for (int c = group.firstColumn(); c <= group
                        .lastColumn(); c++) {
                    CTCol col = splitColumn(sheet, c);
                    col.unsetOutlineLevel();
                    setHidden(sheet, c, false);
                }
            }
        }
        sortColumns(sheet);
    }

    /**
     * Collapses or expands the given group. Nested groups keep their own
     * collapsed state, so restoring any combination of nested states works in
     * any order.
     */
    static void setCollapsed(XSSFSheet sheet, ColumnGroup group,
            boolean collapsed) {
        setMarker(sheet, group, collapsed);
        ColumnGroup outermost = getGroups(sheet).stream()
                .filter(g -> g.level() == 1 && g.contains(group.firstColumn()))
                .findFirst().orElse(group);
        updateHiddenColumns(sheet, outermost.firstColumn(),
                outermost.lastColumn(), new ColumnGroup(group.firstColumn(),
                        group.lastColumn(), group.level(), collapsed));
        sortColumns(sheet);
    }

    /**
     * Expands all groups above the given level and collapses the groups on and
     * below it, like the level buttons in Excel.
     */
    static void collapseToLevel(XSSFSheet sheet, int level) {
        List<ColumnGroup> groups = getGroups(sheet);
        for (ColumnGroup group : groups) {
            setMarker(sheet, group, group.level() >= level);
        }
        for (ColumnGroup group : groups) {
            if (group.level() == 1) {
                updateHiddenColumns(sheet, group.firstColumn(),
                        group.lastColumn(), new ColumnGroup(group.firstColumn(),
                                group.lastColumn(), 1, level <= 1));
            }
        }
        sortColumns(sheet);
    }

    /**
     * A column is hidden when any group containing it is collapsed. The
     * override is needed for groups that end at the last column, as they have
     * no marker column.
     */
    private static void updateHiddenColumns(XSSFSheet sheet, int from, int to,
            ColumnGroup override) {
        List<ColumnGroup> groups = getGroups(sheet);
        for (int c = from; c <= to; c++) {
            final int column = c;
            boolean hidden = groups.stream().filter(g -> g.contains(column))
                    .anyMatch(g -> override != null && g.sameRange(override)
                            ? override.collapsed()
                            : g.collapsed());
            setHidden(sheet, c, hidden);
        }
    }

    /**
     * The column holding the collapsed flag: after the group, or before it when
     * the summary column is on the left. -1 if there is no such column.
     */
    private static int getMarkerColumn(XSSFSheet sheet, int firstColumn,
            int lastColumn) {
        int marker = isInversed(sheet) ? firstColumn - 1 : lastColumn + 1;
        return marker < 0 || marker > LAST_COLUMN ? -1 : marker;
    }

    private static boolean isInversed(XSSFSheet sheet) {
        var sheetPr = sheet.getCTWorksheet().getSheetPr();
        return sheetPr != null && sheetPr.getOutlinePr() != null
                && !sheetPr.getOutlinePr().getSummaryRight();
    }

    private static void setMarker(XSSFSheet sheet, ColumnGroup group,
            boolean collapsed) {
        setMarker(sheet,
                getMarkerColumn(sheet, group.firstColumn(), group.lastColumn()),
                collapsed);
    }

    private static boolean isCollapsed(XSSFSheet sheet, int firstColumn,
            int lastColumn) {
        int markerColumn = getMarkerColumn(sheet, firstColumn, lastColumn);
        if (markerColumn >= 0) {
            CTCol marker = sheet.getColumnHelper().getColumn(markerColumn,
                    false);
            return marker != null && marker.getCollapsed();
        }
        for (int c = firstColumn; c <= lastColumn; c++) {
            if (!sheet.isColumnHidden(c)) {
                return false;
            }
        }
        return true;
    }

    private static void setMarker(XSSFSheet sheet, int column,
            boolean collapsed) {
        if (column < 0) {
            // Excel doesn't write a marker past the edge of the sheet either
            return;
        }
        if (!collapsed) {
            CTCol existing = sheet.getColumnHelper().getColumn(column, false);
            if (existing == null || !existing.isSetCollapsed()) {
                return;
            }
        }
        CTCol col = splitColumn(sheet, column);
        if (collapsed) {
            col.setCollapsed(true);
        } else {
            col.unsetCollapsed();
        }
    }

    private static void setHidden(XSSFSheet sheet, int column, boolean hidden) {
        if (sheet.isColumnHidden(column) == hidden) {
            return;
        }
        CTCol col = splitColumn(sheet, column);
        if (hidden) {
            col.setHidden(true);
        } else {
            // Unset instead of hidden="0", so that the column isn't mistaken
            // for a collapsed one by code checking isSetHidden()
            col.unsetHidden();
        }
    }

    /**
     * Returns the {@code CTCol} of exactly the given column, splitting a shared
     * range if needed.
     */
    private static CTCol splitColumn(XSSFSheet sheet, int column) {
        ColumnHelper helper = sheet.getColumnHelper();
        boolean hidden = sheet.isColumnHidden(column);
        // setColHidden is the public entry point that splits ranges
        helper.setColHidden(column, hidden);
        CTCol col = helper.getColumn(column, false);
        if (!hidden) {
            col.unsetHidden();
        }
        return col;
    }

    /**
     * Splitting a column range appends the new {@code <col>} elements at the
     * end, while the rest of the grouping code expects them in order.
     */
    private static void sortColumns(XSSFSheet sheet) {
        for (CTCols cols : sheet.getCTWorksheet().getColsArray()) {
            ColumnHelper.sortColumns(cols);
        }
    }

    private static int getLastOutlinedColumn(XSSFSheet sheet) {
        if (sheet.getCTWorksheet().sizeOfColsArray() == 0) {
            return -1;
        }
        int last = -1;
        for (CTCols cols : sheet.getCTWorksheet().getColsArray()) {
            for (CTCol col : cols.getColArray()) {
                if (col.getOutlineLevel() > 0) {
                    last = Math.max(last, (int) col.getMax() - 1);
                }
            }
        }
        return last;
    }

    private static void validateRange(int firstColumn, int lastColumn) {
        if (firstColumn < 0 || lastColumn > LAST_COLUMN
                || firstColumn > lastColumn) {
            throw new IllegalArgumentException(
                    "Invalid column range " + firstColumn + ".." + lastColumn);
        }
    }
}
