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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/merged-cell-with-custom-editor")
public class MergedCellWithCustomEditorPage extends VerticalLayout {

    private static final int MERGED_CELL_ROW = 1;
    private static final int MERGED_CELL_COL = 1;

    private final TextField customEditor = new TextField();

    public MergedCellWithCustomEditorPage() {
        setSizeFull();

        var spreadsheet = new Spreadsheet();
        spreadsheet.setSizeFull();

        spreadsheet.createCell(MERGED_CELL_ROW, MERGED_CELL_COL, "Merged cell");

        spreadsheet.addMergedRegion(new CellRangeAddress(MERGED_CELL_ROW,
                MERGED_CELL_ROW + 1, MERGED_CELL_COL, MERGED_CELL_COL + 1));

        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {

                    @Override
                    public Component getCustomComponentForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return null;
                    }

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        if (rowIndex == MERGED_CELL_ROW
                                && columnIndex == MERGED_CELL_COL) {
                            return customEditor;
                        }
                        return null;
                    }

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                        // NO-OP
                    }
                });

        var controls = new HorizontalLayout();
        var refreshMergedCell = new Button("Refresh merged cell", event -> {
            var cell = spreadsheet.getCell(MERGED_CELL_ROW, MERGED_CELL_COL);
            spreadsheet.refreshCells(cell);
        });
        refreshMergedCell.setId("refresh-merged-cell");
        controls.add(refreshMergedCell);

        add(controls, spreadsheet);
        setFlexGrow(1, spreadsheet);
    }
}
