/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.InvalidFormulaMarks;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * This class is used to store the data of removed row so that it can be
 * restored. This feature is needed for example when user undoes the row
 * deletion.
 */
class RowData implements Serializable {

    private final List<CellData> cellsData = new ArrayList<CellData>();
    private final List<CommentData> commentsWithoutCell = new ArrayList<CommentData>();
    private final List<CellRangeAddress> mergedCells = new ArrayList<CellRangeAddress>();
    private ConditionalFormattingSnapshot conditionalFormatting = null;
    private boolean isCopied;
    private int maxCol;
    private Spreadsheet spreadsheet;
    private Float height;
    private int rowIndex;

    public RowData(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public void copy(int rowIndex) {
        isCopied = true;
        this.rowIndex = rowIndex;
        maxCol = spreadsheet.getLastColumn();
        cellsData.clear();
        mergedCells.clear();
        commentsWithoutCell.clear();

        Row row = spreadsheet.getActiveSheet().getRow(rowIndex);

        height = row == null ? null
                : row.getZeroHeight() ? 0.0F : row.getHeightInPoints();

        if (row != null) {
            copyCellsData(row);
        }

        Sheet sheet = spreadsheet.getActiveSheet();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() == rowIndex) {
                mergedCells.add(mergedRegion);
            }
        }

        conditionalFormatting = ConditionalFormattingSnapshot.ofRow(sheet,
                rowIndex);
    }

    private void copyCellsData(Row row) {
        for (Cell cell : row) {
            if (cell == null) {
                continue;
            } else {
                CellData cellData = new CellData(spreadsheet);
                cellData.read(cell);
                cellsData.add(cellData);
            }
        }

        for (int i = 0; i < maxCol; ++i) {
            Comment cellComment = row.getSheet()
                    .getCellComment(new CellAddress(row.getRowNum(), i));
            Cell cell = row.getCell(i);
            if (cellComment != null && cell == null) {
                CommentData commenData = new CommentData();
                commenData.read(cellComment);
                commentsWithoutCell.add(commenData);
            }
        }
    }

    public boolean isCopied() {
        return isCopied;
    }

    public void writeTo(Row row) {
        for (CellData cellData : cellsData) {
            if (cellData == null) {
                continue;
            }
            int col = cellData.getColumnIndex();
            Cell cell = row.getCell(col);
            if (cell == null) { // Do real check
                cell = row.createCell(col);
            }
            cellData.writeTo(cell);
        }

        for (CommentData comment : commentsWithoutCell) {
            Cell cell = row.createCell(comment.getColumn());
            comment.writeTo(cell);
        }

        for (CellRangeAddress mergedRegion : mergedCells) {
            spreadsheet.addMergedRegion(mergedRegion);
        }

        if (conditionalFormatting != null) {
            conditionalFormatting.restoreTo(row.getSheet());
        }

        if (height != null) {
            spreadsheet.setRowHeight(rowIndex, height);
        }

        isCopied = false;
    }

    private static class CellData implements Serializable {

        private int columnIndex;
        private int rowIndex;
        private CellType cellType;
        private String cellFormula;
        private double numericCellValue;
        private Date dateCellValue;
        private RichTextString richTextCellValue;
        private String stringCellValue;
        private boolean booleanCellValue;
        private byte errorCellValue;
        private CommentData cellComment;
        private Hyperlink hyperlink;
        private CellStyle cellStyle;
        private boolean invalidFormula;
        private Spreadsheet spreadsheet;

        public CellData(Spreadsheet spreadsheet) {
            this.spreadsheet = spreadsheet;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public void read(Cell cell) {
            columnIndex = cell.getColumnIndex();
            rowIndex = cell.getRowIndex();
            if (cell.getCellComment() != null) {
                CommentData commenData = new CommentData();
                commenData.read(cell.getCellComment());
                cellComment = commenData;
            }
            hyperlink = cell.getHyperlink();
            cellStyle = cell.getCellStyle();
            cellType = cell.getCellType();
            // A formula the server could not parse is kept as text, and the
            // flag that says so lives beside the workbook. Restoring the text
            // without it would leave a broken cell looking fine.
            invalidFormula = InvalidFormulaMarks.isMarked(spreadsheet,
                    columnIndex + 1, rowIndex + 1);

            switch (cellType) {
            case _NONE, BLANK -> stringCellValue = cell.getStringCellValue();
            case BOOLEAN -> booleanCellValue = cell.getBooleanCellValue();
            case ERROR -> errorCellValue = cell.getErrorCellValue();
            case FORMULA -> cellFormula = cell.getCellFormula();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // The duplicated null guard is preserved verbatim from
                    // the pre-modernization code; modernizing the redundant
                    // check is out of scope for this slice.
                    if (DateUtil.isCellDateFormatted(cell)) {
                        dateCellValue = cell.getDateCellValue();
                    }
                } else {
                    numericCellValue = cell.getNumericCellValue();
                }
            }
            case STRING -> richTextCellValue = cell.getRichStringCellValue();
            }
        }

        public void writeTo(Cell cell) {
            if (cellComment != null) {
                cellComment.writeTo(cell);
            }
            if (hyperlink != null) {
                cell.setHyperlink(hyperlink);
            }
            cell.setCellStyle(cellStyle);

            switch (cellType) {
            case _NONE, BLANK -> cell.setBlank();
            case BOOLEAN -> cell.setCellValue(booleanCellValue);
            case ERROR -> cell.setCellErrorValue(errorCellValue);
            case FORMULA -> cell.setCellFormula(cellFormula);
            case NUMERIC -> {
                if (dateCellValue != null) {
                    cell.setCellValue(dateCellValue);
                } else {
                    cell.setCellValue(numericCellValue);
                }
            }
            case STRING -> cell.setCellValue(richTextCellValue);
            }

            if (invalidFormula) {
                InvalidFormulaMarks.mark(spreadsheet, cell.getColumnIndex() + 1,
                        cell.getRowIndex() + 1);
            }
        }

    }

    private static class CommentData implements Serializable {

        private ClientAnchor clientAnchor;
        private String author;
        private String text;
        private boolean visible;
        private int row;
        private int column;

        public void read(Comment cellComment) {
            clientAnchor = cellComment.getClientAnchor();
            author = cellComment.getAuthor();
            text = cellComment.getString().getString();
            visible = cellComment.isVisible();
            row = cellComment.getRow();
            column = cellComment.getColumn();
        }

        public void writeTo(Cell cell) {
            Drawing<?> drawingPatriarch = cell.getSheet()
                    .createDrawingPatriarch();
            CreationHelper factory = cell.getSheet().getWorkbook()
                    .getCreationHelper();

            Comment newCellComment = drawingPatriarch
                    .createCellComment(clientAnchor);
            newCellComment.setAuthor(author);
            RichTextString richTextString = factory.createRichTextString(text);
            newCellComment.setString(richTextString);
            newCellComment.setVisible(visible);

            cell.setCellComment(newCellComment);
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }
    }

}
