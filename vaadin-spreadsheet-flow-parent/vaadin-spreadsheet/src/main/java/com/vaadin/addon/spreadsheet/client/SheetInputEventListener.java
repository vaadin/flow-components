package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class SheetInputEventListener implements EventListener {

    private SheetWidget widget;
    private InputElement input;

    /**
     * Input has full focus only when it has been clicked. Otherwise i.e. arrow
     * keys change cell selection instead of input carret position.
     */
    private boolean inputFullFocus;

    public SheetInputEventListener() {
    }

    public void setSheetWidget(SheetWidget sheetWidget,
            InputElement inputElement) {
        widget = sheetWidget;
        input = inputElement;

        Event.sinkEvents(input, Event.ONKEYPRESS | Event.ONKEYDOWN
                | Event.FOCUSEVENTS | Event.ONCLICK | Event.ONMOUSEDOWN);
        Event.setEventListener(input, this);
    }

    public void setInputFullFocus(boolean inputFullFocus) {
        this.inputFullFocus = inputFullFocus;
    }

    @Override
    public void onBrowserEvent(Event event) {
        final int type = event.getTypeInt();
        switch (type) {
        case Event.ONFOCUS:
            onInputFocus(event);
            break;
        case Event.ONBLUR:
            onInputBlur(event);
            break;
        case Event.ONKEYDOWN:
            onInputKeyDown(event);
            break;
        case Event.ONKEYPRESS:
            onInputKeyPress(event);
            break;
        case Event.ONCLICK:
            onInputClick(event);
            break;
        case Event.ONMOUSEDOWN:
            onInputMouseDown(event);
            break;
        default:
            break;
        }
    }

    protected void onInputMouseDown(Event event) {
        if (event.getButton() == NativeEvent.BUTTON_RIGHT) {
            SheetJsniUtil jsniUtil = widget.getSheetJsniUtil();
            jsniUtil.parseColRow(widget.getSelectedCellKey());
            widget.getSheetHandler().onCellRightClick(event,
                    jsniUtil.getParsedCol(), jsniUtil.getParsedRow());
        }
        event.stopPropagation();
    }

    protected void onInputFocus(Event event) {
        widget.getSheetHandler().onCellInputFocus();
        event.stopPropagation();
    }

    protected void onInputBlur(Event event) {
        if (widget.isEditingCell()) {
            widget.getSheetHandler().onCellInputBlur(input.getValue());
            event.stopPropagation();
        }
    }

    protected void onInputClick(Event event) {
        if (widget.isEditingCell()) {
            inputFullFocus = true;
        }
        event.stopPropagation();
    }

    protected void onInputKeyPress(Event event) {
        final int keyCode = event.getKeyCode();
        if (widget.isEditingCell()) {
            if (keyCode == KeyCodes.KEY_ENTER) {
                widget.getSheetHandler().onCellInputEnter(input.getValue(),
                        event.getShiftKey());
            } else {
                widget.handleInputElementValueChange(true);
            }
            event.stopPropagation();
        }
    }

    protected void onInputKeyDown(Event event) {
        final int keyCode = event.getKeyCode();
        final SheetHandler handler = widget.getSheetHandler();
        if (widget.isEditingCell()) {
            switch (keyCode) {
            case KeyCodes.KEY_BACKSPACE:
                widget.handleInputElementValueChange(true);
                break;
            case KeyCodes.KEY_ESCAPE:
                handler.onCellInputCancel();
                break;
            case KeyCodes.KEY_TAB:
                handler.onCellInputTab(input.getValue(), event.getShiftKey());
                // prevent the default tab from happening (switches
                // focus)
                event.preventDefault();
                break;
            case KeyCodes.KEY_UP:
                if (!inputFullFocus) {
                    handler.onCellInputEnter(input.getValue(), true);
                }
                break;
            case KeyCodes.KEY_DOWN:
                if (!inputFullFocus) {
                    handler.onCellInputEnter(input.getValue(), false);
                }
                break;
            case KeyCodes.KEY_LEFT:
                if (!inputFullFocus) {
                    handler.onCellInputTab(input.getValue(), true);
                }
                break;
            case KeyCodes.KEY_RIGHT:
                if (!inputFullFocus) {
                    handler.onCellInputTab(input.getValue(), false);
                }
                break;
            default:
                break;
            }
        } else {
            handler.onSheetKeyPress(event, widget.getSheetJsniUtil()
                    .convertUnicodeIntoCharacter(event.getCharCode()));
        }
        event.stopPropagation();
    }

    protected void cellEditingStopped() {
        inputFullFocus = false;
    }

}
