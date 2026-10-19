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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOutlinePr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import com.vaadin.flow.component.spreadsheet.shared.GroupingData;

/**
 * The outline of the sheet shown: its groups of rows and columns, read from POI
 * for the client, and collapsed and expanded as the user asks, a group at a
 * time or a level at a time. POI reads one only from an XLSX sheet; an XLS
 * sheet is shown without, the sheet's scope having set the outline back.
 */
final class Outline implements Serializable {

    private final Spreadsheet spreadsheet;

    Outline(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Loads all data relating to grouping if the current sheet is a
     * {@link XSSFSheet}.
     *
     * <p>
     * Top-level orchestrator: bail out for HSSF, capture outline-pr settings,
     * then process the column and row outlines separately. The two
     * outline-processing branches are independent (they touch different POI
     * types and different Spreadsheet setters), so they live in dedicated
     * helpers below.
     */
    void load() {

        if (!(spreadsheet.getActiveSheet() instanceof XSSFSheet xssfSheet)) {
            // API not available on HSSF
            return;
        }

        CTWorksheet ctWorksheet = xssfSheet.getCTWorksheet();
        spreadsheet.sheetModel().colGroupingMax.set(0);
        spreadsheet.sheetModel().rowGroupingMax.set(0);

        if (ctWorksheet.getSheetPr() != null
                && ctWorksheet.getSheetPr().getOutlinePr() != null) {
            CTOutlinePr outlinePr = ctWorksheet.getSheetPr().getOutlinePr();
            spreadsheet.sheetModel().colGroupingInversed
                    .set(!outlinePr.getSummaryRight());
            spreadsheet.sheetModel().rowGroupingInversed
                    .set(!outlinePr.getSummaryBelow());
        } else {
            spreadsheet.sheetModel().colGroupingInversed.set(false);
            spreadsheet.sheetModel().rowGroupingInversed.set(false);
        }

        loadColumns(ctWorksheet);
        loadRows();
    }

    /**
     * Column-outline branch of {@link #load}.
     *
     * <p>
     * Columns are grouped so that columns that are beside each other and share
     * properties have a single {@code CTCol} with a min and max index. A column
     * that is part of a group has an outline level. Each col also has a
     * property called 'collapsed', which doesn't appear to be used for
     * anything. If a group is collapsed, each col in the group has its
     * 'visibility' property set to false.
     */
    private void loadColumns(CTWorksheet ctWorksheet) {
        CTCols colsArray = ctWorksheet.getColsArray(0);

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
                    if (spreadsheet.sheetModel().colGroupingMax
                            .get() < lastlevel) {
                        spreadsheet.sheetModel().colGroupingMax
                                .set((int) lastlevel);
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

                boolean together = spreadsheet.sheetModel().colGroupingInversed
                        .get() ? d1.startIndex == d2.startIndex
                                : d1.endIndex == d2.endIndex;
                if (together) {
                    // The outer one is drawn, whichever was read first.
                    toRemove.add(d1.level > d2.level ? d1 : d2);
                }
            }
        }

        data.removeAll(toRemove);

        spreadsheet.sheetModel().colGroupingData.set(data);
    }

    /**
     * Row-outline branch of {@link #load}.
     *
     * <p>
     * Each row that has data (or grouping props) exists separately, they are
     * not grouped like columns. Each row that is part of a group has a set
     * outline level. Unlike cols, the 'collapse' property is actually used for
     * rows, in conjuction with the 'hidden' prop. If a group is collapsed, each
     * row in the group has its 'hidden' prop set to true. Also, the column
     * after the group (or before, if inverted) has its 'collapsed' property set
     * to true.
     */
    private void loadRows() {
        List<GroupingData> data = new ArrayList<GroupingData>();
        Set<GroupingData> toRemove;

        Stack<GroupingData> rows = new Stack<GroupingData>();
        short lastlevel = 0;
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
                    if (spreadsheet.sheetModel().rowGroupingInversed.get()) {
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
                            i, lastlevel);
                    long uniqueIndex = GroupingUtil
                            .findUniqueRowIndex(spreadsheet, i, end, lastlevel);

                    GroupingData d = new GroupingData(i, end, lastlevel,
                            uniqueIndex, false);

                    rows.push(d);

                    if (spreadsheet.sheetModel().rowGroupingMax
                            .get() < d.level) {
                        spreadsheet.sheetModel().rowGroupingMax.set(d.level);
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

                boolean together = spreadsheet.sheetModel().rowGroupingInversed
                        .get() ? d1.startIndex == d2.startIndex
                                : d1.endIndex == d2.endIndex;
                if (together) {
                    // The outer one is drawn, whichever was read first.
                    toRemove.add(d1.level > d2.level ? d1 : d2);
                }
            }
        }

