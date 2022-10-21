package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/embedding")
public class Embedding extends Div {
    public Embedding() {
        setSizeFull();
        Spreadsheet s = new Spreadsheet();
        s.setSizeFull();

        s.setSpreadsheetComponentFactory(new SpreadsheetComponentFactory() {

            @Override
            public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                    Component customEditor) {
            }

            @Override
            public Component getCustomEditorForCell(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
                return null;
            }

            @Override
            public Component getCustomComponentForCell(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
                if (rowIndex == 1 && columnIndex == 1) {
                    return new Button("Click me",
                            event -> Notification.show("Clicked"));
                }
                return null;
            }
        });

        add(s);
    }
}
