package com.vaadin.flow.component.spreadsheet.action;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;

/**
 * Spreadsheet action for inserting or deleting a comment to a cell.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class InsertDeleteCellCommentAction extends SpreadsheetAction {

    public InsertDeleteCellCommentAction() {
        super("");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        if (!spreadsheet.getActiveSheet().getProtect()) {
            if (event.getCellRangeAddresses().size() == 0
                    && event.getIndividualSelectedCells().size() == 0) {
                CellReference cr = event.getSelectedCellReference();
                Comment cellComment = spreadsheet.getActiveSheet()
                        .getCellComment(
                                new CellAddress(cr.getRow(), cr.getCol()));
                if (cellComment == null) {
                    setCaption("Insert comment");
                } else {
                    setCaption("Delete comment");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isApplicableForHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        return false;
    }

    @Override
    public void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        Sheet sheet = spreadsheet.getActiveSheet();
        CellReference cr = event.getSelectedCellReference();
        boolean cellCreated = false, rowCreated = false, commentEdited = false;
        Row row = sheet.getRow(cr.getRow());
        if (row == null) {
            row = sheet.createRow(cr.getRow());
            rowCreated = true;
        }
        Cell cell = spreadsheet.getCell(cr);
        if (cell == null) {
            cell = row.createCell(cr.getCol());
            cellCreated = true;
        }
        if (cell.getCellComment() == null) {
            createCellComment(spreadsheet, sheet, cell, cr);
            commentEdited = true;
        } else {
            cell.removeCellComment();
            if (cellCreated) {
                sheet.getRow(cr.getRow()).removeCell(cell);
            }
            if (rowCreated) {
                sheet.removeRow(sheet.getRow(cr.getRow()));
            }
        }
        if (cell != null) {
            spreadsheet.refreshCells(cell);
        }
        if (commentEdited) {
            spreadsheet.editCellComment(cr);
        }
    }

    private void createCellComment(Spreadsheet spreadsheet, Sheet sheet,
            Cell cell, CellReference cellRef) {
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);

        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString("");
        comment.setString(str);

        // Fetch author from provider or fall back to default
        String author = null;
        if (spreadsheet.getCommentAuthorProvider() != null) {
            author = spreadsheet.getCommentAuthorProvider()
                    .getAuthorForComment(cellRef);
        }
        if (author == null || author.trim().isEmpty()) {
            author = "Spreadsheet User";
        }
        comment.setAuthor(author);

        // Assign the comment to the cell
        cell.setCellComment(comment);
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        throw new UnsupportedOperationException(
                "Cell comment actions can't be executed against a header range.");
    }

}
