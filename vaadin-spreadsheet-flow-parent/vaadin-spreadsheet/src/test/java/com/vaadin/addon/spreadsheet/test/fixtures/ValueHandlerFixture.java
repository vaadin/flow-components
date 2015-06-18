package com.vaadin.addon.spreadsheet.test.fixtures;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class ValueHandlerFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        MultiplexerCellValueHandler multiplexer = new MultiplexerCellValueHandler();
        spreadsheet.setCellValueHandler(multiplexer);

        spreadsheet.createCell(0, 1, "Doubler");
        multiplexer.addHandler(1, 1, new Spreadsheet.CellValueHandler() {

            @Override
            public boolean cellValueUpdated(Cell cell, Sheet sheet,
                    int colIndex, int rowIndex, String newValue,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter) {
                if (cell == null) {
                    cell = spreadsheet.createCell(rowIndex, colIndex, "");
                }
                try {
                    cell.setCellValue(Double.parseDouble(newValue) * 2);
                    spreadsheet.refreshCells(cell);
                } catch (NumberFormatException exception) {
                    return true;
                } finally {
                    formulaEvaluator.notifyUpdateCell(cell);
                }
                return false;
            }
        });

        spreadsheet.createCell(0, 2, "Dates");
        Cell c3 = spreadsheet.createCell(2, 2, "");
        final CellStyle dateStyle = c3.getSheet().getWorkbook()
                .createCellStyle();
        c3.setCellFormula("C2+1");
        dateStyle.setDataFormat((short) 15);
        c3.setCellStyle(dateStyle);

        multiplexer.addHandler(1, 2, new Spreadsheet.CellValueHandler() {

            @Override
            public boolean cellValueUpdated(Cell cell, Sheet sheet,
                    int colIndex, int rowIndex, String newValue,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter) {

                if (cell == null) {
                    cell = spreadsheet.createCell(rowIndex, colIndex, "");
                }
                try {
                    cell.setCellValue(new Date(Long.parseLong(newValue)));
                    cell.setCellStyle(dateStyle);
                } catch (NumberFormatException e) {

                    SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy");
                    try {
                        cell.setCellValue(format.parse(newValue));
                    } catch (ParseException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                formulaEvaluator.notifyUpdateCell(cell);
                return false;
            }
        });

        spreadsheet.refreshAllCellValues();
    }

    class DoubleCellValue implements Spreadsheet.CellValueHandler {

        @Override
        public boolean cellValueUpdated(Cell cell, Sheet sheet, int colIndex,
                int rowIndex, String newValue,
                FormulaEvaluator formulaEvaluator, DataFormatter formatter) {

            cell.setCellValue(Double.parseDouble(newValue) * 2);
            return false;
        }
    }
}

class MultiplexerCellValueHandler implements Spreadsheet.CellValueHandler {

    private Map<String, Spreadsheet.CellValueHandler> handlerFactories = new HashMap<String, Spreadsheet.CellValueHandler>();

    public void addHandler(int row, int column,
            Spreadsheet.CellValueHandler handlerFactory) {
        handlerFactories.put(row + ":" + column, handlerFactory);
    }

    @Override
    public boolean cellValueUpdated(Cell cell, Sheet sheet, int colIndex,
            int rowIndex, String newValue, FormulaEvaluator formulaEvaluator,
            DataFormatter formatter) {
        if (!handlerFactories.containsKey(rowIndex + ":" + colIndex)) {
            return true;
        }

        return handlerFactories.get(rowIndex + ":" + colIndex)
                .cellValueUpdated(cell, sheet, colIndex, rowIndex, newValue,
                        formulaEvaluator, formatter);
    }

}

class StackedCellValueHandler implements Spreadsheet.CellValueHandler {

    private List<Spreadsheet.CellValueHandler> handlers = new ArrayList<Spreadsheet.CellValueHandler>();

    public void addHandler(Spreadsheet.CellValueHandler handler) {
        handlers.add(handler);
    }

    @Override
    public boolean cellValueUpdated(Cell cell, Sheet sheet, int colIndex,
            int rowIndex, String newValue, FormulaEvaluator formulaEvaluator,
            DataFormatter formatter) {

        for (Spreadsheet.CellValueHandler handler : handlers) {
            if (!handler.cellValueUpdated(cell, sheet, colIndex, rowIndex,
                    newValue, formulaEvaluator, formatter)) {
                return false;
            }
        }

        return true;
    }

}
