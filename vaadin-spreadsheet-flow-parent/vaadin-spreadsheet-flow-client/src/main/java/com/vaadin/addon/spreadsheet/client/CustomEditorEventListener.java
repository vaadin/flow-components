/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class CustomEditorEventListener implements EventListener {

    private Slot slot;
    private String key;
    private SpreadsheetWidget widget;

    public void init(Slot slot, String key) {
        this.slot = slot;
        this.key = key;
        Event.setEventListener(slot.getAssignedElement(), this);
        DOM.sinkEvents(slot.getAssignedElement(),
                Event.ONKEYDOWN | Event.FOCUSEVENTS);
    }

    @Override
    public void onBrowserEvent(Event event) {
        switch (event.getTypeInt()) {
        case Event.ONKEYDOWN:
            var sheetWidget = getSheetWidget();
            switch (event.getKeyCode()) {
            case KeyCodes.KEY_TAB:
                event.preventDefault();
                getSheetWidget().focusSheet();
                getSheetWidget().getSheetHandler().onSheetKeyPress(event, "");
                break;
            case KeyCodes.KEY_ESCAPE:
                sheetWidget.focusSheet();
                break;
            }
            break;
        case Event.ONFOCUS:
            var jsniUtil = getSheetWidget().getSheetJsniUtil();
            jsniUtil.parseColRow(key);
            var col = jsniUtil.getParsedCol();
            var row = jsniUtil.getParsedRow();
            getSheetWidget().setSelectedCell(col, row);
            getSheetWidget().updateSelectionOutline(col, col, row, row);
            getSheetWidget().updateSelectedCellStyles(col, col, row, row, true);
            getSpreadsheetWidget().getSpreadsheetHandler().cellSelected(row,
                    col, true);
            slot.setElementFocused(true);
            break;
        case Event.ONBLUR:
            slot.setElementFocused(false);
            break;
        }
    }

    SheetWidget getSheetWidget() {
        return widget.getSheetWidget();
    }

    public void setSpreadsheetWidget(SpreadsheetWidget widget) {
        this.widget = widget;
    }

    SpreadsheetWidget getSpreadsheetWidget() {
        return widget;
    }
}
