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

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.command.RowInsertOrDeleteCommand;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * What happens to comments when a row is deleted from the context menu and the
 * delete is undone.
 */
class CommentRowUndoTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void aCommentOfACellThatDoesNotExist_comesBack_whereverTheClientIsScrolled() {
        Spreadsheet spreadsheet = new Spreadsheet();
        Sheet sheet = spreadsheet.getActiveSheet();
        // A note on Z3, a cell that does not exist, right of the columns
        // the client shows.
        CreationHelper helper = spreadsheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setRow1(2);
        anchor.setCol1(25);
        Comment comment = sheet.createDrawingPatriarch()
                .createCellComment(anchor);
        comment.setAddress(new CellAddress("Z3"));
        comment.setString(helper.createRichTextString("far"));
        sheet.createRow(2);
        ui.add(spreadsheet);
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20, \"lastColumn\": 10}");
        RowInsertOrDeleteCommand command = new RowInsertOrDeleteCommand(
                spreadsheet, new CellRangeAddress(2, 2, 0, 0));
        command.execute();

        command.execute();

        assertEquals("far", sheet.getCellComment(new CellAddress("Z3"))
                .getString().getString());
    }
}
