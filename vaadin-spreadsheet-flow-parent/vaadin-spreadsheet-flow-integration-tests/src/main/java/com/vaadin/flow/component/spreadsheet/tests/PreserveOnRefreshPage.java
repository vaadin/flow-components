package com.vaadin.flow.component.spreadsheet.tests;

import java.io.IOException;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

@Route("vaadin-spreadsheet/preserve-on-refresh")
@PreserveOnRefresh
public class PreserveOnRefreshPage extends Div {

    private Spreadsheet spreadsheet;

    public PreserveOnRefreshPage() {
        setSizeFull();

        Button setSpreadsheetWithChartsButton = new Button("With Charts",
                click -> setSpreadsheetWithCharts());
        setSpreadsheetWithChartsButton.setId("with-charts");
        Button setSpreadsheetWithComponentsButton = new Button("With Button",
                click -> setSpreadsheetWithComponent());
        setSpreadsheetWithComponentsButton.setId("with-button");
        Button setSpreadsheetWithEditorButton = new Button("With TextField",
                click -> setSpreadsheetWithEditor());
        setSpreadsheetWithEditorButton.setId("with-text-field");

        add(setSpreadsheetWithChartsButton, setSpreadsheetWithComponentsButton,
                setSpreadsheetWithEditorButton);
    }

    private void setSpreadsheetWithEditor() {
        if (spreadsheet != null) {
            spreadsheet.removeFromParent();
        }
        spreadsheet = new Spreadsheet();
        spreadsheet.setSpreadsheetComponentFactory(new CustomEditorFactory());
        spreadsheet.setId("spreadsheet");
        add(spreadsheet);
    }

    private void setSpreadsheetWithComponent() {
        if (spreadsheet != null) {
            spreadsheet.removeFromParent();
        }
        spreadsheet = new Spreadsheet();
        spreadsheet
                .setSpreadsheetComponentFactory(new CustomComponentFactory());
        spreadsheet.setId("spreadsheet");
        add(spreadsheet);
    }

    private void setSpreadsheetWithCharts() {
        if (spreadsheet != null) {
            spreadsheet.removeFromParent();
        }
        var stream = getClass().getResourceAsStream("/test_sheets/Bubble.xlsx");
        try {
            spreadsheet = new Spreadsheet(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        spreadsheet.setId("spreadsheet");
        add(spreadsheet);
    }

    private static class CustomComponentFactory
            implements SpreadsheetComponentFactory {

        private final Button button = new Button("Click",
                e -> Notification.show("Button clicked!"));

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            if (rowIndex != 1 || columnIndex != 1) {
                return null;
            }
            return button;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, final int rowIndex,
                final int columnIndex, final Spreadsheet spreadsheet,
                Sheet sheet) {
            return null;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
        }
    }

    private static class CustomEditorFactory
            implements SpreadsheetComponentFactory {

        private TextField textField;

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, final int rowIndex,
                final int columnIndex, final Spreadsheet spreadsheet,
                Sheet sheet) {
            if (rowIndex != 1 || columnIndex != 1) {
                return null;
            }
            if (textField == null) {
                textField = new TextField();
                textField.addValueChangeListener(
                        e -> spreadsheet.refreshCells(spreadsheet.createCell(
                                rowIndex, columnIndex, e.getValue())));
            }
            return textField;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            if (cell == null) {
                return;
            }
            ((HasValue) customEditor).setValue(cell.getStringCellValue());
        }
    }
}
