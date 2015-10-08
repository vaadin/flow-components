package com.vaadin.addon.spreadsheet.test.fixtures;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.ArrayList;
import java.util.List;

public class AddOrRemoveCommentFixture implements SpreadsheetFixture {

    private Spreadsheet spreadsheet;

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        spreadsheet.addSelectionChangeListener(new Spreadsheet.SelectionChangeListener() {
            @Override
            public void onSelectionChange(Spreadsheet.SelectionChangeEvent event) {
                CellReference ref = event.getSelectedCellReference();
                createCommentToCell(ref.getRow(), ref.getCol());
            }
        });
    }

    private void createCommentToCell(int row, int col) {
        Cell cell = spreadsheet.getCell(row, col);
        if(cell != null) {
            if(cell.getCellComment() != null) {
                cell.removeCellComment();
            } else {
                Comment comment1 = createCommentTo(row, col);
                cell.setCellComment(comment1);
            }
        } else {
            Comment comment1 = createCommentTo(row, col);
            spreadsheet.createCell(row, col, "commented cell").setCellComment(comment1);
        }
        spreadsheet.refreshAllCellValues();
    }

    private Comment createCommentTo(int row, int col) {
        Drawing drawing = spreadsheet.getActiveSheet().createDrawingPatriarch();
        CreationHelper factory = spreadsheet.getActiveSheet().getWorkbook()
                .getCreationHelper();

        ClientAnchor anchor1 = factory.createClientAnchor();
        anchor1.setCol1(col);
        anchor1.setCol2(col);
        anchor1.setRow1(row);
        anchor1.setRow2(row);
        Comment comment1 = drawing.createCellComment(anchor1);
        comment1.setString(new XSSFRichTextString("comment"));
        return comment1;
    }

}
