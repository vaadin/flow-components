package com.vaadin.flow.component.spreadsheet.command;

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

            switch (cellType) {
            case _NONE:
            case BLANK:
                stringCellValue = cell.getStringCellValue();
                break;
            case BOOLEAN:
                booleanCellValue = cell.getBooleanCellValue();
                break;
            case ERROR:
                errorCellValue = cell.getErrorCellValue();
                break;
            case FORMULA:
                cellFormula = cell.getCellFormula();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        dateCellValue = cell.getDateCellValue();
                    }
                } else {
                    numericCellValue = cell.getNumericCellValue();
                }
                break;
            case STRING:
                richTextCellValue = cell.getRichStringCellValue();
                break;
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
            case _NONE:
            case BLANK:
                cell.setBlank();
                break;
            case BOOLEAN:
                cell.setCellValue(booleanCellValue);
                break;
            case ERROR:
                cell.setCellErrorValue(errorCellValue);
                break;
            case FORMULA:
                cell.setCellFormula(cellFormula);
                break;
            case NUMERIC:
                if (dateCellValue != null) {
                    cell.setCellValue(dateCellValue);
                } else {
                    cell.setCellValue(numericCellValue);
                }
                break;
            case STRING:
                cell.setCellValue(richTextCellValue);
                break;
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
