/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Pins how client events map onto handler calls.
 *
 * The client sends one named object per event. This used to be read
 * positionally — {@code (ArrayNode) event.getData()} — which threw a
 * ClassCastException on every single event, so nothing a user did ever reached
 * the server. These tests pin the field names, and in particular the two
 * handlers whose argument order does not match the order the fields are
 * declared in.
 */
public class SpreadsheetEventListenerTest {

    private SpreadsheetHandlerImpl handler;
    private SpreadsheetEventListener listener;
    // The sheet the events come from: 200 rows, 52 columns and one cell
    // style. Resize and style indexes are checked against it.
    private Spreadsheet source;

    @BeforeEach
    public void setup() {
        handler = mock(SpreadsheetHandlerImpl.class);
        listener = new SpreadsheetEventListener(handler);
        source = new Spreadsheet();
    }

    private void fire(String type, String json) {
        ObjectNode data = (ObjectNode) JsonMapper.builder().build()
                .readTree(json);
        listener.onComponentEvent(
                new Spreadsheet.SpreadsheetEvent(source, true, type, data));
    }

    @Test
    public void cellSelected_argumentsTakenByName() {
        fire("cellSelected",
                "{\"row\":7,\"column\":3,\"oldSelectionRangeDiscarded\":true}");

        verify(handler).cellSelected(7, 3, true);
    }

    @Test
    public void cellRangeSelected_argumentsTakenByName() {
        fire("cellRangeSelected",
                "{\"row1\":2,\"col1\":3,\"row2\":8,\"col2\":9}");

        verify(handler).cellRangeSelected(2, 3, 8, 9);
    }

    @Test
    public void linkCellClicked_argumentsTakenByName() {
        fire("linkCellClicked", "{\"row\":5,\"column\":1}");

        verify(handler).linkCellClicked(5, 1);
    }

    @Test
    public void columnSelected_takesColumnFirst() {
        fire("columnSelected", "{\"column\":4,\"firstRowIndex\":1}");

        verify(handler).columnSelected(4, 1);
    }

    @Test
    public void columnAddedToSelection_takesRowIndexFirst() {
        // Deliberately the opposite order to columnSelected. Reading the
        // fields by name is what keeps these two from being swapped.
        fire("columnAddedToSelection", "{\"column\":4,\"firstRowIndex\":1}");

        verify(handler).columnAddedToSelection(1, 4);
    }

    @Test
    public void rowAddedToRangeSelection_argumentsTakenByName() {
        fire("rowAddedToRangeSelection", "{\"row\":6,\"firstColumnIndex\":1}");

        verify(handler).rowAddedToRangeSelection(6, 1);
    }

    @Test
    public void cellValueEdited_argumentsTakenByName() {
        fire("cellValueEdited", "{\"row\":2,\"col\":3,\"value\":\"=A1+1\"}");

        verify(handler).cellValueEdited(2, 3, "=A1+1");
    }

    @Test
    public void onSheetScroll_argumentsTakenByName() {
        fire("onSheetScroll",
                "{\"firstRow\":1,\"firstColumn\":2,\"lastRow\":30,\"lastColumn\":12}");

        verify(handler).onSheetScroll(1, 2, 30, 12);
    }

    @Test
    public void sheetSelected_argumentsTakenByName() {
        fire("sheetSelected",
                "{\"sheetIndex\":2,\"scrollLeft\":40,\"scrollTop\":80}");

        verify(handler).sheetSelected(2, 40, 80);
    }

    @Test
    public void sheetAddressChanged_argumentsTakenByName() {
        fire("sheetAddressChanged", "{\"value\":\"B12\"}");

        verify(handler).sheetAddressChanged("B12");
    }

    @Test
    public void contextMenuOpenOnSelection_argumentsTakenByName() {
        fire("contextMenuOpenOnSelection", "{\"row\":3,\"column\":4}");

        verify(handler).contextMenuOpenOnSelection(3, 4);
    }

    @Test
    public void headerContextMenus_useTheirOwnFieldNames() {
        fire("rowHeaderContextMenuOpen", "{\"rowIndex\":9}");
        fire("columnHeaderContextMenuOpen", "{\"columnIndex\":4}");

        verify(handler).rowHeaderContextMenuOpen(9);
        verify(handler).columnHeaderContextMenuOpen(4);
    }

    @Test
    public void groupingCollapsed_argumentsTakenByName() {
        fire("groupingCollapsed",
                "{\"cols\":true,\"colIndex\":3,\"collapsed\":true}");

        verify(handler).setGroupingCollapsed(true, 3, true);
    }

    @Test
    public void popupButtonClick_argumentsTakenByName() {
        fire("popupButtonClick", "{\"row\":4,\"column\":2}");

        verify(handler).onPopupButtonClick(4, 2);
    }

    @Test
    public void onPaste_argumentsTakenByName() {
        fire("onPaste", "{\"text\":\"a\\tb\"}");

        verify(handler).onPaste("a\tb");
    }

    @Test
    public void rowsResized_zipsParallelArraysIntoAMap() {
        fire("rowsResized",
                "{\"rowIndexes\":[3,4],\"newHeights\":[22.5,30.0]}");

        ArgumentCaptor<Map<Integer, Float>> sizes = ArgumentCaptor
                .forClass(Map.class);
        verify(handler).rowsResized(sizes.capture(), anyInt(), anyInt(),
                anyInt(), anyInt());
        org.junit.jupiter.api.Assertions.assertEquals(22.5f,
                sizes.getValue().get(3), 0.001f);
        org.junit.jupiter.api.Assertions.assertEquals(30.0f,
                sizes.getValue().get(4), 0.001f);
    }

