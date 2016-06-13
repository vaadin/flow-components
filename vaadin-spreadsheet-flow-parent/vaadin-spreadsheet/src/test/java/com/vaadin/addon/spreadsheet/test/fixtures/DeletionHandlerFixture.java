package com.vaadin.addon.spreadsheet.test.fixtures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class DeletionHandlerFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        MultiplexerCellDeletionHandler multiplexer = new MultiplexerCellDeletionHandler();
        spreadsheet.setCellDeletionHandler(multiplexer);

        Spreadsheet.CellDeletionHandler notifierAccepter = new Spreadsheet.CellDeletionHandler() {

            @Override
            public boolean cellDeleted(Cell cell, Sheet sheet, int colIndex,
                    int rowIndex, FormulaEvaluator formulaEvaluator,
                    DataFormatter formatter) {
                String message = "Deleting: " + rowIndex + ":" + colIndex;
                Notification.show(message, Type.ERROR_MESSAGE);
                return true;
            }

            @Override
            public boolean individualSelectedCellsDeleted(
                    List<CellReference> individualSelectedCells, Sheet sheet,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter) {
                String message = "Deleting: "
                        + MultiplexerCellDeletionHandler
                                .parseIndividualSelectedCellsKey(individualSelectedCells);
                Notification.show(message, Type.ERROR_MESSAGE);
                return true;
            }

            @Override
            public boolean cellRangeDeleted(
                    List<CellRangeAddress> cellRangeAddresses, Sheet sheet,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter) {
                String message = "Deleting: "
                        + MultiplexerCellDeletionHandler
                                .parseCellRangeKey(cellRangeAddresses);
                Notification.show(message, Type.ERROR_MESSAGE);
                return true;
            }
        };

        Spreadsheet.CellDeletionHandler notifierRefuser = new Spreadsheet.CellDeletionHandler() {

            @Override
            public boolean cellDeleted(Cell cell, Sheet sheet, int colIndex,
                    int rowIndex, FormulaEvaluator formulaEvaluator,
                    DataFormatter formatter) {
                String message = "Attempting to delete: " + rowIndex + ":"
                        + colIndex;
                Notification.show(message, Type.ERROR_MESSAGE);
                return false;
            }

            @Override
            public boolean individualSelectedCellsDeleted(
                    List<CellReference> individualSelectedCells, Sheet sheet,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter) {
                String message = "Attempting to delete: "
                        + MultiplexerCellDeletionHandler
                                .parseIndividualSelectedCellsKey(individualSelectedCells);
                Notification.show(message, Type.ERROR_MESSAGE);
                return false;
            }

            @Override
            public boolean cellRangeDeleted(
                    List<CellRangeAddress> cellRangeAddresses, Sheet sheet,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter) {
                String message = "Attempting to delete: "
                        + MultiplexerCellDeletionHandler
                                .parseCellRangeKey(cellRangeAddresses);
                Notification.show(message, Type.ERROR_MESSAGE);
                return false;
            }
        };
        spreadsheet.createCell(0, 1, "Accepted");
        spreadsheet.createCell(1, 1, "Delete me!");
        spreadsheet.createCell(2, 1, "Delete us too!");
        spreadsheet.createCell(4, 1, "Delete us too!");
        spreadsheet.createCell(5, 1, "Delete this range!");
        spreadsheet.createCell(6, 1, "Delete this range!");
        spreadsheet.createCell(7, 1, "Delete this range!");
        multiplexer.addHandler("1:1", notifierAccepter);
        multiplexer.addHandler("2:1;4:1", notifierAccepter);
        multiplexer.addHandler("5:1-7:1", notifierAccepter);

        spreadsheet.createCell(0, 2, "Refused");
        spreadsheet.createCell(1, 2, "Try to delete me!");
        spreadsheet.createCell(2, 2, "Try to delete us too!");
        spreadsheet.createCell(4, 2, "Try to delete us too!");
        spreadsheet.createCell(5, 2, "Try to delete this range!");
        spreadsheet.createCell(6, 2, "Try to delete this range!");
        spreadsheet.createCell(7, 2, "Try to delete this range!");
        multiplexer.addHandler("1:2", notifierRefuser);
        multiplexer.addHandler("2:2;4:2", notifierRefuser);
        multiplexer.addHandler("5:2-7:2", notifierRefuser);

        spreadsheet.refreshAllCellValues();
    }
}

