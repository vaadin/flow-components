/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/locked-cell-with-custom-editor")
public class LockedCellsWithCustomEditorPage extends Div {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LockedCellsWithCustomEditorPage.class);

    private TextField customEditor;

    public LockedCellsWithCustomEditorPage() {
        setSizeFull();
        try {
            var file = loadFile();
            var spreadsheet = loadSpreadSheet(file);
            add(spreadsheet);
        } catch (Exception e) {
            LOGGER.warn("Could not load spreadsheet", e);
        }
    }

    private Spreadsheet loadSpreadSheet(File file) throws IOException {
        var spreadsheet = new Spreadsheet(file);
        spreadsheet.setSizeFull();
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {

                    @Override
                    public Component getCustomComponentForCell(
                            org.apache.poi.ss.usermodel.Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet) {
                        return null;
                    }

                    @Override
                    public Component getCustomEditorForCell(
                            org.apache.poi.ss.usermodel.Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet) {

                        if (spreadsheet.getActiveSheetIndex() == 0
                                && rowIndex == 2 && columnIndex == 2) {
                            if (customEditor == null) {
                                customEditor = new TextField();
                                customEditor.addValueChangeListener(
                                        e -> spreadsheet.refreshCells(
                                                spreadsheet.createCell(rowIndex,
                                                        columnIndex,
                                                        e.getValue())));
                            }
                            return customEditor;
                        }
                        return null;

                    }

                    @Override
                    public void onCustomEditorDisplayed(
                            org.apache.poi.ss.usermodel.Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                        if (cell == null) {
                            return;
                        }
                        ((TextField) customEditor)
                                .setValue(cell.getStringCellValue());
                    }

                });
        spreadsheet.setShowCustomEditorOnFocus(true);
        return spreadsheet;
    }

    private static File loadFile() throws URISyntaxException {
        var classLoader = LockedCellsWithCustomEditorPage.class
                .getClassLoader();
        var resource = classLoader.getResource("test_sheets" + File.separator
                + "locked_cell_next_to_custom_editor.xlsx");
        assert resource != null;
        return new File(resource.toURI());
    }
}
