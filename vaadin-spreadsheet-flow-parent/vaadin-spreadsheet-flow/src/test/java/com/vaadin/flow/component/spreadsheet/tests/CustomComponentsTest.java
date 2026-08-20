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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;

class CustomComponentsTest {
    private Spreadsheet spreadsheet;
    private Component customComponent;

    @BeforeEach
    void init() {
        customComponent = new Span("Custom component");

        spreadsheet = new Spreadsheet();
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                    }

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return null;
                    }

                    @Override
                    public Component getCustomComponentForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        if (rowIndex == 1 && columnIndex == 1) {
                            return customComponent;
                        }
                        return null;
                    }
                });

        // Simulate initial scroll with a viewport of 10x10 cells.
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "[1, 1, 10, 10]");
    }

    @Test
    void initialScroll_customComponentAttached() {
        // The custom component should be attached as the spreadsheet's
        // (virtual) child.
        Assertions.assertEquals(spreadsheet, customComponent.getParent().get());
    }

    @Test
    void scrollAway_customComponentDetached() {
        // Scroll away from the cell with the custom component.
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "[10, 10, 20, 20]");

        // The custom component should be detached.
        Assertions.assertFalse(customComponent.getParent().isPresent());
    }

    @Test
    void factoryThrowsForOneCell_otherCellsStillLoad() {
        var goodComponent = new Span("Good component");

        var throwingSpreadsheet = new Spreadsheet();
        throwingSpreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {

                    @Override
                    public Component getCustomComponentForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        // Throw for cell (0, 0)
                        if (rowIndex == 0 && columnIndex == 0) {
                            throw new RuntimeException(
                                    "Simulated factory error");
                        }
                        // Return valid component for cell (1, 1)
                        if (rowIndex == 1 && columnIndex == 1) {
                            return goodComponent;
                        }
                        return null;
                    }

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return null;
                    }

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                    }
                });

        // Simulate viewport covering both cells
        TestHelper.fireClientEvent(throwingSpreadsheet, "onSheetScroll",
                "[1, 1, 10, 10]");

        // The good component should still be attached despite the factory
        // throwing for another cell.
        Assertions.assertEquals(throwingSpreadsheet,
                goodComponent.getParent().get());
    }

    @Test
    void onCustomEditorDisplayedThrows_doesNotPropagate() {
        var editor = new Span("Editor");

        var throwingSpreadsheet = new Spreadsheet();
        throwingSpreadsheet.setSpreadsheetComponentFactory(
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
                        // Return editor for A1
                        if (rowIndex == 0 && columnIndex == 0) {
                            return editor;
                        }
                        return null;
                    }

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                        throw new RuntimeException(
                                "Simulated onCustomEditorDisplayed error");
                    }
                });

        // First scroll registers the editor in the map via
        // loadCustomComponents().
        TestHelper.fireClientEvent(throwingSpreadsheet, "onSheetScroll",
                "[1, 1, 10, 10]");

        // cellSelected triggers onCellSelected() ->
        // loadCustomEditorOnSelectedCell() which calls
        // onCustomEditorDisplayed() for A1. This call site has no
        // surrounding try-catch, so the exception must be caught inside
        // loadCustomEditorOnSelectedCell() itself.
        Assertions.assertDoesNotThrow(() -> TestHelper.fireClientEvent(
                throwingSpreadsheet, "cellSelected", "[1, 1, true]"));
    }

}
