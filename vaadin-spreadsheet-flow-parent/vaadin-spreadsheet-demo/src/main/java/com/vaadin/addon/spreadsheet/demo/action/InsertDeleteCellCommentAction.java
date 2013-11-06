package com.vaadin.addon.spreadsheet.demo.action;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.SpreadsheetAction;

public class InsertDeleteCellCommentAction extends SpreadsheetAction {

    public InsertDeleteCellCommentAction() {
        super("");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        if (!spreadsheet.getActiveSheet().getProtect()) {
            if (event.getCellRangeAddresses().length == 0
                    && event.getIndividualSelectedCells().length == 0) {
                CellReference cr = event.getSelectedCellReference();
                Comment cellComment = spreadsheet.getActiveSheet()
                        .getCellComment(cr.getRow(), cr.getCol());
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
        Cell cell = spreadsheet.getCell(cr.getRow(), cr.getCol());
        if (cell == null) {
            Row row = sheet.getRow(cr.getRow());
            if (row == null) {
                row = sheet.createRow(cr.getRow());
            }
            cell = row.createCell(cr.getCol());
            createCellComment(sheet, cell);
        } else {

            if (cell.getCellComment() == null) {
                createCellComment(sheet, cell);
            } else {
                cell.removeCellComment();
            }
        }
        spreadsheet.updateMarkedCells();
    }

    private void createCellComment(Sheet sheet, Cell cell) {
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);

        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory
                .createRichTextString("Demo comment on cell "
                        + CellReference.convertNumToColString(cell
                                .getColumnIndex()) + (cell.getRowIndex() + 1));
        comment.setString(str);
        comment.setAuthor("Spreadsheet user");

        // Assign the comment to the cell
        cell.setCellComment(comment);
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        // TODO throw error
    }

}
