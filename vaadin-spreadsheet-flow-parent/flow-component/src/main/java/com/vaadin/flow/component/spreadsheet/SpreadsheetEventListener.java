package com.vaadin.flow.component.spreadsheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ComponentEventListener;

public class SpreadsheetEventListener implements ComponentEventListener<Spreadsheet.SpreadsheetEvent> {
    private final SpreadsheetHandlerImpl handler;

    public SpreadsheetEventListener(SpreadsheetHandlerImpl spreadsheetHandler) {
        this.handler = spreadsheetHandler;
    }

    @Override
    public void onComponentEvent(Spreadsheet.SpreadsheetEvent event) {
        System.out.println("received " + event.getMessage() + ": " + event.getPayload());
        List<String> tokens = parse(event.getPayload());
        if ("onConnectorInit".equals(event.getMessage())) {
            handler.onConnectorInit();
        } else if ("contextMenuOpenOnSelection".equals(event.getMessage())) {
            handler.contextMenuOpenOnSelection(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("actionOnCurrentSelection".equals(event.getMessage())) {
            handler.actionOnCurrentSelection(event.getPayload());
        } else if ("rowHeaderContextMenuOpen".equals(event.getMessage())) {
            handler.rowHeaderContextMenuOpen(Integer.parseInt(event.getPayload()));
        } else if ("actionOnRowHeader".equals(event.getMessage())) {
            handler.actionOnRowHeader(event.getPayload());
        } else if ("columnHeaderContextMenuOpen".equals(event.getMessage())) {
            handler.columnHeaderContextMenuOpen(Integer.parseInt(event.getPayload()));
        } else if ("actionOnColumnHeader".equals(event.getMessage())) {
            handler.actionOnColumnHeader(event.getPayload());
        } else if ("onSheetScroll".equals(event.getMessage())) {
            handler.onSheetScroll(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3)));
        } else if ("sheetAddressChanged".equals(event.getMessage())) {
            handler.sheetAddressChanged(event.getPayload());
        } else if ("cellSelected".equals(event.getMessage())) {
            handler.cellSelected(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)), Boolean.parseBoolean(tokens.get(2)));
        } else if ("cellRangeSelected".equals(event.getMessage())) {
            handler.cellRangeSelected(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3)));
        } else if ("cellAddedToSelectionAndSelected".equals(event.getMessage())) {
            handler.cellAddedToSelectionAndSelected(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("cellsAddedToRangeSelection".equals(event.getMessage())) {
            handler.cellsAddedToRangeSelection(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3)));
        } else if ("rowSelected".equals(event.getMessage())) {
            handler.rowSelected(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("rowAddedToRangeSelection".equals(event.getMessage())) {
            handler.rowAddedToRangeSelection(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("columnSelected".equals(event.getMessage())) {
            handler.columnSelected(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("columnAddedToSelection".equals(event.getMessage())) {
            handler.columnAddedToSelection(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("selectionIncreasePainted".equals(event.getMessage())) {
            handler.selectionIncreasePainted(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3)));
        } else if ("selectionDecreasePainted".equals(event.getMessage())) {
            handler.selectionDecreasePainted(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("cellValueEdited".equals(event.getMessage())) {
            handler.cellValueEdited(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)), tokens.get(2));
        } else if ("sheetSelected".equals(event.getMessage())) {
            handler.sheetSelected(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)));
        } else if ("sheetRenamed".equals(event.getMessage())) {
            handler.sheetRenamed(Integer.parseInt(tokens.get(0)), tokens.get(1));
        } else if ("sheetCreated".equals(event.getMessage())) {
            handler.sheetCreated(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("cellRangePainted".equals(event.getMessage())) {
            handler.cellRangePainted(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3)), Integer.parseInt(tokens.get(4)), Integer.parseInt(tokens.get(5)));
        } else if ("deleteSelectedCells".equals(event.getMessage())) {
            handler.deleteSelectedCells();
        } else if ("linkCellClicked".equals(event.getMessage())) {
            handler.linkCellClicked(Integer.parseInt(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        } else if ("rowsResized".equals(event.getMessage())) {
            handler.rowsResized(parseMapIntegerFloat(tokens.get(0)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3)), Integer.parseInt(tokens.get(4)));
        } else if ("columnResized".equals(event.getMessage())) {
            handler.columnResized(parseMapIntegerInteger(tokens.get(0)), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)), Integer.parseInt(tokens.get(3)), Integer.parseInt(tokens.get(4)));
        } else if ("onRowAutofit".equals(event.getMessage())) {
            handler.onRowAutofit(Integer.parseInt(tokens.get(0)));
        } else if ("onColumnAutofit".equals(event.getMessage())) {
            handler.onColumnAutofit(Integer.parseInt(tokens.get(0)));
        } else if ("onUndo".equals(event.getMessage())) {
            handler.onUndo();
        } else if ("onRedo".equals(event.getMessage())) {
            handler.onRedo();
        } else if ("setCellStyleWidthRatios".equals(event.getMessage())) {
            handler.setCellStyleWidthRatios(tokens.size() > 0?parseMapIntegerFloat(tokens.get(0)):new HashMap<>());
        } else if ("protectedCellWriteAttempted".equals(event.getMessage())) {
            handler.protectedCellWriteAttempted();
        } else if ("onPaste".equals(event.getMessage())) {
            handler.onPaste(tokens.get(0));
        } else if ("clearSelectedCellsOnCut".equals(event.getMessage())) {
            handler.clearSelectedCellsOnCut();
        } else if ("updateCellComment".equals(event.getMessage())) {
            handler.updateCellComment(tokens.get(0), Integer.parseInt(tokens.get(1)), Integer.parseInt(tokens.get(2)));
        } else if ("groupingCollapsed".equals(event.getMessage())) {
            handler.setGroupingCollapsed(Boolean.parseBoolean(tokens.get(0)), Integer.parseInt(tokens.get(1)), Boolean.parseBoolean(tokens.get(2)));
        } else if ("levelHeaderClicked".equals(event.getMessage())) {
            handler.levelHeaderClicked(Boolean.parseBoolean(tokens.get(0)), Integer.parseInt(tokens.get(1)));
        }

    }

    private Map<Integer, Integer> parseMapIntegerInteger(String s) {
        HashMap<Integer, Integer> m = new HashMap<>();
        if (s != null) {
            for (String t : s.split("\\|")) {
                String[] n = t.split("#");
                m.put(Integer.parseInt(n[0]), Integer.parseInt(n[1]));
            }
        }
        return m;
    }

    private HashMap<Integer, Float> parseMapIntegerFloat(String s) {
        HashMap<Integer, Float> m = new HashMap<>();
        if (s != null) {
            for (String t : s.split("\\|")) {
                String[] n = t.split("#");
                m.put(Integer.parseInt(n[0]), Float.parseFloat(n[1]));
            }
        }
        return m;
    }

    private List<String> parse(String payload) {
        return parse(payload, ',');
    }

    private List<String> parse(String payload, char separator) {
        ArrayList<String> tokens = new ArrayList<>();
        if (payload != null) {
            int pos = 0;
            int start = 0;
            boolean hasNonString = false;
            boolean insideString = false;
            boolean escaped = false;
            while (pos < payload.length()) {
                if (!escaped && separator == payload.charAt(pos) && !insideString) {
                    if (pos > start) tokens.add(payload.substring(hasNonString?start:start + 1, hasNonString?pos:pos - 1).replaceAll("\\\\", ""));
                    else tokens.add("");
                    start = pos + 1;
                    hasNonString = false;
                } else if ('"' == payload.charAt(pos)) {
                    if (!escaped) {
                        if (insideString) { // end of string
                            insideString = false;
                        } else { // start of string
                            insideString = true;
                        }
                    } else {
                        escaped = false;
                    }
                } else if ('\\' == payload.charAt(pos)) {
                    escaped = true;
                } else {
                    if (escaped) escaped = false;
                    if (!insideString) hasNonString = true;
                }
                pos++;
            }
            if (pos > start) tokens.add(payload.substring(hasNonString?start:start + 1, hasNonString?pos:pos - 1).replaceAll("\\\\", ""));
        }
        return tokens;
    }

}
