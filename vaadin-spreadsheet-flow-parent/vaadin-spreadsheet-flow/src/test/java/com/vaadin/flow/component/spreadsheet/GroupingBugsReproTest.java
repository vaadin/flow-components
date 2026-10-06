/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

/**
 * Reproductions for the column grouping bugs listed in
 * {@code vaadin-spreadsheet-flow-parent/COLUMN-GROUPING-RESEARCH.md}, section
 * 9. Bug numbers match that list.
 * <p>
 * Each test asserts the correct behaviour. All bugs marked "reproduced" there
 * fail on {@code main}. On this prototype branch, the tests that still fail are
 * {@code @Disabled} with the bug number; remove the annotation when fixing the
 * bug. The enabled tests pass and guard against regressions, including the bug
 * 7 and bug 18 checks, which didn't reproduce.
 */
class GroupingBugsReproTest {

    // ---------------------------------------------------------------- helpers

    private static XSSFWorkbook dataWorkbook(int rows, int cols) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("S1");
        for (int r = 0; r < rows; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < cols; c++) {
                row.createCell(c).setCellValue("r" + r + "c" + c);
            }
        }
        return wb;
    }

    /** Simulates a workbook loaded from a file (write + read). */
    private static Spreadsheet loadFromFile(XSSFWorkbook wb) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            Spreadsheet s = new Spreadsheet(
                    new ByteArrayInputStream(out.toByteArray()));
            scroll(s);
            return s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void scroll(Spreadsheet s) {
        TestHelper.fireClientEvent(s, "onSheetScroll", "[1, 1, 1, 1]");
    }

    private static void colClick(Spreadsheet s, int uniqueIndex,
            boolean collapse) {
        TestHelper.fireClientEvent(s, "groupingCollapsed",
                "[true, " + uniqueIndex + ", " + collapse + "]");
    }

    private static void rowClick(Spreadsheet s, int uniqueIndex,
            boolean collapse) {
        TestHelper.fireClientEvent(s, "groupingCollapsed",
                "[false, " + uniqueIndex + ", " + collapse + "]");
    }

    private static void colLevel(Spreadsheet s, int level) {
        TestHelper.fireClientEvent(s, "levelHeaderClicked",
                "[true, " + level + "]");
    }

    private static void rowLevel(Spreadsheet s, int level) {
        TestHelper.fireClientEvent(s, "levelHeaderClicked",
                "[false, " + level + "]");
    }

    private static JsonNode colGroups(Spreadsheet s) {
        return JacksonUtils.getMapper()
                .readTree(s.getElement().getProperty("colGroupingData"));
    }

    private static JsonNode group(Spreadsheet s, int startIndex, int level) {
        for (JsonNode g : colGroups(s)) {
            if (g.get("startIndex").asInt() == startIndex
                    && g.get("level").asInt() == level) {
                return g;
            }
        }
        return null;
    }

    private static String dumpCols(Spreadsheet s) {
        XSSFSheet sheet = (XSSFSheet) s.getActiveSheet();
        return dumpCols(sheet);
    }

    private static String dumpCols(XSSFSheet sheet) {
        if (sheet.getCTWorksheet().sizeOfColsArray() == 0) {
            return "<no cols element>";
        }
        StringBuilder sb = new StringBuilder();
        for (CTCol c : sheet.getCTWorksheet().getColsArray(0).getColList()) {
            sb.append("[").append(c.getMin()).append("-").append(c.getMax())
                    .append(" L").append(c.getOutlineLevel());
            if (c.isSetHidden()) {
                sb.append(" hidden=").append(c.getHidden() ? "1" : "0");
            }
            if (c.isSetCollapsed()) {
                sb.append(" collapsed=").append(c.getCollapsed() ? "1" : "0");
            }
            sb.append("] ");
        }
        return sb.toString();
    }

    private static void log(String id, String msg) {
        System.out.println("### BUG" + id + " " + msg);
    }

    private static void collapseAllGroupCols(XSSFSheet sheet, int firstCol,
            int lastCol) {
        CTCols cols = sheet.getCTWorksheet().getColsArray(0);
        for (CTCol c : cols.getColList()) {
            if (c.getMin() - 1 >= firstCol && c.getMax() - 1 <= lastCol) {
                c.setHidden(true);
            }
        }
    }

    // ------------------------------------------------------------- bug 3 is in
    // tests.GroupingTest (the two nested-column tests that were @Disabled)

    // ------------------------------------------------------------------ bug 4

    private Spreadsheet bug4Sheet() {
        XSSFWorkbook wb = dataWorkbook(10, 8);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 2);
        sheet.groupRow(2, 3);
        Spreadsheet s = loadFromFile(wb);
        log("4", "initial cols=" + s.getColumns() + " rows=" + s.getRows());
        s.setMaxColumns(6);
        s.setMaxRows(15);
        log("4", "after setMax cols=" + s.getColumns() + " rows="
                + s.getRows());
        return s;
    }

    @Disabled("Bug 4: a group click resets setMaxColumns/setMaxRows")
    @Test
    void bug4_collapseClick_keepsMaxColumnsAndRows() {
        Spreadsheet s = bug4Sheet();
        colClick(s, 1, true);
        log("4", "after collapse click cols=" + s.getColumns() + " rows="
                + s.getRows() + " prop cols="
                + s.getElement().getProperty("cols") + " prop rows="
                + s.getElement().getProperty("rows"));
        Assertions.assertEquals(6, s.getColumns());
        Assertions.assertEquals(15, s.getRows());
    }

    @Disabled("Bug 4: a group click resets setMaxColumns/setMaxRows")
    @Test
    void bug4_rowCollapseClick_keepsMaxColumnsAndRows() {
        Spreadsheet s = bug4Sheet();
        rowClick(s, 2, true);
        log("4", "after row collapse click cols=" + s.getColumns() + " rows="
                + s.getRows());
        Assertions.assertEquals(6, s.getColumns());
        Assertions.assertEquals(15, s.getRows());
    }

    @Disabled("Bug 4: a group click resets setMaxColumns/setMaxRows")
    @Test
    void bug4_levelClick_keepsMaxColumnsAndRows() {
        Spreadsheet s = bug4Sheet();
        colLevel(s, 1);
        log("4", "after level click cols=" + s.getColumns() + " rows="
                + s.getRows());
        Assertions.assertEquals(6, s.getColumns());
        Assertions.assertEquals(15, s.getRows());
    }

    // ------------------------------------------------------------------ bug 5

    private Spreadsheet bug5Sheet() {
        XSSFWorkbook wb = dataWorkbook(6, 8);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 2); // B:C
        sheet.groupColumn(4, 5); // E:F
        Spreadsheet s = loadFromFile(wb);
        log("5", "initial " + dumpCols(s) + " groups=" + colGroups(s));
        return s;
    }

    @Disabled("Bug 5: a column shown again makes its group read as collapsed")
    @Test
    void bug5_hideThenShowColumn_groupStillExpandedAfterRefresh() {
        Spreadsheet s = bug5Sheet();
        s.setColumnHidden(1, true);
        s.setColumnHidden(1, false);
        log("5", "after hide+show " + dumpCols(s));
        // grouping refresh caused by a click on the other group
        colClick(s, 4, true);
        log("5", "after collapse E:F " + dumpCols(s) + " groups="
                + colGroups(s));
        Assertions.assertFalse(s.isColumnHidden(1));
        Assertions.assertFalse(
                group(s, 1, 1).path("collapsed").asBoolean(false),
                "B:C group must not read as collapsed");
    }

    @Disabled("Bug 5: a column shown again makes its group read as collapsed")
    @Test
    void bug5_showOnlyFirstColumn_groupStillExpandedAfterRefresh() {
        Spreadsheet s = bug5Sheet();
        s.setColumnHidden(1, false);
        log("5", "after show-only B " + dumpCols(s));
        colClick(s, 4, true);
        log("5", "after collapse E:F " + dumpCols(s) + " groups="
                + colGroups(s));
        Assertions.assertFalse(
                group(s, 1, 1).path("collapsed").asBoolean(false),
                "B:C group must not read as collapsed");
    }

    @Test
    void bug5_showOnlyColumn_groupStillExpandedAfterRefresh() {
        Spreadsheet s = bug5Sheet();
        s.setColumnHidden(2, false);
        log("5", "after show-only " + dumpCols(s));
        colClick(s, 4, true);
        log("5", "after collapse E:F " + dumpCols(s) + " groups="
                + colGroups(s));
        Assertions.assertFalse(
                group(s, 1, 1).path("collapsed").asBoolean(false),
                "B:C group must not read as collapsed");
    }

    @Test
    void bug5_userClickOnWronglyCollapsedGroup() {
        Spreadsheet s = bug5Sheet();
        s.setColumnHidden(1, true);
        s.setColumnHidden(1, false);
        colClick(s, 4, true);
        // client now shows "+" for B:C, the user clicks it -> expand request
        colClick(s, 1, false);
        log("5", "after user click on B:C '+' " + dumpCols(s) + " groups="
                + colGroups(s) + " B hidden=" + s.isColumnHidden(1)
                + " C hidden=" + s.isColumnHidden(2));
        // second click on B:C (now shown as '-') -> collapse request
        colClick(s, 1, true);
        log("5", "after 2nd click on B:C " + dumpCols(s) + " B hidden="
                + s.isColumnHidden(1) + " C hidden=" + s.isColumnHidden(2));
        Assertions.assertTrue(s.isColumnHidden(1));
    }

    // ------------------------------------------------------------------ bug 6

    @Disabled("Bug 6: a range grouped twice gets uniqueIndex -2")
    @Test
    void bug6_sameRangeTwoLevels_userClicksDoNotThrow() {
        XSSFWorkbook wb = dataWorkbook(6, 8);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 3);
        sheet.groupColumn(1, 3); // B:D grouped twice -> level 2
        Spreadsheet s = loadFromFile(wb);
        JsonNode groups = colGroups(s);
        log("6", "cols " + dumpCols(s) + " groups=" + groups);
        int unique = groups.get(0).get("uniqueIndex").asInt();
        // first click: client shows '-', sends collapse
        Assertions.assertDoesNotThrow(() -> colClick(s, unique, true));
        log("6", "after collapse click " + dumpCols(s) + " B hidden="
                + s.isColumnHidden(1));
        // second click: client now shows '+', sends expand
        Throwable t = null;
        try {
            colClick(s, unique, false);
        } catch (Throwable e) {
            t = e;
            log("6", "expand click threw " + e);
        }
        Assertions.assertNull(t, "expand click must not throw");
        Assertions.assertTrue(s.isColumnHidden(1),
                "collapse click should have hidden B:D");
    }

    @Test
    void bug6_crafted_expandColumnWithoutCol() {
        XSSFWorkbook wb = dataWorkbook(6, 8);
        wb.getSheetAt(0).groupColumn(1, 2);
        Spreadsheet s = loadFromFile(wb);
        Throwable t = null;
        try {
            colClick(s, 6, false);
        } catch (Throwable e) {
            t = e;
            log("6", "crafted expand(6) threw " + e);
        }
        Assertions.assertNull(t);
    }

    // ------------------------------------------------------------------ bug 7

    private static XSSFWorkbook rowGroupOnlyWorkbook() {
        XSSFWorkbook wb = dataWorkbook(10, 3);
        wb.getSheetAt(0).groupRow(2, 4);
        XSSFSheet s2 = wb.createSheet("S2");
        s2.createRow(0).createCell(0).setCellValue("x");
        return wb;
    }

    @Test
    void bug7_writeToStream_thenRowGroupClick() throws IOException {
        Spreadsheet s = loadFromFile(rowGroupOnlyWorkbook());
        XSSFSheet sheet = (XSSFSheet) s.getActiveSheet();
        log("7", "cols elements before write="
                + sheet.getCTWorksheet().sizeOfColsArray());
        s.write(new ByteArrayOutputStream());
        log("7", "cols elements after write="
                + sheet.getCTWorksheet().sizeOfColsArray());
        Throwable t = null;
        try {
            rowClick(s, 2, true);
        } catch (Throwable e) {
            t = e;
            log("7", "row collapse click threw " + e);
        }
        Assertions.assertNull(t);
    }

    @Test
    void bug7_writeToStream_thenRowLevelClick() throws IOException {
        Spreadsheet s = loadFromFile(rowGroupOnlyWorkbook());
        s.write(new ByteArrayOutputStream());
        Throwable t = null;
        try {
            rowLevel(s, 1);
        } catch (Throwable e) {
            t = e;
            log("7", "row level click threw " + e);
        }
        Assertions.assertNull(t);
    }

    @Test
    void bug7_writeToStream_thenSheetSwitch() throws IOException {
        Spreadsheet s = loadFromFile(rowGroupOnlyWorkbook());
        s.write(new ByteArrayOutputStream());
        Throwable t = null;
        try {
            s.setActiveSheetIndex(1);
            s.setActiveSheetIndex(0);
        } catch (Throwable e) {
            t = e;
            log("7", "sheet switch threw " + e);
        }
        Assertions.assertNull(t);
    }

    @Test
    void bug7_fileWithoutColsElement_rowGroupClick() throws IOException {
        // the written file has no <cols> (removed by XSSFSheet.write)
        XSSFWorkbook wb = rowGroupOnlyWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        log("7", "source wb cols elements after write="
                + wb.getSheetAt(0).getCTWorksheet().sizeOfColsArray());
        Spreadsheet s = new Spreadsheet(
                new ByteArrayInputStream(out.toByteArray()));
        scroll(s);
        log("7", "loaded-from-file cols elements="
                + ((XSSFSheet) s.getActiveSheet()).getCTWorksheet()
                        .sizeOfColsArray());
        Assertions.assertDoesNotThrow(() -> rowClick(s, 2, true));
    }

    @Test
    void bug7_inMemoryWorkbookWrittenBeforeSetWorkbook() throws IOException {
        // app writes the workbook (e.g. to persist it) and then shows it
        XSSFWorkbook wb = rowGroupOnlyWorkbook();
        wb.write(new ByteArrayOutputStream());
        Throwable t = null;
        try {
            Spreadsheet s = new Spreadsheet(wb);
            scroll(s);
        } catch (Throwable e) {
            t = e;
            log("7", "new Spreadsheet(writtenWb) threw " + e);
        }
        Assertions.assertNull(t);
    }

    // ------------------------------------------------------------------ bug 8

    @Test
    void bug8_collapseGroupEndingAtLastColumn() throws IOException {
        XSSFWorkbook wb = dataWorkbook(4, 3);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(16380, 16383);
        Spreadsheet s = loadFromFile(wb);
        log("8", "initial " + dumpCols(s) + " groups=" + colGroups(s));
        int unique = colGroups(s).get(0).get("uniqueIndex").asInt();
        colClick(s, unique, true);
        String cols = dumpCols(s);
        log("8", "after collapse " + cols);
        long maxMax = 0;
        for (CTCol c : ((XSSFSheet) s.getActiveSheet()).getCTWorksheet()
                .getColsArray(0).getColList()) {
            maxMax = Math.max(maxMax, c.getMax());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        s.write(out);
        try (XSSFWorkbook back = new XSSFWorkbook(
                new ByteArrayInputStream(out.toByteArray()))) {
            log("8", "read back by POI OK: " + dumpCols(back.getSheetAt(0)));
            Spreadsheet s2 = new Spreadsheet(back);
            scroll(s2);
            log("8", "reloaded in Spreadsheet groups=" + colGroups(s2));
        } catch (Throwable e) {
            log("8", "read back threw " + e);
        }
        Assertions.assertTrue(maxMax <= 16384,
                "col max must be <= 16384 (XFD), was " + maxMax);
    }

    // ------------------------------------------------------------------ bug 9

    @Test
    void bug9_levelClick_keepsComponentState() {
        XSSFWorkbook wb = dataWorkbook(10, 8);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 2);
        sheet.groupColumn(4, 5);
        Spreadsheet s = loadFromFile(wb);
        s.setMaxColumns(7);
        s.setSelection("H5");
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(s,
                new CellRangeAddress(0, 5, 0, 2));
        s.registerTable(table);
        // a user edit to have something to undo
        TestHelper.fireClientEvent(s, "cellValueEdited", "[1, 1, \"edited\"]");
        String before = "cols=" + s.getColumns() + " sel="
                + s.getSelectedCellReference() + " tables="
                + s.getTables().size() + " canUndo="
                + s.getSpreadsheetHistoryManager().canUndo();
        colLevel(s, 1);
        String after = "cols=" + s.getColumns() + " sel="
                + s.getSelectedCellReference() + " tables="
                + s.getTables().size() + " canUndo="
                + s.getSpreadsheetHistoryManager().canUndo();
        log("9", "before level click: " + before);
        log("9", "after  level click: " + after);
        Assertions.assertEquals(before, after);
    }

    @Test
    void bug9_levelClick_writesCollapsedFlags() throws IOException {
        XSSFWorkbook wb = dataWorkbook(10, 8);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 2);
        sheet.groupColumn(4, 5);
        Spreadsheet s = loadFromFile(wb);

        // reference: per-group click
        Spreadsheet ref = loadFromFile(wb);
        colClick(ref, 1, true);
        colClick(ref, 4, true);
        log("9", "after per-group collapse clicks " + dumpCols(ref));

        colLevel(s, 1);
        String cols = dumpCols(s);
        log("9", "after level-1 click " + cols);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        s.write(out);
        try (XSSFWorkbook back = new XSSFWorkbook(
                new ByteArrayInputStream(out.toByteArray()))) {
            Spreadsheet s2 = new Spreadsheet(back);
            scroll(s2);
            log("9", "save/reload groups=" + colGroups(s2));
        }
        Assertions.assertTrue(cols.contains("collapsed=1"),
                "level click should write a collapsed flag like Excel");
    }

    // ----------------------------------------------------------------- bug 14

    private Spreadsheet bug14Sheet(boolean inverted, int[][] groups,
            int outerFirst, int outerLast) {
        XSSFWorkbook wb = dataWorkbook(6, 12);
        XSSFSheet sheet = wb.getSheetAt(0);
        for (int[] g : groups) {
            sheet.groupColumn(g[0], g[1]);
        }
        if (inverted) {
            sheet.setRowSumsRight(false);
        }
        collapseAllGroupCols(sheet, outerFirst, outerLast);
        int marker = inverted ? outerFirst - 1 : outerLast + 1;
        sheet.setColumnWidth(marker, sheet.getColumnWidth(marker));
        sheet.getColumnHelper().getColumn(marker, false).setCollapsed(true);
        Spreadsheet s = loadFromFile(wb);
        log("14", (inverted ? "inverted " : "") + dumpCols(s) + " groups="
                + colGroups(s));
        return s;
    }

    @Test
    void bug14_outerCollapsed_twoChildren() {
        Spreadsheet s = bug14Sheet(false,
                new int[][] { { 1, 7 }, { 2, 3 }, { 5, 6 } }, 1, 7);
        Assertions.assertEquals(1, colGroups(s).size());
    }

    @Test
    void bug14_outerCollapsed_childAndGrandchild() {
        Spreadsheet s = bug14Sheet(false,
                new int[][] { { 1, 7 }, { 2, 5 }, { 3, 4 } }, 1, 7);
        Assertions.assertEquals(1, colGroups(s).size());
    }

    @Test
    void bug14_inverted_outerCollapsed_twoChildren() {
        Spreadsheet s = bug14Sheet(true,
                new int[][] { { 1, 7 }, { 2, 3 }, { 5, 6 } }, 1, 7);
        Assertions.assertEquals(1, colGroups(s).size());
    }

    @Test
    void bug14_inverted_outerCollapsed_childAndGrandchild() {
        Spreadsheet s = bug14Sheet(true,
                new int[][] { { 1, 7 }, { 2, 5 }, { 3, 4 } }, 1, 7);
        Assertions.assertEquals(1, colGroups(s).size());
    }

    @Disabled("Bug 14: a child of a collapsed parent gets a control (inverted outline)")
    @Test
    void bug14_inverted_outerCollapsed_childOnLastColumn() {
        // child starts on the last column of the collapsed parent
        Spreadsheet s = bug14Sheet(true, new int[][] { { 1, 4 }, { 4, 4 } }, 1,
                4);
        Assertions.assertEquals(1, colGroups(s).size());
    }

    @Disabled("Bug 14: a child of a collapsed parent gets a control (inverted outline)")
    @Test
    void bug14_inverted_outerCollapsed_childEndingOnLastColumn() {
        Spreadsheet s = bug14Sheet(true,
                new int[][] { { 1, 6 }, { 3, 6 }, { 6, 6 } }, 1, 6);
        Assertions.assertEquals(1, colGroups(s).size());
    }

    // ----------------------------------------------------------------- bug 16

    @Disabled("Bug 16: autofit width for filter buttons lost after a group click")
    @Test
    void bug16_autofitFilterColumn_survivesGroupingRefresh() {
        XSSFWorkbook wb = dataWorkbook(6, 8);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.getRow(0).getCell(1).setCellValue("A long header text");
        sheet.groupColumn(1, 2); // B:C inside filter table
        sheet.groupColumn(5, 6); // F:G
        Spreadsheet s = loadFromFile(wb);
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(s,
                new CellRangeAddress(0, 5, 0, 3));
        s.registerTable(table);
        s.autofitColumn(1);
        int afterAutofit = s.getColW()[1];
        int poiPx = (int) s.getActiveSheet().getColumnWidthInPixels(1);
        colClick(s, 5, true);
        int afterCollapse = s.getColW()[1];
        colClick(s, 5, false);
        int afterExpand = s.getColW()[1];
        log("16",
                "colW[1] autofit=" + afterAutofit + " poiPx=" + poiPx
                        + " afterCollapseOther=" + afterCollapse
                        + " afterExpandOther=" + afterExpand);
        Assertions.assertEquals(afterAutofit, afterCollapse);
        Assertions.assertEquals(afterAutofit, afterExpand);
    }

    // ----------------------------------------------------------------- bug 17

    @Disabled("Bug 17: groups past the column count are sent to the client")
    @Test
    void bug17_groupBeyondDataColumns() {
        XSSFWorkbook wb = dataWorkbook(4, 3);
        wb.getSheetAt(0).groupColumn(60, 62);
        Spreadsheet s = loadFromFile(wb);
        JsonNode g = colGroups(s).get(0);
        log("17", "cols=" + s.getColumns() + " colW.length="
                + s.getColW().length + " group=" + g);
        colClick(s, g.get("uniqueIndex").asInt(), true);
        log("17", "after collapse hiddenColumnIndexes="
                + s.getElement().getProperty("hiddenColumnIndexes")
                + " colW.length=" + s.getColW().length + " poi hidden(61)="
                + s.getActiveSheet().isColumnHidden(61));
        Assertions.assertTrue(
                colGroups(s).get(0).get("endIndex").asInt() < s.getColumns(),
                "group sent to client must be inside cols");
    }

    @Disabled("Bug 17: groups past the column count are sent to the client")
    @Test
    void bug17_groupBeyondMaxColumns() {
        XSSFWorkbook wb = dataWorkbook(4, 10);
        wb.getSheetAt(0).groupColumn(3, 8);
        Spreadsheet s = loadFromFile(wb);
        s.setMaxColumns(5);
        JsonNode g = colGroups(s).get(0);
        log("17", "maxCols=5 cols=" + s.getColumns() + " colW.length="
                + s.getColW().length + " group=" + g);
        Assertions.assertTrue(g.get("endIndex").asInt() < s.getColumns(),
                "group sent to client must be inside cols");
    }

    // ----------------------------------------------------------------- bug 18

    private static List<Integer> spyMarkedColumns(Spreadsheet s,
            Runnable action) throws Exception {
        Field f = Spreadsheet.class.getDeclaredField("valueManager");
        f.setAccessible(true);
        CellValueManager orig = (CellValueManager) f.get(s);
        CellValueManager spy = Mockito.spy(orig);
        f.set(s, spy);
        TreeSet<Integer> cols = new TreeSet<>();
        Mockito.doAnswer(inv -> {
            cols.add(((Cell) inv.getArgument(0)).getColumnIndex());
            return inv.callRealMethod();
        }).when(spy).markCellForUpdate(Mockito.any(Cell.class));
        action.run();
        f.set(s, orig);
        return new ArrayList<>(cols);
    }

    @Test
    void bug18_expandParent_marksAllNewlyVisibleColumns() throws Exception {
        // outer B:G (1..6), child C:D (2..3), not collapsed in file
        XSSFWorkbook wb = dataWorkbook(6, 10);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 6);
        sheet.groupColumn(2, 3);
        Spreadsheet s = loadFromFile(wb);
        log("18", "groups=" + colGroups(s));
        int outerUnique = group(s, 1, 1).get("uniqueIndex").asInt();
        colClick(s, outerUnique, true);
        boolean[] hiddenBefore = new boolean[8];
        for (int c = 0; c < 8; c++) {
            hiddenBefore[c] = s.isColumnHidden(c);
        }
        List<Integer> marked = spyMarkedColumns(s,
                () -> colClick(s, outerUnique, false));
        List<Integer> becameVisible = new ArrayList<>();
        for (int c = 0; c < 8; c++) {
            if (hiddenBefore[c] && !s.isColumnHidden(c)) {
                becameVisible.add(c);
            }
        }
        log("18",
                "hiddenBefore=" + Arrays.toString(hiddenBefore)
                        + " becameVisible=" + becameVisible + " marked="
                        + marked + " " + dumpCols(s));
        Assertions.assertTrue(marked.containsAll(becameVisible));
    }

    @Test
    void bug18_expandParentWithChildAtStart_marksAllNewlyVisibleColumns()
            throws Exception {
        // outer B:G (1..6), child B:C (1..2) starts with the parent, so the
        // parent's uniqueIndex is D (3), not its startIndex
        XSSFWorkbook wb = dataWorkbook(6, 10);
        XSSFSheet sheet = wb.getSheetAt(0);
        sheet.groupColumn(1, 6);
        sheet.groupColumn(1, 2);
        Spreadsheet s = loadFromFile(wb);
        log("18", "groups=" + colGroups(s));
        int outerUnique = group(s, 1, 1).get("uniqueIndex").asInt();
        colClick(s, outerUnique, true);
        boolean[] hiddenBefore = new boolean[8];
        for (int c = 0; c < 8; c++) {
            hiddenBefore[c] = s.isColumnHidden(c);
        }
        List<Integer> marked = spyMarkedColumns(s,
                () -> colClick(s, outerUnique, false));
        List<Integer> becameVisible = new ArrayList<>();
        for (int c = 0; c < 8; c++) {
            if (hiddenBefore[c] && !s.isColumnHidden(c)) {
                becameVisible.add(c);
            }
        }
        log("18",
                "childAtStart hiddenBefore=" + Arrays.toString(hiddenBefore)
                        + " becameVisible=" + becameVisible + " marked="
                        + marked + " " + dumpCols(s));
        Assertions.assertTrue(marked.containsAll(becameVisible));
    }
}
