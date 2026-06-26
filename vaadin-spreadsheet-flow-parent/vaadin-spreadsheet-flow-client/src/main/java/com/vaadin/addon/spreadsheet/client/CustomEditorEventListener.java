/**
 * Copyright 2000-2026 Vaadin Ltd.
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

/**
 * Custom editor event listener for handling keyboard events and focus changes
 * in a custom editor slot within a spreadsheet.
 *
 * This class listens for keydown events to handle tab and escape keys, focus
 * events to update the selected cell. The focus/blur events is also used to
 * store the focused state of the assigned element in the slot.
 *
 * It is designed to work with a {@link Slot} that represents a custom editor
 * and a {@link SpreadsheetWidget} that provides the context of the spreadsheet.
 *
 */
public class CustomEditorEventListener implements EventListener {

    private Slot slot;
    private String cellAddress;
    private SpreadsheetWidget widget;
    private boolean userInitiatedFocus;

    public void init(Slot slot, String cellAddress) {
        this.slot = slot;
        this.cellAddress = cellAddress;
        Event.setEventListener(slot.getAssignedElement(), this);
        DOM.sinkEvents(slot.getAssignedElement(), Event.ONKEYDOWN
                | Event.FOCUSEVENTS | Event.ONMOUSEDOWN | Event.ONTOUCHSTART);
    }

    public void setCellAddress(String cellAddress) {
        this.cellAddress = cellAddress;
    }

    public String getCellAddress() {
        return cellAddress;
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
        case Event.ONMOUSEDOWN:
        case Event.ONTOUCHSTART:
            userInitiatedFocus = true;
            break;
        case Event.ONFOCUS:
            slot.setElementFocused(true);
            // Only update selection and notify the server if this focus was
            // triggered by a user interaction (mouse/touch). Programmatic
            // focus changes (e.g. inputElement.select() from
            // onCustomEditorDisplayed) must not update the selection or send
            // cellSelected — doing so would move the selection to a stale
            // cell when the delayed server response arrives, and could
            // create an infinite feedback loop between client and server.
            // Keyboard navigation already handles selection updates and
            // cellSelected through SelectionHandler independently.
            if (userInitiatedFocus) {
                userInitiatedFocus = false;
                var jsniUtil = getSheetWidget().getSheetJsniUtil();
                jsniUtil.parseColRow(cellAddress);
                var col = jsniUtil.getParsedCol();
                var row = jsniUtil.getParsedRow();
                getSheetWidget().setSelectedCell(col, row);
                getSheetWidget().updateSelectionOutline(col, col, row, row);
                getSheetWidget().updateSelectedCellStyles(col, col, row, row,
                        true);
                getSpreadsheetWidget().getSpreadsheetHandler().cellSelected(row,
                        col, true);
            } else if (!cellAddress
                    .equals(getSheetWidget().getSelectedCellKey())) {
                // Programmatic focus (e.g. inputElement.select() from a
                // delayed server response) on a cell the user has already
                // left. Return focus to the sheet so keyboard input isn't
                // captured by a stale editor.
                getSheetWidget().focusSheet();
            }
            break;
        case Event.ONBLUR:
            slot.setElementFocused(false);
            userInitiatedFocus = false;
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
