package com.vaadin.flow.component.spreadsheet.test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PoiTester {

    public static void main(String[] args) {
        new PoiTester().testComments();
    }

    private void testComments() {

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        Cell cell = getOrCreateCell(sheet, 0, 0);
        cell.setBlank();

        CreationHelper factory = wb.getCreationHelper();
        RichTextString str = factory.createRichTextString("comentario!");
        Comment comment = cell.getCellComment();
        if (comment == null) {
            Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setCol2(cell.getColumnIndex());
            anchor.setRow1(cell.getRowIndex());
            anchor.setRow2(cell.getRowIndex());
            comment = drawingPatriarch.createCellComment(anchor);
            cell.setCellComment(comment);
        }
        comment.setString(str);


        cell = getOrCreateCell(sheet, 0, 0);
        System.out.println(cell.getCellComment().getAuthor());
        System.out.println(cell.getCellComment().getString().getString());
        System.out.println(cell.getCellComment().getColumn());
        System.out.println(cell.getCellComment().getRow());
        System.out.println(cell.getCellComment().getAddress());
        System.out.println(cell.getCellComment().getClientAnchor());

    }

    public Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }

        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }

        return cell;
    }

}
