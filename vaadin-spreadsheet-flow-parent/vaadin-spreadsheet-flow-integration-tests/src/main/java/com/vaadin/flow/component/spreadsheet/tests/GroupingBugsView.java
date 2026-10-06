/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * Repro scenarios for column grouping bugs. Not for merging. Pick a scenario
 * with the {@code s} query parameter.
 */
@Route("spreadsheet-grouping-bugs")
public class GroupingBugsView extends VerticalLayout
        implements BeforeEnterObserver {

    /** Exposes the protected method customers use as a refresh workaround. */
    public static class RefreshableSpreadsheet extends Spreadsheet {
        public RefreshableSpreadsheet(XSSFWorkbook workbook) {
            super(workbook);
        }

        public void refreshViaProtected(int columnInGroup) {
            setGroupingCollapsed(true, columnInGroup, false);
        }
    }

    private final Div info = new Div();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String s = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("s", java.util.List.of("2")).get(0);
        removeAll();
        setSizeFull();
        info.setId("info");
        info.setText("scenario " + s);
        switch (s) {
        case "1" -> scenario1(true);
        case "1b" -> scenario1(false);
        case "2" -> scenario2();
        case "12" -> scenario12();
        case "13" -> scenario13(true);
        case "13b" -> scenario13(false);
        case "14" -> scenario14(false);
        case "14b" -> scenario14(true);
        case "18" -> scenario18();
        case "6" -> scenario6();
        case "14i" -> scenario14Inverted();
        case "17" -> scenario17();
        default -> info.setText("unknown scenario " + s);
        }
    }

    /** Bug 1: runtime POI grouping, refreshed via the protected method. */
    private void scenario1(boolean limits) {
        XSSFWorkbook wb = workbook(12);
        RefreshableSpreadsheet ss = new RefreshableSpreadsheet(wb);
        if (!limits) {
            ss.setSizeFull();
            add(info, ss);
            expand(ss);
        }
        NativeButton group = new NativeButton("Group B:E via POI + refresh",
                e -> {
                    ((XSSFSheet) ss.getActiveSheet()).groupColumn(1, 4);
                    ss.refreshViaProtected(1);
                });
        group.setId("runtime-group");
        if (limits) {
            add(spreadsheet(ss, 12));
        }
        add(group);
    }

    /** Bug 2: workbook group ending at the last column (setMaxColumns). */
    private void scenario2() {
        XSSFWorkbook wb = workbook(12);
        wb.getSheetAt(0).groupColumn(9, 11);
        add(spreadsheet(new Spreadsheet(wb), 12));
    }

    /** Bug 12: groups while scrolling horizontally. */
    private void scenario12() {
        XSSFWorkbook wb = workbook(40);
        wb.getSheetAt(0).groupColumn(1, 3);
        wb.getSheetAt(0).groupColumn(6, 9);
        add(spreadsheet(new Spreadsheet(wb), 40));
    }

    /** Bug 13: frozen columns A:B, group C:E right after the split. */
    private void scenario13(boolean inverted) {
        XSSFWorkbook wb = workbook(12);
        XSSFSheet sheet = wb.getSheetAt(0);
        if (inverted) {
            sheet.getCTWorksheet().addNewSheetPr().addNewOutlinePr()
                    .setSummaryRight(false);
        }
        sheet.groupColumn(2, 4);
        sheet.groupColumn(1, 7);
        Spreadsheet ss = new Spreadsheet(wb);
        ss.createFreezePane(1, 2);
        add(spreadsheet(ss, 12));
    }

    /**
     * Bug 14: collapsed parent B:G with two children (C:D, E:F), or with a
     * child C:F and grandchild D:E.
     */
    private void scenario14(boolean grandchild) {
        XSSFWorkbook wb = workbook(12);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 6);
        if (grandchild) {
            sheet.groupColumn(2, 5);
            sheet.groupColumn(3, 4);
        } else {
            sheet.groupColumn(2, 3);
            sheet.groupColumn(4, 5);
        }
        collapse(sheet, 1, 6);
        add(spreadsheet(new Spreadsheet(wb), 12));
    }

    /** Bug 18: parent B:G and child C:D, both collapsed in the file. */
    private void scenario18() {
        XSSFWorkbook wb = workbook(12);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 6);
        sheet.groupColumn(2, 3);
        collapse(sheet, 2, 3);
        collapse(sheet, 1, 6);
        add(spreadsheet(new Spreadsheet(wb), 12));
    }

    /** Bugs 6 and 11: B:D grouped twice (same range, two levels). */
    private void scenario6() {
        XSSFWorkbook wb = workbook(12);
        wb.getSheetAt(0).groupColumn(1, 3);
        wb.getSheetAt(0).groupColumn(1, 3);
        add(spreadsheet(new Spreadsheet(wb), 12));
    }

    /** Bugs 14 and 11: inverted, collapsed B:E with child E:E. */
    private void scenario14Inverted() {
        XSSFWorkbook wb = workbook(12);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.getCTWorksheet().addNewSheetPr().addNewOutlinePr()
                .setSummaryRight(false);
        sheet.groupColumn(1, 4);
        sheet.groupColumn(4, 4);
        for (int c = 1; c <= 4; c++) {
            sheet.setColumnHidden(c, true);
        }
        // inverted: the flag is on the column before the group
        sheet.setColumnHidden(0, false);
        CTCol marker = sheet.getColumnHelper().getColumn(0, false);
        marker.unsetHidden();
        marker.setCollapsed(true);
        ColumnHelper.sortColumns(sheet.getCTWorksheet().getColsArray(0));
        add(spreadsheet(new Spreadsheet(wb), 12));
    }

    /** Bug 17: group G:I past setMaxColumns(5). */
    private void scenario17() {
        XSSFWorkbook wb = workbook(12);
        wb.getSheetAt(0).groupColumn(1, 2);
        wb.getSheetAt(0).groupColumn(6, 8);
        add(spreadsheet(new Spreadsheet(wb), 5));
    }

    /** Excel encoding: hide the columns, flag the column after the group. */
    private static void collapse(XSSFSheet sheet, int first, int last) {
        for (int c = first; c <= last; c++) {
            sheet.setColumnHidden(c, true);
        }
        sheet.setColumnHidden(last + 1, false);
        CTCol marker = sheet.getColumnHelper().getColumn(last + 1, false);
        marker.unsetHidden();
        marker.setCollapsed(true);
        // POI appends split <col> elements; Excel files are always sorted
        ColumnHelper.sortColumns(sheet.getCTWorksheet().getColsArray(0));
    }

    private static XSSFWorkbook workbook(int columns) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Data");
        for (int r = 0; r < 30; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < columns; c++) {
                row.createCell(c).setCellValue(
                        r == 0 ? "Col " + CellReference.convertNumToColString(c)
                                : String.valueOf(r * 100 + c));
            }
        }
        return wb;
    }

    private Spreadsheet spreadsheet(Spreadsheet ss, int columns) {
        ss.setSizeFull();
        ss.setMaxColumns(columns);
        ss.setMaxRows(30);
        add(info);
        expand(ss);
        return ss;
    }
}
