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
import java.util.Set;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * Groups of rows and columns collapsed and expanded by the user: the client has
 * what a fresh load gives it, the size the application set for the sheet stays,
 * and so do the pictures the client has.
 */
class SpreadsheetGroupingTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private static final FreshLoadHarness.Viewport VIEWPORT = new FreshLoadHarness.Viewport(
            1, 1, 20, 10);

    /** With the components of the fixture, one of them on H13. */
    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            FreshLoadFixture::configure, VIEWPORT);
    private Spreadsheet spreadsheet;

    /**
     * A sheet of values with rows 6 to 8 and columns D and E grouped, a picture
     * over F6:G8, a comment on B7, and the component of the fixture on H13.
     */
    private static Workbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        // The sheet the components of the fixture are on.
        XSSFSheet sheet = workbook.createSheet("First");
        for (int row = 0; row < 20; row++) {
            var cells = sheet.createRow(row);
            for (int col = 0; col < 10; col++) {
                cells.createCell(col).setCellValue(row * 10 + col);
            }
        }
        sheet.groupRow(5, 7);
        sheet.groupColumn(3, 4);
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 5, 7, 8);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        drawing.createPicture(anchor, workbook
                .addPicture(FreshLoadFixture.png(), Workbook.PICTURE_TYPE_PNG));
        Comment comment = drawing.createCellComment(
                drawing.createAnchor(0, 0, 0, 0, 2, 6, 4, 9));
        comment.setString(
                workbook.getCreationHelper().createRichTextString("noted"));
        sheet.getRow(6).getCell(1).setCellComment(comment);
        return workbook;
    }

    @BeforeEach
    void show() {
        spreadsheet = harness.show(workbook());
    }

    private Consumer<Spreadsheet> toggle(boolean columns, int index,
            boolean collapsed) {
        return ignored -> harness.clientEvent("groupingCollapsed",
                "{\"cols\": " + columns + ", \"colIndex\": " + index
                        + ", \"collapsed\": " + collapsed + "}");
    }

    @Test
    void aRowGroupCollapsed_isAsAFreshLoadGivesIt() {
        assertEquals(List.of(), harness.check(toggle(false, 5, true)));
        assertTrue(spreadsheet.isRowHidden(6));
    }

    @Test
    void aRowGroupCollapsedAndExpanded_isAsAFreshLoadGivesIt() {
        harness.check(toggle(false, 5, true));

        assertEquals(List.of(), harness.check(toggle(false, 5, false)));
        assertFalse(spreadsheet.isRowHidden(6));
    }

    @Test
    void aColumnGroupCollapsed_isAsAFreshLoadGivesIt() {
        assertEquals(List.of(), harness.check(toggle(true, 3, true)));
        assertTrue(spreadsheet.isColumnHidden(4));
    }

    @Test
    void aGroupCollapsed_leavesTheSizeTheApplicationSet() {
        Consumer<Spreadsheet> size = s -> {
            s.setMaxRows(300);
            s.setMaxColumns(30);
        };
        size.accept(spreadsheet);
        Consumer<Spreadsheet> fresh = ((Consumer<Spreadsheet>) FreshLoadFixture::configure)
                .andThen(size);

        assertEquals(List.of(), harness.check(toggle(false, 5, true), fresh));
        assertEquals(List.of(), harness.check(toggle(true, 3, true), fresh));
        assertEquals(300, spreadsheet.getRows());
        assertEquals(30, spreadsheet.getColumns());
    }

    @Test
    void aGroupCollapsed_keepsThePictureOnTheClient() {
        Set<String> keys = Set.copyOf(spreadsheet.getResources().keySet());
        assertFalse(keys.isEmpty(), "the picture is shown");

        toggle(true, 3, true).accept(spreadsheet);

        assertEquals(keys, spreadsheet.getResources().keySet());
    }
}
