package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import java.util.HashMap;

public class SpreadsheetHelper {

    private Spreadsheet spreadsheet;

    public SpreadsheetHelper(Spreadsheet spreadsheet) {
        super();
        this.spreadsheet = spreadsheet;
    }

    public Cell retrieveCell(int row, int column) {
        Sheet sheet = spreadsheet.getActiveSheet();
        Row r = sheet.getRow(row);
        if (r == null) {
            r = sheet.createRow(row);
        }

        Cell cell = r.getCell(column);
        if (cell == null) {
            cell = r.createCell(column);
        }

        return cell;
    }

    public HashMap<String, Cell> selectedCell(
            Spreadsheet.SelectionChangeEvent event) {

        CellMap cells = new CellMap();

        cells.safePut(retrieveCell(event.getSelectedCellReference().getRow(),
                event.getSelectedCellReference().getCol()));

        for (CellReference cellRef : event.getIndividualSelectedCells()) {
            cells.safePut(retrieveCell(cellRef.getRow(), cellRef.getCol()));
        }

        for (CellRangeAddress rangeAddress : event.getCellRangeAddresses()) {
            for (int i = rangeAddress.getFirstColumn(); i <= rangeAddress
                    .getLastColumn(); i++) {
                for (int j = rangeAddress.getFirstRow(); j <= rangeAddress
                        .getLastRow(); j++) {
                    cells.safePut(retrieveCell(j, i));
                }
            }
        }

        return cells;
    }

}

class CellMap extends HashMap<String, Cell> {

    private static final long serialVersionUID = 1L;

    public void put(Cell cell) {
        put(cell.getRowIndex() + ":" + cell.getColumnIndex(), cell);
    }

    public void safePut(Cell cell) {
        if (containsKey(cell.getRowIndex() + ":" + cell.getColumnIndex())) {
            return;
        }
        put(cell);
    }
}