class MultiplexerCellDeletionHandler implements Spreadsheet.CellDeletionHandler {

    private Map<String, Spreadsheet.CellDeletionHandler> handlerFactories = new HashMap<String, Spreadsheet.CellDeletionHandler>();

    public void addHandler(String key,
            Spreadsheet.CellDeletionHandler handlerFactory) {
        handlerFactories.put(key, handlerFactory);
    }

    @Override
    public boolean cellDeleted(Cell cell, Sheet sheet, int colIndex,
            int rowIndex, FormulaEvaluator formulaEvaluator,
            DataFormatter formatter) {
        if (!handlerFactories.containsKey(rowIndex + ":" + colIndex)) {
            return true;
        }

        return handlerFactories.get(rowIndex + ":" + colIndex).cellDeleted(
                cell, sheet, colIndex, rowIndex, formulaEvaluator, formatter);
    }

    @Override
    public boolean individualSelectedCellsDeleted(
            List<CellReference> individualSelectedCells, Sheet sheet,
            FormulaEvaluator formulaEvaluator, DataFormatter formatter) {
        String key = parseIndividualSelectedCellsKey(individualSelectedCells);
        if (!handlerFactories.containsKey(key)) {
            return true;
        }

        return handlerFactories.get(key).individualSelectedCellsDeleted(
                individualSelectedCells, sheet, formulaEvaluator, formatter);
    }

    public static String parseIndividualSelectedCellsKey(
            List<CellReference> individualSelectedCells) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (CellReference cell : individualSelectedCells) {
            if (!first) {
                sb.append(";");
            } else {
                first = false;
            }
            sb.append(cell.getRow());
            sb.append(":");
            sb.append(cell.getCol());
        }
        String key = sb.toString();
        return key;
    }

    @Override
    public boolean cellRangeDeleted(List<CellRangeAddress> cellRangeAddresses,
            Sheet sheet, FormulaEvaluator formulaEvaluator,
            DataFormatter formatter) {
        String key = parseCellRangeKey(cellRangeAddresses);
        if (!handlerFactories.containsKey(key)) {
            return true;
        }

        return handlerFactories.get(key).cellRangeDeleted(cellRangeAddresses,
                sheet, formulaEvaluator, formatter);
    }

    public static String parseCellRangeKey(
            List<CellRangeAddress> cellRangeAddresses) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (CellRangeAddress cellRange : cellRangeAddresses) {
            if (!first) {
                sb.append(";");
            } else {
                first = false;
            }
            sb.append(cellRange.getFirstRow());
            sb.append(":");
            sb.append(cellRange.getFirstColumn());
            sb.append("-");
            sb.append(cellRange.getLastRow());
            sb.append(":");
            sb.append(cellRange.getLastColumn());
        }
        return sb.toString();
    }

}

class StackedCellDeletionHandler implements Spreadsheet.CellDeletionHandler {

    private List<Spreadsheet.CellDeletionHandler> handlers = new ArrayList<Spreadsheet.CellDeletionHandler>();

    public void addHandler(Spreadsheet.CellDeletionHandler handler) {
        handlers.add(handler);
    }

    @Override
    public boolean cellDeleted(Cell cell, Sheet sheet, int colIndex,
            int rowIndex, FormulaEvaluator formulaEvaluator,
            DataFormatter formatter) {

        for (Spreadsheet.CellDeletionHandler handler : handlers) {
            if (!handler.cellDeleted(cell, sheet, colIndex, rowIndex,
                    formulaEvaluator, formatter)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean individualSelectedCellsDeleted(
            List<CellReference> individualSelectedCells, Sheet sheet,
            FormulaEvaluator formulaEvaluator, DataFormatter formatter) {

        for (Spreadsheet.CellDeletionHandler handler : handlers) {
            if (!handler
                    .individualSelectedCellsDeleted(individualSelectedCells,
                            sheet, formulaEvaluator, formatter)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean cellRangeDeleted(List<CellRangeAddress> cellRangeAddresses,
            Sheet sheet, FormulaEvaluator formulaEvaluator,
            DataFormatter formatter) {

        for (Spreadsheet.CellDeletionHandler handler : handlers) {
            if (!handler.cellRangeDeleted(cellRangeAddresses, sheet,
                    formulaEvaluator, formatter)) {
                return false;
            }
        }

        return true;
    }

}
