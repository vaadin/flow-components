package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.FormulaFormatter;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueHandlerFixture implements SpreadsheetFixture {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ValueHandlerFixture.class);

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        MultiplexerCellValueHandler multiplexer = new MultiplexerCellValueHandler();
        spreadsheet.setCellValueHandler(multiplexer);

        spreadsheet.createCell(0, 1, "Doubler");
        multiplexer.addHandler(1, 1, new Spreadsheet.CellValueHandler() {

            @Override
            public boolean cellValueUpdated(Cell cell, Sheet sheet,
                    int colIndex, int rowIndex, String newValue,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                    ConditionalFormattingEvaluator conditionalFormattingEvaluator) {
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
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                    ConditionalFormattingEvaluator conditionalFormattingEvaluator) {

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
                        LOGGER.info("ERROR parsing: " + newValue, e);
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
                FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                ConditionalFormattingEvaluator conditionalFormattingEvaluator) {

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
            DataFormatter formatter,
            ConditionalFormattingEvaluator conditionalFormattingEvaluator) {
        if (!handlerFactories.containsKey(rowIndex + ":" + colIndex)) {
            return true;
        }

        return handlerFactories.get(rowIndex + ":" + colIndex).cellValueUpdated(
                cell, sheet, colIndex, rowIndex, newValue, formulaEvaluator,
                formatter, conditionalFormattingEvaluator);
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
            DataFormatter formatter,
            ConditionalFormattingEvaluator conditionalFormattingEvaluator) {

        for (Spreadsheet.CellValueHandler handler : handlers) {
            if (!handler.cellValueUpdated(cell, sheet, colIndex, rowIndex,
                    newValue, formulaEvaluator, formatter,
                    conditionalFormattingEvaluator)) {
                return false;
            }
        }

        return true;
    }

}