    @Test
    public void columnResized_zipsParallelArraysIntoAMap() {
        fire("columnResized", "{\"colIndexes\":[2,5],\"newWidths\":[80,120]}");

        ArgumentCaptor<Map<Integer, Integer>> sizes = ArgumentCaptor
                .forClass(Map.class);
        verify(handler).columnResized(sizes.capture(), anyInt(), anyInt(),
                anyInt(), anyInt());
        org.junit.jupiter.api.Assertions.assertEquals(Integer.valueOf(80),
                sizes.getValue().get(2));
        org.junit.jupiter.api.Assertions.assertEquals(Integer.valueOf(120),
                sizes.getValue().get(5));
    }

    @Test
    public void eventsWithoutPayload_reachTheHandler() {
        fire("onUndo", "{}");
        fire("onRedo", "{}");
        fire("deleteSelectedCells", "{}");
        fire("clearSelectedCellsOnCut", "{}");

        verify(handler).onUndo();
        verify(handler).onRedo();
        verify(handler).deleteSelectedCells();
        verify(handler).clearSelectedCellsOnCut();
    }

    @Test
    public void missingField_readsAsZeroRatherThanThrowing() {
        fire("cellSelected", "{\"row\":7}");

        verify(handler).cellSelected(7, 0, false);
    }

    @Test
    public void fractionalNumber_isRoundedToTheNearestInt() {
        fire("sheetSelected",
                "{\"sheetIndex\":1,\"scrollLeft\":12.4,\"scrollTop\":37.6}");

        verify(handler).sheetSelected(1, 12, 38);
    }

    @Test
    public void numberFieldThatIsNotANumber_readsAsZero() {
        fire("cellSelected", "{\"row\":\"7\",\"column\":{\"x\":1}}");

        verify(handler).cellSelected(0, 0, false);
    }

    @Test
    public void flagThatIsNotABoolean_readsAsFalse() {
        fire("groupingCollapsed",
                "{\"cols\":1.5,\"colIndex\":3,\"collapsed\":[true]}");

        verify(handler).setGroupingCollapsed(false, 3, false);
    }

    @Test
    public void textFieldThatIsNotAScalar_readsAsNull() {
        fire("sheetAddressChanged", "{\"value\":{\"cell\":\"B12\"}}");

        verify(handler).sheetAddressChanged(null);
    }

    @Test
    public void columnResized_fractionalWidthIsRounded() {
        fire("columnResized", "{\"colIndexes\":[2],\"newWidths\":[80.6]}");

        ArgumentCaptor<Map<Integer, Integer>> sizes = ArgumentCaptor
                .forClass(Map.class);
        verify(handler).columnResized(sizes.capture(), anyInt(), anyInt(),
                anyInt(), anyInt());
        assertEquals(Map.of(2, 81), sizes.getValue());
    }

    @Test
    public void rowsResized_entriesThatAreNotNumbersAreLeftOut() {
        fire("rowsResized",
                "{\"rowIndexes\":[3,\"x\",5],\"newHeights\":[20,30,\"y\"]}");

        ArgumentCaptor<Map<Integer, Float>> sizes = ArgumentCaptor
                .forClass(Map.class);
        // The refresh range covers only the rows that are resized.
        verify(handler).rowsResized(sizes.capture(), eq(3), eq(1), eq(3),
                eq(1));
        assertEquals(Map.of(3, 20f), sizes.getValue());
    }

    @Test
    public void rowsResized_indexesOutsideTheSheetAreLeftOut() {
        fire("rowsResized",
                "{\"rowIndexes\":[-1,0,3,201],\"newHeights\":[20,20,20,20]}");

        ArgumentCaptor<Map<Integer, Float>> sizes = ArgumentCaptor
                .forClass(Map.class);
        verify(handler).rowsResized(sizes.capture(), eq(3), eq(1), eq(3),
                eq(1));
        assertEquals(Map.of(3, 20f), sizes.getValue());
    }

    @Test
    public void columnResized_indexesOutsideTheSheetAreLeftOut() {
        fire("columnResized",
                "{\"colIndexes\":[-1,0,4,53],\"newWidths\":[80,80,80,80]}");

        ArgumentCaptor<Map<Integer, Integer>> sizes = ArgumentCaptor
                .forClass(Map.class);
        verify(handler).columnResized(sizes.capture(), eq(1), eq(4), eq(1),
                eq(4));
        assertEquals(Map.of(4, 80), sizes.getValue());
    }

    @Test
    public void resizeWithEveryIndexOutsideTheSheet_doesNotReachTheHandler() {
        fire("rowsResized", "{\"rowIndexes\":[0,201],\"newHeights\":[20,20]}");
        fire("columnResized", "{\"colIndexes\":[0,53],\"newWidths\":[80,80]}");

        verify(handler, never()).rowsResized(any(), anyInt(), anyInt(),
                anyInt(), anyInt());
        verify(handler, never()).columnResized(any(), anyInt(), anyInt(),
                anyInt(), anyInt());
    }

    @Test
    public void styleWidthRatios_stylesTheWorkbookDoesNotHaveAreLeftOut() {
        fire("setCellStyleWidthRatios",
                "{\"styleIndexes\":[-1,0,7],\"ratios\":[7.5,7.5,7.5]}");

        verify(handler).setCellStyleWidthRatios(new HashMap<>(Map.of(0, 7.5f)));
    }
}
