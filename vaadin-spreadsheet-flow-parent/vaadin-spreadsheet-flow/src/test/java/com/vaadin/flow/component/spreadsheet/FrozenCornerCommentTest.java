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
import static org.mockito.Mockito.mock;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;

/**
 * A comment in the frozen corner of a sheet.
 *
 * <p>
 * Every load of the visible comments empties the comment maps and scans the
 * panes again. The comment loader scanned the frozen corner on the first load
 * only, so the next scroll or refresh took the corner's comments away: a
 * comment on A1 of a sheet with frozen rows and columns showed once and was
 * gone.
 */
class FrozenCornerCommentTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void setUp() {
        SpreadsheetClientRpc rpc = mock(SpreadsheetClientRpc.class);
        spreadsheet = new Spreadsheet() {
            @Override
            protected SpreadsheetClientRpc getRpcProxy() {
                return rpc;
            }
        };
        Drawing<?> drawing = spreadsheet.getActiveSheet()
                .createDrawingPatriarch();
        CreationHelper factory = spreadsheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString("First cell comment"));
        spreadsheet.createCell(0, 0, "cell").setCellComment(comment);
        // Two rows and one column frozen: A1 is in the corner.
        spreadsheet.createFreezePane(2, 1);
    }

    @Test
    void cornerComment_isThereOnTheFirstLoad() {
        spreadsheet.onSheetScroll(3, 2, 30, 15);

        assertEquals("First cell comment",
                spreadsheet.sheetModel().cellComments.get().get("col1 row1"));
    }

    @Test
    void cornerComment_staysThroughTheNextLoad() {
        spreadsheet.onSheetScroll(3, 2, 30, 15);
        // Scrolled on: the comments are loaded again.
        spreadsheet.onSheetScroll(20, 2, 50, 15);

        assertEquals("First cell comment",
                spreadsheet.sheetModel().cellComments.get().get("col1 row1"));
    }
}
