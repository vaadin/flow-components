/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.Locale;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.action.InsertDeleteCellCommentAction;
import com.vaadin.tests.MockUIExtension;

/**
 * The Insert comment / Delete comment entry of the cell context menu, run
 * against a real workbook.
 */
class InsertDeleteCellCommentActionTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final InsertDeleteCellCommentAction action = new InsertDeleteCellCommentAction();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setLocale(Locale.US);
    }

    @Test
    void insertOnEmptyCell_createsComment() {
        runOn("B3");

        Assertions.assertNotNull(commentAt("B3"));
    }

    @Test
    void insertOnEmptyCell_thenDelete_removesComment() {
        runOn("B3");
        runOn("B3");

        Assertions.assertNull(commentAt("B3"));
    }

    @Test
    void insertOnEmptyCell_writeNote_thenDelete_removesComment() {
        runOn("B3");
        fireUpdateCellCommentEvent(3, 2, "A note");
        runOn("B3");

        Assertions.assertNull(commentAt("B3"));
    }

    @Test
    void deleteOnCellWithValue_removesCommentAndKeepsValue() {
        spreadsheet.createCell(2, 1, "Value");
        runOn("B3");
        runOn("B3");

        Assertions.assertNull(commentAt("B3"));
        Assertions.assertEquals("Value",
                spreadsheet.getCell("B3").getStringCellValue());
    }

    @Test
    void deleteOnCommentWithoutCell_removesComment() {
        // An .xlsx can anchor a note to a cell that has no entry of its own,
        // and no row either.
        Sheet sheet = spreadsheet.getActiveSheet();
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(1);
        anchor.setCol2(2);
        anchor.setRow1(2);
        anchor.setRow2(5);
        Comment comment = sheet.createDrawingPatriarch()
                .createCellComment(anchor);
        comment.setAddress(new CellAddress("B3"));
        comment.setString(factory.createRichTextString("A note"));
        Assertions.assertNull(sheet.getRow(2));

        runOn("B3");

        Assertions.assertNull(commentAt("B3"));
        Assertions.assertNull(sheet.getRow(2),
                "the row made to reach the comment is gone again");
    }

    private void runOn(String address) {
        spreadsheet.setSelection(address);
        SelectionChangeEvent event = spreadsheet.getCellSelectionManager()
                .getLatestSelectionEvent();
        Assertions.assertTrue(
                action.isApplicableForSelection(spreadsheet, event));
        action.executeActionOnSelection(spreadsheet, event);
    }

    private Comment commentAt(String address) {
        return spreadsheet.getActiveSheet()
                .getCellComment(new CellAddress(address));
    }

    private void fireUpdateCellCommentEvent(int row, int col, String text) {
        TestHelper.fireClientEvent(spreadsheet, "updateCellComment",
                "{\"text\": \"" + text + "\", \"col\": " + col + ", \"row\": "
                        + row + "}");
    }
}
