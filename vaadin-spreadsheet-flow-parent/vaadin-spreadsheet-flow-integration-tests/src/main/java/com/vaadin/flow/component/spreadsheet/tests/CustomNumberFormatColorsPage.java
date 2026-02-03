/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/custom-number-format-colors")
public class CustomNumberFormatColorsPage extends VerticalLayout {

    private Spreadsheet spreadsheet;
    private TextField formatField;

    public CustomNumberFormatColorsPage() {
        setSizeFull();

        createFormatControls();
        createSpreadsheet();
    }

    private void createFormatControls() {
        HorizontalLayout controls = new HorizontalLayout();
        controls.setAlignItems(Alignment.BASELINE);

        formatField = new TextField("Format String");
        formatField.setId("format-field");
        formatField.setWidth("400px");
        formatField.setValue("[Blue]#,##0;[Red](#,##0);[Green]0;[Magenta]@");

        Button applyFormatButton = new Button("Apply Format",
                e -> applyFormatToAllCells());
        applyFormatButton.setId("apply-format-btn");

        controls.add(formatField, applyFormatButton);
        add(controls);
    }

    private void createSpreadsheet() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setSizeFull();
        spreadsheet.setId("spreadsheet");

        // A1: positive, A2: negative, A3: zero, A4: text
        spreadsheet.createCell(0, 0, null).setCellValue(42);
        spreadsheet.createCell(1, 0, null).setCellValue(-75);
        spreadsheet.createCell(2, 0, null).setCellValue(0);
        spreadsheet.createCell(3, 0, "text");

        Div container = new Div(spreadsheet);
        container.setSizeFull();
        add(container);
        setFlexGrow(1, container);
    }

    private void applyFormatToAllCells() {
        String formatString = formatField.getValue();
        if (formatString == null || formatString.isEmpty()) {
            return;
        }

        var workbook = spreadsheet.getWorkbook();
        DataFormat dataFormat = workbook.createDataFormat();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(dataFormat.getFormat(formatString));

        for (int row = 0; row < 4; row++) {
            Cell cell = spreadsheet.getCell(row, 0);
            if (cell != null) {
                cell.setCellStyle(style);
            }
        }
        spreadsheet.refreshAllCellValues();
    }
}
