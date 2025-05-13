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

    private SheetWidget sheetWidget;
    private Slot slot;
    private String key;

    public void setSheetWidget(SheetWidget sheetWidget) {
        this.sheetWidget = sheetWidget;
    }

    SheetWidget getSheetWidget() {
        return sheetWidget;
    }

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
                sheetWidget.focusSheet();
                sheetWidget.getSheetHandler().onSheetKeyPress(event, "");
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
            slot.setElementFocused(true);
            break;
        case Event.ONBLUR:
            slot.setElementFocused(false);
            break;
        }
    }
}
