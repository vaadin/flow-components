package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class CommentFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        Drawing<?> drawing = spreadsheet.getActiveSheet()
                .createDrawingPatriarch();
        CreationHelper factory = spreadsheet.getActiveSheet().getWorkbook()
                .getCreationHelper();

        ClientAnchor anchor1 = factory.createClientAnchor();
        anchor1.setCol1(1);
        anchor1.setCol2(2);
        anchor1.setRow1(3);
        anchor1.setRow2(4);
        Comment comment1 = drawing.createCellComment(anchor1);
        comment1.setString(new XSSFRichTextString("first cell comment"));
        spreadsheet.createCell(0, 0, "cell").setCellComment(comment1);

        ClientAnchor anchor2 = factory.createClientAnchor();
        anchor2.setCol1(4);
        anchor2.setCol2(5);
        anchor2.setRow1(4);
        anchor2.setRow2(5);
        Comment comment2 = drawing.createCellComment(anchor2);
        comment2.setString(new XSSFRichTextString("Always Visible Comment."));
        comment2.setVisible(true);
        spreadsheet.createCell(4, 4, "visible").setCellComment(comment2);

        spreadsheet.refreshAllCellValues();
    }

}
