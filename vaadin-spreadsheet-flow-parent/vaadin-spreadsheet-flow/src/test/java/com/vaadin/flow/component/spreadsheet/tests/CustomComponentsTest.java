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

}
