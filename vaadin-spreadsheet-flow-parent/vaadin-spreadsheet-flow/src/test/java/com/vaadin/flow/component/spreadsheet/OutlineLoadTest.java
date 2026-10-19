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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.shared.GroupingData;
import com.vaadin.tests.MockUIExtension;

/**
 * The outline the client is given, read from the groups of an XLSX sheet: each
 * group by its first and last row or column, 0-based, its level, and whether it
 * is collapsed. The groups inside a collapsed one are left out, as Excel draws
 * none of them; so is a group that ends where another of its rows or columns
 * does, as Excel keeps one collapsed flag for both.
 */
class OutlineLoadTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    /**
     * Twelve rows of twelve values, made before grouping: a row made again
     * loses its group.
     */
    private final XSSFSheet sheet = filled(workbook.createSheet());

    private static XSSFSheet filled(XSSFSheet sheet) {
        for (int r = 0; r < 12; r++) {
            var row = sheet.createRow(r);
            for (int c = 0; c < 12; c++) {
                row.createCell(c).setCellValue(r * 12 + c);
            }
        }
        return sheet;
    }

    private Spreadsheet show() {
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        return spreadsheet;
    }

    /** The groups as "first-last level", with " collapsed" when they are. */
    private static List<String> groups(List<GroupingData> data) {
        return data.stream()
                .map(group -> group.startIndex + "-" + group.endIndex + " "
                        + group.level + (group.collapsed ? " collapsed" : ""))
                .sorted().toList();
    }

    private static List<String> columnGroups(Spreadsheet spreadsheet) {
        return groups(spreadsheet.sheetModel().colGroupingData.get());
    }

    private static List<String> rowGroups(Spreadsheet spreadsheet) {
        return groups(spreadsheet.sheetModel().rowGroupingData.get());
    }

    @Test
    void columnGroupsWithAGapBetween_areTwoGroupsOfTheFirstLevel() {
        sheet.groupColumn(1, 2);
        sheet.groupColumn(5, 6);

        assertEquals(List.of("1-2 1", "5-6 1"), columnGroups(show()));
    }

    @Test
    void aColumnGroupInsideAnother_isOfTheSecondLevel() {
        sheet.groupColumn(1, 6);
        sheet.groupColumn(2, 4);

        assertEquals(List.of("1-6 1", "2-4 2"), columnGroups(show()));
    }

    @Test
    void aCollapsedColumnGroup_leavesOutTheGroupsInsideIt() {
        sheet.groupColumn(1, 6);
        sheet.groupColumn(2, 4);
        sheet.setColumnGroupCollapsed(1, true);

        assertEquals(List.of("1-6 1 collapsed"), columnGroups(show()));
    }

    @Test
    void rowGroupsWithAGapBetween_areTwoGroupsOfTheFirstLevel() {
        sheet.groupRow(1, 2);
        sheet.groupRow(5, 6);

        assertEquals(List.of("1-2 1", "5-6 1"), rowGroups(show()));
    }

    @Test
    void aCollapsedRowGroup_isReadCollapsed() {
        sheet.groupRow(2, 4);
        sheet.setRowGroupCollapsed(2, true);

        assertEquals(List.of("2-4 1 collapsed"), rowGroups(show()));
    }

    @Test
    void aCollapsedRowGroup_withItsSummaryAbove_isReadCollapsed() {
        // As Excel saves it: the rows hidden, and the collapsed mark on the
        // summary row above them. POI's own collapse puts the mark below.
        sheet.setRowSumsBelow(false);
        sheet.groupRow(2, 4);
        for (int r = 2; r <= 4; r++) {
            sheet.getRow(r).getCTRow().setHidden(true);
        }
        sheet.getRow(1).getCTRow().setCollapsed(true);

        assertEquals(List.of("2-4 1 collapsed"), rowGroups(show()));
    }

    @Test
    void aCollapsedRowGroup_leavesOutTheGroupsInsideIt() {
        sheet.groupRow(1, 6);
        sheet.groupRow(2, 4);
        sheet.setRowGroupCollapsed(1, true);

        assertEquals(List.of("1-6 1 collapsed"), rowGroups(show()));
    }

    @Test
    void columnGroupsEndingOnOneColumn_areGivenAsTheOuterOne() {
        sheet.groupColumn(1, 6);
        sheet.groupColumn(3, 6);

        assertEquals(List.of("1-6 1"), columnGroups(show()));
    }

    @Test
    void rowGroupsEndingOnOneRow_areGivenAsTheOuterOne() {
        sheet.groupRow(1, 6);
        sheet.groupRow(3, 6);

        assertEquals(List.of("1-6 1"), rowGroups(show()));
    }

    @Test
    void rowGroupsStartingOnOneRow_withTheirSummaryAbove_areGivenAsTheOuterOne() {
        sheet.setRowSumsBelow(false);
        sheet.groupRow(1, 6);
        sheet.groupRow(1, 3);

        assertEquals(List.of("1-6 1"), rowGroups(show()));
    }

    @Test
    void columnGroupsStartingOnOneColumn_withTheirSummaryLeft_areGivenAsTheOuterOne() {
        sheet.setRowSumsRight(false);
        sheet.groupColumn(1, 6);
        sheet.groupColumn(1, 3);

        assertEquals(List.of("1-6 1"), columnGroups(show()));
    }

    @Test
    void summariesBelowAndRight_leaveTheOutlineAsItIs() {
        sheet.groupRow(1, 2);

        Spreadsheet spreadsheet = show();

        assertFalse(spreadsheet.sheetModel().rowGroupingInversed.get());
        assertFalse(spreadsheet.sheetModel().colGroupingInversed.get());
    }

    @Test
    void summariesAboveAndLeft_inverseTheOutline() {
        sheet.setRowSumsBelow(false);
        sheet.setRowSumsRight(false);
        sheet.groupRow(1, 2);

        Spreadsheet spreadsheet = show();

        assertTrue(spreadsheet.sheetModel().rowGroupingInversed.get());
        assertTrue(spreadsheet.sheetModel().colGroupingInversed.get());
    }
}