        data.removeAll(toRemove);

        spreadsheet.sheetModel().rowGroupingData.set(data);
    }

    /**
     * Collapses or expands the group the row or column is in, and reads the
     * outline and the sizes again.
     *
     * @see Spreadsheet#setGroupingCollapsed(boolean, int, boolean)
     */
    void setCollapsed(boolean isCols, int index, boolean collapsed) {

        XSSFSheet activeSheet = (XSSFSheet) spreadsheet.getActiveSheet();
        if (isCols) {
            if (collapsed) {
                GroupingUtil.collapseColumn(activeSheet, index);
            } else {
                // The cells of the columns shown are sent below, for the
                // rows on the screen.
                GroupingUtil.expandColumn(activeSheet, index);
            }
        } else {
            if (collapsed) {
                GroupingUtil.collapseRow(activeSheet, index);
            } else {
                GroupingUtil.expandRow(activeSheet, index);
            }
        }
        load();
        // The rows and columns the group hid or showed are read again.
        spreadsheet.sizes().groupToggled();
    }

    /**
     * Expands every group above the level clicked and collapses those of the
     * level, which hides the levels below it; then reads the outline and the
     * sizes again.
     *
     * @see Spreadsheet#levelHeaderClicked(boolean, int)
     */
    void showLevel(boolean isCols, int level) {

        /*
         * A click on a header should change groupings so that all levels above
         * the selected are expanded, and the selected level is all collapsed
         * (which hides any levels underneath this).
         */

        if (!(spreadsheet.getActiveSheet() instanceof XSSFSheet xsheet)) {
            return;
        }
        CTWorksheet ctWorksheet = xsheet.getCTWorksheet();

        if (isCols) {

            CTCols ctCols = ctWorksheet.getColsList().get(0);
            List<CTCol> colList = ctCols.getColList();
            for (CTCol col : colList) {
                short l = col.getOutlineLevel();

                // It's a lot easier to not call expand/collapse

                if (l >= 0 && l < level) {
                    // expand
                    if (col.isSetHidden()) {
                        col.unsetHidden();
                    }
                } else {
                    // collapse
                    col.setHidden(true);
                }
            }

        } else {

            /*
             * Groups are more complicated than cols, use existing
             * collapse/expand functionality.
             */

            int lastlevel = 0;
            for (int i = 0; i < spreadsheet.getRows(); i++) {

                XSSFRow row = xsheet.getRow(i);
                if (row == null) {
                    lastlevel = 0;
                    continue;
                }

                short l = row.getCTRow().getOutlineLevel();
                if (l != lastlevel) {

                    // group starts here

                    int end = (int) GroupingUtil.findEndOfRowGroup(spreadsheet,
                            i, l);
                    long uniqueIndex = GroupingUtil
                            .findUniqueRowIndex(spreadsheet, i, end, l);

                    if (l > 0 && l < level) {
                        // expand
                        GroupingUtil.expandRow(xsheet, (int) uniqueIndex);

                    } else if (l >= level) {
                        // collapse
                        GroupingUtil.collapseRow(xsheet, (int) uniqueIndex);
                    }

                    lastlevel = l;
                }

            }

        }

        // As for one group: the grouping and the sizes are read again. Loading
        // the whole workbook again, as this once did, emptied the undo history
        // and dropped what the application had set on the sheet.
        load();
        spreadsheet.sizes().groupToggled();
    }
}
