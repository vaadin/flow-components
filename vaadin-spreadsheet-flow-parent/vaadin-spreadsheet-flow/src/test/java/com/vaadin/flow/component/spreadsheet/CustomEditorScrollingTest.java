/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * Custom editors as the sheet scrolls: a factory that makes a new editor for
 * every cell of rows 1-1,000 and columns A-J.
 */
class CustomEditorScrollingTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;
    private final List<Component> handedOut = new ArrayList<>();

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setMaxRows(1000);
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        if (rowIndex >= 1000 || columnIndex >= 10) {
                            return null;
                        }
                        Component editor = new Span();
                        handedOut.add(editor);
                        return editor;
                    }

                    @Override
                    public Component getCustomComponentForCell(Cell cell,
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
    }

    /** Report rows first to last, 1-based, of columns A-J as shown. */
    private void scrollTo(int firstRow, int lastRow) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + firstRow
                        + ", \"firstColumn\": 1, \"lastRow\": " + lastRow
                        + ", \"lastColumn\": 10}");
    }

    private Component editorOf(String cellKey) {
        return spreadsheet.getCellKeyToEditorMap().get(cellKey);
    }

    private boolean registered(Component editor) {
        return editor.getElement().isVirtualChild() && spreadsheet.getElement()
                .equals(editor.getElement().getParent());
    }

    @Test
    void aCellStillLoaded_keepsItsEditor() {
        scrollTo(1, 30);
        Component b2 = editorOf("col2 row2");
        assertNotNull(b2);

        scrollTo(2, 31);

        assertSame(b2, editorOf("col2 row2"));
        assertTrue(registered(b2));
    }

    @Test
    void theSelectedCell_keepsItsEditorWhenItIsScrolledAway() {
        // What the user is typing into it is in no cell yet.
        scrollTo(1, 30);
        spreadsheet.setSelection("B2");
        Component b2 = editorOf("col2 row2");

        scrollTo(101, 130);

        assertSame(b2, editorOf("col2 row2"));
        assertTrue(registered(b2));
    }
}
