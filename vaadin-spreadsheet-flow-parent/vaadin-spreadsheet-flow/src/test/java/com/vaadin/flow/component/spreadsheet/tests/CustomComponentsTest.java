package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CustomComponentsTest {
    private Spreadsheet spreadsheet;
    private Component customComponent;

    @Before
    public void init() {
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
    public void initialScroll_customComponentAttached() {
        // The custom component should be attached as the spreadsheet's
        // (virtual) child.
        Assert.assertEquals(spreadsheet, customComponent.getParent().get());
    }

    @Test
    public void scrollAway_customComponentDetached() {
        // Scroll away from the cell with the custom component.
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "[10, 10, 20, 20]");

        // The custom component should be detached.
        Assert.assertFalse(customComponent.getParent().isPresent());
    }

}
