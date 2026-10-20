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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Pins how client events map onto handler calls.
 *
 * The client sends one named object per event. Read positionally -
 * {@code (ArrayNode) event.getData()} - every event would throw a
 * ClassCastException, and nothing a user did would reach the server. These
 * tests pin the field names, and in particular the two handlers whose argument
 * order does not match the order the fields are declared in.
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

    /** An event, with distinct values, and the one handler call it makes. */
    private static Arguments event(String type, String json,
            Consumer<SpreadsheetHandlerImpl> call) {
        return Arguments.of(type, json, call);
    }

    private static Stream<Arguments> everyEvent() {
        return Stream.of(
                event("onConnectorInit", "{}", h -> h.onConnectorInit()),
                event("contextMenuOpenOnSelection", "{\"row\":3,\"column\":4}",
                        h -> h.contextMenuOpenOnSelection(3, 4)),
                event("actionOnCurrentSelection", "{\"actionKey\":\"a1\"}",
                        h -> h.actionOnCurrentSelection("a1")),
                event("rowHeaderContextMenuOpen", "{\"rowIndex\":5}",
                        h -> h.rowHeaderContextMenuOpen(5)),
                event("actionOnRowHeader", "{\"actionKey\":\"r1\"}",
                        h -> h.actionOnRowHeader("r1")),
                event("columnHeaderContextMenuOpen", "{\"columnIndex\":6}",
                        h -> h.columnHeaderContextMenuOpen(6)),
                event("actionOnColumnHeader", "{\"actionKey\":\"c1\"}",
                        h -> h.actionOnColumnHeader("c1")),
                event("onSheetScroll",
                        "{\"firstRow\":1,\"firstColumn\":2,\"lastRow\":30,\"lastColumn\":12}",
                        h -> h.onSheetScroll(1, 2, 30, 12)),
                event("sheetAddressChanged", "{\"value\":\"B12\"}",
                        h -> h.sheetAddressChanged("B12")),
                event("cellSelected",
                        "{\"row\":7,\"column\":3,\"oldSelectionRangeDiscarded\":true}",
                        h -> h.cellSelected(7, 3, true)),
                event("cellRangeSelected",
                        "{\"row1\":2,\"col1\":3,\"row2\":8,\"col2\":9}",
                        h -> h.cellRangeSelected(2, 3, 8, 9)),
                event("cellAddedToSelectionAndSelected",
                        "{\"row\":4,\"column\":5}",
                        h -> h.cellAddedToSelectionAndSelected(4, 5)),
                event("cellsAddedToRangeSelection",
                        "{\"row1\":1,\"col1\":2,\"row2\":3,\"col2\":4}",
                        h -> h.cellsAddedToRangeSelection(1, 2, 3, 4)),
                event("rowSelected", "{\"row\":6,\"firstColumnIndex\":2}",
                        h -> h.rowSelected(6, 2)),
                event("rowAddedToRangeSelection",
                        "{\"row\":7,\"firstColumnIndex\":3}",
                        h -> h.rowAddedToRangeSelection(7, 3)),
                event("columnSelected", "{\"column\":4,\"firstRowIndex\":1}",
                        h -> h.columnSelected(4, 1)),
                event("columnAddedToSelection",
                        "{\"column\":5,\"firstRowIndex\":2}",
                        h -> h.columnAddedToSelection(2, 5)),
                event("selectionIncreasePainted",
                        "{\"r1\":1,\"c1\":2,\"r2\":3,\"c2\":4}",
                        h -> h.selectionIncreasePainted(1, 2, 3, 4)),
                event("selectionDecreasePainted", "{\"row\":5,\"col\":6}",
                        h -> h.selectionDecreasePainted(5, 6)),
                event("cellValueEdited",
                        "{\"row\":2,\"col\":3,\"value\":\"=A1+1\"}",
                        h -> h.cellValueEdited(2, 3, "=A1+1")),
                event("sheetSelected",
                        "{\"sheetIndex\":2,\"scrollLeft\":40,\"scrollTop\":80}",
                        h -> h.sheetSelected(2, 40, 80)),
                event("sheetRenamed",
                        "{\"sheetIndex\":1,\"newName\":\"Budget\"}",
                        h -> h.sheetRenamed(1, "Budget")),
                event("sheetCreated", "{\"scrollLeft\":10,\"scrollTop\":20}",
                        h -> h.sheetCreated(10, 20)),
                event("cellRangePainted",
                        "{\"selectedCellRow\":1,\"selectedCellColumn\":2,\"row1\":3,\"col1\":4,\"row2\":5,\"col2\":6}",
                        h -> h.cellRangePainted(1, 2, 3, 4, 5, 6)),
                event("deleteSelectedCells", "{}",
                        h -> h.deleteSelectedCells()),
                event("linkCellClicked", "{\"row\":5,\"column\":1}",
                        h -> h.linkCellClicked(5, 1)),
                event("rowsResized",
                        "{\"rowIndexes\":[3,5],\"newHeights\":[20.5,30]}",
                        h -> h.rowsResized(
                                new HashMap<>(Map.of(3, 20.5f, 5, 30f)), 3, 1,
                                5, 1)),
                event("columnResized",
                        "{\"colIndexes\":[2],\"newWidths\":[80]}",
                        h -> h.columnResized(new HashMap<>(Map.of(2, 80)), 1, 2,
                                1, 2)),
                event("onRowAutofit", "{\"rowIndex\":8}",
                        h -> h.onRowAutofit(8)),
                event("onColumnAutofit", "{\"columnIndex\":9}",
                        h -> h.onColumnAutofit(9)),
                event("onUndo", "{}", h -> h.onUndo()),
                event("onRedo", "{}", h -> h.onRedo()),
                event("setCellStyleWidthRatios",
                        "{\"styleIndexes\":[0],\"ratios\":[7.5]}",
                        h -> h.setCellStyleWidthRatios(
                                new HashMap<>(Map.of(0, 7.5f)))),
                event("protectedCellWriteAttempted", "{}",
                        h -> h.protectedCellWriteAttempted()),
                event("onPaste", "{\"text\":\"a\\tb\"}",
                        h -> h.onPaste("a\tb")),
                event("clearSelectedCellsOnCut", "{}",
                        h -> h.clearSelectedCellsOnCut()),
                event("updateCellComment",
                        "{\"text\":\"note\",\"col\":3,\"row\":4}",
                        h -> h.updateCellComment("note", 3, 4)),
                event("groupingCollapsed",
                        "{\"cols\":true,\"colIndex\":3,\"collapsed\":false}",
                        h -> h.setGroupingCollapsed(true, 3, false)),
                event("levelHeaderClicked", "{\"cols\":false,\"level\":2}",
                        h -> h.levelHeaderClicked(false, 2)),
                event("popupButtonClick", "{\"row\":4,\"column\":2}",
                        h -> h.onPopupButtonClick(4, 2)),
                event("popupClose", "{\"row\":5,\"column\":3}",
                        h -> h.onPopupClose(5, 3)),
                event("contextMenuClosed", "{}", h -> h.contextMenuClosed()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("everyEvent")
    public void eachEvent_reachesItsOwnHandlerMethod_withItsValues(String type,
            String json, Consumer<SpreadsheetHandlerImpl> call) {
        fire(type, json);

        call.accept(verify(handler));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void everyEventTheClientSends_isInTheTableAbove()
            throws IOException {
        String contract;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(
                "META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.ts")) {
            contract = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        int start = contract.indexOf("export type SpreadsheetEventType =");
        Set<String> sent = new TreeSet<>();
        Matcher m = Pattern.compile("\\|\\s*'([a-zA-Z]+)'").matcher(
                contract.substring(start, contract.indexOf(';', start)));
        while (m.find()) {
            sent.add(m.group(1));
        }

        Set<String> tabled = everyEvent().map(event -> (String) event.get()[0])
                .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(sent, tabled);
    }

    @Test
    public void anEventOfNoKnownType_reachesNoHandlerMethod() {
        fire("cellSelectd", "{\"row\":7,\"column\":3}");
        fire(null, "{}");

        verifyNoInteractions(handler);
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

    @Test
    public void styleWidthRatios_stylesPast32767AreKept() {
        // CellStyle.getIndex() is a short, so the server keys an XLSX
        // workbook's styles past 32767 by negative numbers, and the client
        // reports them by those keys.
        while (source.getWorkbook().getNumCellStyles() < 32770) {
            source.getWorkbook().createCellStyle();
        }
        assertEquals(-32767,
                source.getWorkbook().getCellStyleAt(32769).getIndex());

        fire("setCellStyleWidthRatios",
                "{\"styleIndexes\":[-32768,-32767,-32766,40000],"
                        + "\"ratios\":[7.5,7.5,7.5,7.5]}");

        verify(handler).setCellStyleWidthRatios(
                new HashMap<>(Map.of(-32768, 7.5f, -32767, 7.5f)));
    }
}
