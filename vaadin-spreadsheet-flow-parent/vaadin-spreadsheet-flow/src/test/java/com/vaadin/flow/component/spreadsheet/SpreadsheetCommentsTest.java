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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The comments of the cells shown, with their authors and whether they are
 * shown always, and the invalid formulas marked: in the frozen corner, the
 * frozen rows and columns and the rest of the sheet, but not in hidden rows or
 * columns, nor under a merged cell.
 */
class SpreadsheetCommentsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final Sheet sheet = workbook.createSheet();
    private final Drawing<?> drawing = sheet.createDrawingPatriarch();
    private Spreadsheet spreadsheet;

    private Cell cell(String name) {
        CellReference reference = new CellReference(name);
        Row row = sheet.getRow(reference.getRow());
        if (row == null) {
            row = sheet.createRow(reference.getRow());
        }
        return row.createCell(reference.getCol());
    }

    /** A comment on the cell, by its name in lower case, shown on hover. */
    private void comment(String name) {
        comment(name, false);
    }

    private void comment(String name, boolean shownAlways) {
        CreationHelper helper = workbook.getCreationHelper();
        CellReference reference = new CellReference(name);
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setRow1(reference.getRow());
        anchor.setCol1(reference.getCol());
        anchor.setRow2(reference.getRow() + 2);
        anchor.setCol2(reference.getCol() + 2);
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(helper.createRichTextString("on " + name));
        comment.setAuthor(name.toLowerCase());
        comment.setVisible(shownAlways);
        cell(name).setCellComment(comment);
    }

    /**
     * Shows the sheet frozen at two rows and two columns, as the client shows
     * rows 10-20 of columns E-H.
     */
    private void show() {
        sheet.createFreezePane(2, 2);
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        showRows(10, 20);
    }

    private void showRows(int first, int last) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + first
                        + ", \"firstColumn\": 5, \"lastRow\": " + last
                        + ", \"lastColumn\": 8}");
    }

    private static String name(String key) {
        return new CellReference(SpreadsheetUtil.getRowFromKey(key) - 1,
                SpreadsheetUtil.getColumnIndexFromKey(key) - 1)
                .formatAsString();
    }

    private static Map<String, String> byName(Map<String, String> byKey) {
        Map<String, String> byName = new TreeMap<>();
        byKey.forEach((key, value) -> byName.put(name(key), value));
        return byName;
    }

    private static List<String> names(Collection<String> keys) {
        TreeSet<String> names = new TreeSet<>();
        keys.forEach(key -> names.add(name(key)));
        return List.copyOf(names);
    }

    @Test
    void theCommentsShown_comeWithTheirAuthors_inEveryPane() {
        comment("A1");
        comment("F1");
        comment("A12");
        comment("F12");
        comment("C5");

        show();

        assertEquals(Map.of("A1", "on A1", "A12", "on A12", "F1", "on F1",
                "F12", "on F12"), byName(spreadsheet.getCellComments()));
        assertEquals(Map.of("A1", "a1", "A12", "a12", "F1", "f1", "F12", "f12"),
                byName(spreadsheet.getCellCommentAuthors()));
    }

    @Test
    void theCommentsShownAlways_areListed() {
        comment("F12", true);
        comment("F13", false);
        comment("A1", true);

        show();

        assertEquals(List.of("A1", "F12"),
                names(spreadsheet.getVisibleCellComments()));
    }

    @Test
    void theCommentsOfHiddenRowsAndColumns_areLeftOut() {
        comment("F12");
        comment("F14");
        comment("G12");
        sheet.getRow(13).setZeroHeight(true);
        sheet.setColumnHidden(6, true);

        show();

        assertEquals(List.of("F12"),
                names(spreadsheet.getCellComments().keySet()));
    }

    @Test
    void theCommentsUnderAMergedCell_areLeftOut_butTheMergedCellsIsNot() {
        comment("E15");
        comment("F15");
        comment("H15");
        sheet.addMergedRegion(CellRangeAddress.valueOf("E15:G15"));

        show();

        assertEquals(List.of("E15", "H15"),
                names(spreadsheet.getCellComments().keySet()));
    }

    @Test
    void theInvalidFormulasShown_areMarked() {
        show();
        spreadsheet.markInvalidFormula(6, 12);
        spreadsheet.markInvalidFormula(1, 1);
        spreadsheet.markInvalidFormula(3, 5);

        showRows(11, 21);

        assertEquals(List.of("A1", "F12"),
                names(spreadsheet.getInvalidFormulaCells()));
    }

    @Test
    void theCommentsScrolledAway_go() {
        comment("F12");
        comment("F32");
        show();

        showRows(30, 40);

        assertEquals(List.of("F32"),
                names(spreadsheet.getCellComments().keySet()));
    }

    @Test
    void noCommentsAreLoaded_beforeTheClientSaysWhatItShows() {
        comment("F12");
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);

        spreadsheet.refreshCells(sheet.getRow(11).getCell(5));

        assertNull(spreadsheet.getCellComments());
    }
}
