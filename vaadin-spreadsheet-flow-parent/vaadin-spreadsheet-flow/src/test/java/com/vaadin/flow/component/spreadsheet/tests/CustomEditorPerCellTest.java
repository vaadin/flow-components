/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * Each cell keeps the editor it was given, whether or not the spreadsheet is
 * attached yet. A node id is -1 for every component until it is attached, so it
 * cannot tell two editors apart before that.
 */
class CustomEditorPerCellTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;
    /** The last editor the factory handed out, by cell address. */
    private final Map<String, Component> handedOut = new HashMap<>();
    private final List<Component> displayed = new ArrayList<>();

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                        displayed.add(customEditor);
                    }

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        // B2 and C2, in 0-based POI coordinates. A fresh
                        // instance per call.
                        if (rowIndex != 1
                                || (columnIndex != 1 && columnIndex != 2)) {
                            return null;
                        }
                        String address = new CellReference(rowIndex,
                                columnIndex).formatAsString();
                        Component editor = new Span(address);
                        handedOut.put(address, editor);
                        return editor;
                    }

                    @Override
                    public Component getCustomComponentForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return null;
                    }
                });
    }

    /** Report a viewport, which is what makes the server load the cells. */
    private void scrollTo(int lastRow) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": " + lastRow
                        + ", \"lastColumn\": 10}");
    }

    private void selectAndReload(String address) {
        spreadsheet.setSelection(address);
        scrollTo(11);
        scrollTo(10);
    }

    @Test
    void twoLoadsBeforeAttach_eachCellIsHandedItsOwnEditor() {
        scrollTo(10);
        scrollTo(11);
        Component b2 = handedOut.get("B2");
        Component c2 = handedOut.get("C2");

        selectAndReload("B2");
        selectAndReload("C2");

        assertNotEquals(b2, c2);
        assertEquals(List.of(b2, c2), displayed);
    }

    @Test
    void attach_clientIsToldTheNodeIdOfEachCellsEditor() {
        scrollTo(10);

        ui.add(spreadsheet);

        JsonNode editorIds = JacksonUtils.getMapper().readTree(
                spreadsheet.getElement().getProperty("cellKeysToEditorIdMap"));
        // Keys are "col<column> row<row>", 1-based.
        assertEquals(nodeId(handedOut.get("B2")),
                editorIds.get("col2 row2").asString());
        assertEquals(nodeId(handedOut.get("C2")),
                editorIds.get("col3 row2").asString());
        assertNotEquals("-1", editorIds.get("col2 row2").asString());
    }

    private static String nodeId(Component component) {
        return Integer.toString(component.getElement().getNode().getId());
    }
}
