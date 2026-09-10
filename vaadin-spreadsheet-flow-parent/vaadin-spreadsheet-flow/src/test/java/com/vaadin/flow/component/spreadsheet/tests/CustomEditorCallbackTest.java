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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;

/**
 * {@code onCustomEditorDisplayed} fires when an editor is newly displayed — not
 * every time the sheet happens to repaint.
 * <p>
 * Everything that reloads the visible cells calls back into
 * {@code loadCustomEditorOnSelectedCell}: a scroll, a resize, extending a
 * range. Firing the callback each time re-runs whatever the application does
 * there, which is typically {@code setValue}, and wipes what the user has typed
 * into the editor.
 */
class CustomEditorCallbackTest {

    private Spreadsheet spreadsheet;
    private List<Component> handedOut;
    private List<String> displayed;

    @BeforeEach
    void init() {
        handedOut = new ArrayList<>();
        displayed = new ArrayList<>();

        spreadsheet = new Spreadsheet();
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                        displayed.add(rowIndex + "," + columnIndex);
                    }

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        // B2 only, in 0-based POI coordinates.
                        if (rowIndex != 1 || columnIndex != 1) {
                            return null;
                        }
                        // A fresh instance per call, which a factory is
                        // free to do and several real ones do. The
                        // spreadsheet is expected to keep the one it has.
                        handedOut.add(new Span("editor"));
                        return handedOut.get(handedOut.size() - 1);
                    }

                    @Override
                    public Component getCustomComponentForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return null;
                    }
                });

        scrollTo(1, 10);
    }

    /** Report a viewport, which is what makes the server reload cells. */
    private void scrollTo(int firstRow, int lastRow) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + firstRow + ", \"firstColumn\": 1,"
                        + " \"lastRow\": " + lastRow + ", \"lastColumn\": 10}");
    }

    private void select(String address) {
        spreadsheet.setSelection(address);
    }

    /**
     * The callback fires from the cell reload, not from the selection write
     * itself, so a test that only selects sees nothing yet.
     */
    private void selectAndReload(String address) {
        spreadsheet.setSelection(address);
        repaint();
    }

    /** A repaint that keeps the viewport, which is what a resize looks like. */
    private void repaint() {
        scrollTo(1, 10);
        // The server skips the reload when the reported bounds are unchanged,
        // so nudge them and come back.
        scrollTo(1, 11);
        scrollTo(1, 10);
    }

    @Test
    void selectEditorCell_callbackFiresOnce() {
        selectAndReload("B2");

        assertEquals(List.of("1,1"), displayed);
    }

    @Test
    void selectEditorCellThenScrollAwayAndBack_callbackDoesNotFireAgain() {
        selectAndReload("B2");
        displayed.clear();

        scrollTo(200, 210);
        scrollTo(1, 10);

        assertEquals(List.of(), displayed);
    }

    @Test
    void repaintWithoutMovingTheSelection_callbackDoesNotFireAgain() {
        selectAndReload("B2");
        displayed.clear();

        repaint();

        assertEquals(List.of(), displayed);
    }

    @Test
    void selectEditorCellTwice_callbackFiresOnce() {
        selectAndReload("B2");
        selectAndReload("B2");

        assertEquals(List.of("1,1"), displayed);
    }

    @Test
    void selectPlainCell_callbackDoesNotFire() {
        selectAndReload("A1");

        assertEquals(List.of(), displayed);
    }

    @Test
    void extendSelectionFromEditorCell_callbackDoesNotFireAgain() {
        selectAndReload("B2");
        displayed.clear();

        // Extending a range keeps B2 as the selected cell and reloads.
        spreadsheet.setSelectionRange(1, 1, 4, 1);
        repaint();

        assertEquals(List.of(), displayed);
    }

    @Test
    void repaint_theEditorIsNotReplaced() {
        // The editor holds what the user has typed into it. Asking the
        // factory again on every repaint throws that away and hands back
        // an empty one.
        selectAndReload("B2");
        int afterFirst = handedOut.size();

        repaint();
        repaint();

        assertEquals(afterFirst, handedOut.size());
    }

    @Test
    void scrollAwayAndBack_theEditorIsNotReplaced() {
        selectAndReload("B2");
        int afterFirst = handedOut.size();

        scrollTo(200, 210);
        scrollTo(1, 10);

        assertEquals(afterFirst, handedOut.size());
    }

    @Test
    void selectAwayFromTheEditorAndBack_callbackFiresAgain() {
        selectAndReload("B2");
        selectAndReload("A1");
        displayed.clear();

        selectAndReload("B2");

        assertEquals(List.of("1,1"), displayed);
    }
}
