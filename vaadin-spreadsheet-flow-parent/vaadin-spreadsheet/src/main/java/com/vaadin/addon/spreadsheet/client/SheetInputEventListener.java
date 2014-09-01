package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.TextBox;

public class SheetInputEventListener implements FocusHandler, KeyPressHandler,
        KeyDownHandler, ClickHandler, MouseDownHandler, BlurHandler {

    private SheetWidget widget;
    private TextBox input;

    /**
     * Input has full focus only when it has been clicked. Otherwise i.e. arrow
     * keys change cell selection instead of input carret position.
     */
    private boolean inputFullFocus;

    public SheetInputEventListener() {
    }

    public void setSheetWidget(SheetWidget sheetWidget, TextBox input) {
        widget = sheetWidget;
        this.input = input;

        input.addFocusHandler(this);
        input.addBlurHandler(this);
        input.addKeyPressHandler(this);
        input.addKeyDownHandler(this);
        input.addClickHandler(this);
        input.addMouseDownHandler(this);
    }

    public void setInputFullFocus(boolean inputFullFocus) {
        this.inputFullFocus = inputFullFocus;
    }

    protected void cellEditingStopped() {
        inputFullFocus = false;
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
            SheetJsniUtil jsniUtil = widget.getSheetJsniUtil();
            jsniUtil.parseColRow(widget.getSelectedCellKey());
            widget.getSheetHandler().onCellRightClick(event.getNativeEvent(),
                    jsniUtil.getParsedCol(), jsniUtil.getParsedRow());
        }
        event.stopPropagation();
    }

    @Override
    public void onClick(ClickEvent event) {
        if (widget.isEditingCell()) {
            inputFullFocus = true;
        }
        event.stopPropagation();
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        final int keyCode = event.getNativeKeyCode();
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
                handler.onCellInputTab(input.getValue(), event.isShiftKeyDown());
                // prevent the default tab from happening (switches
                // focus)
                event.preventDefault();
                break;
            case KeyCodes.KEY_UP:
                if (!inputFullFocus) {
                    handler.onCellInputEnter(input.getValue(), true);
                    event.preventDefault();
                }
                break;
            case KeyCodes.KEY_DOWN:
                if (!inputFullFocus) {
                    handler.onCellInputEnter(input.getValue(), false);
                    event.preventDefault();
                }
                break;
            case KeyCodes.KEY_LEFT:
                if (!inputFullFocus) {
                    handler.onCellInputTab(input.getValue(), true);
                    event.preventDefault();
                }
                break;
            case KeyCodes.KEY_RIGHT:
                if (!inputFullFocus) {
                    handler.onCellInputTab(input.getValue(), false);
                    event.preventDefault();
                }
                break;
            default:
                break;
            }
        } else {
            handler.onSheetKeyPress(
                    event.getNativeEvent(),
                    widget.getSheetJsniUtil().convertUnicodeIntoCharacter(
                            event.getNativeEvent().getCharCode()));
        }
        event.stopPropagation();
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
        final int keyCode = event.getNativeEvent().getKeyCode();
        if (widget.isEditingCell()) {
            if (keyCode == KeyCodes.KEY_ENTER) {
                widget.getSheetHandler().onCellInputEnter(input.getValue(),
                        event.isShiftKeyDown());
            } else {
                widget.handleInputElementValueChange(true);
            }
        }
        event.stopPropagation();
    }

    @Override
    public void onFocus(FocusEvent event) {
        widget.getSheetHandler().onCellInputFocus();
        event.stopPropagation();
    }

    @Override
    public void onBlur(BlurEvent event) {
        if (widget.isEditingCell()) {
            widget.getSheetHandler().onCellInputBlur(input.getValue());
        }
        event.stopPropagation();
    }

}
