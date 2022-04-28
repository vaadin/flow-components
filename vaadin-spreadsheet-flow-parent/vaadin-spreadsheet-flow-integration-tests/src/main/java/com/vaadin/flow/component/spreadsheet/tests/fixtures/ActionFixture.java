package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.tests.SpreadsheetActionHandler;
import com.vaadin.flow.component.spreadsheet.tests.SpreadsheetHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;

public class ActionFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        SpreadsheetActionHandler handler = new SpreadsheetActionHandler();

        handler.addCellHandler(new SpreadsheetActionHandler.Cell() {
            @Override
            public void handleAction(Action action,
                    Spreadsheet.SelectionChangeEvent sender,
                    Spreadsheet target) {
                SpreadsheetHelper helper = new SpreadsheetHelper(
                        sender.getSpreadsheet());

                for (Cell cell : helper.selectedCell(sender).values()) {
                    doubleValue(cell);
                }

                sender.getSpreadsheet().refreshAllCellValues();

            }

            @Override
            public Action[] getActions(
                    Spreadsheet.SelectionChangeEvent selection,
                    Spreadsheet sender) {
                return new Action[] { new Action("Double cell values"), };
            }

            private void doubleValue(Cell cell) {
                cell.setCellValue(cell.getNumericCellValue() * 2);
            }
        });

        handler.addCellHandler(new SpreadsheetActionHandler.Cell() {
            @Override
            public void handleAction(Action action,
                    Spreadsheet.SelectionChangeEvent sender,
                    Spreadsheet target) {
                SpreadsheetHelper helper = new SpreadsheetHelper(
                        sender.getSpreadsheet());

                for (Cell cell : helper.selectedCell(sender).values()) {
                    cell.setCellValue(42);
                }

                sender.getSpreadsheet().refreshAllCellValues();
            }

            @Override
            public Action[] getActions(Spreadsheet.SelectionChangeEvent target,
                    Spreadsheet sender) {
                return new Action[] { new Action("Number"), };
            }
        });

        handler.addColumnHandler(new SpreadsheetActionHandler.Column() {
            @Override
            public void handleAction(Action action, CellRangeAddress sender,
                    Spreadsheet target) {
                SpreadsheetHelper helper = new SpreadsheetHelper(target);
                helper.retrieveCell(2, sender.getFirstColumn())
                        .setCellValue("first column");
                helper.retrieveCell(3, sender.getFirstColumn())
                        .setCellValue("last column");

                target.refreshAllCellValues();
            }

            @Override
            public Action[] getActions(CellRangeAddress target,
                    Spreadsheet sender) {
                return new Action[] { new Action("Column action"), };
            }
        });

        spreadsheet.removeDefaultActionHandler();
        spreadsheet.addActionHandler(handler);
    }

}
