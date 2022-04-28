package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.TextBox;

//TODO merge with FormulaBarWidget? or parts of it?
public class SheetInputEventListener
        implements FocusHandler, KeyPressHandler, KeyDownHandler, ClickHandler,
        MouseDownHandler, BlurHandler, MouseUpHandler {

    private SheetWidget widget;

    private FormulaBarWidget formulaBarWidget;

    public SheetInputEventListener() {
    }

    public void setSheetWidget(SheetWidget sheetWidget,
            FormulaBarWidget formulaBarWidget) {
        widget = sheetWidget;
        this.formulaBarWidget = formulaBarWidget;

        TextBox editor = sheetWidget.getInlineEditor();
        editor.addFocusHandler(this);
        editor.addBlurHandler(this);
        editor.addKeyPressHandler(this);
        editor.addKeyDownHandler(this);
        editor.addClickHandler(this);
        editor.addMouseDownHandler(this);
        editor.addMouseUpHandler(this);
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {

            // let through to underlying cell
            SheetJsniUtil jsniUtil = widget.getSheetJsniUtil();
            jsniUtil.parseColRow(widget.getSelectedCellKey());
            widget.getSheetHandler().onCellRightClick(event.getNativeEvent(),
                    jsniUtil.getParsedCol(), jsniUtil.getParsedRow());
        }
        event.stopPropagation();
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        formulaBarWidget.updateEditorCaretPos(false);
    }

    @Override
    public void onClick(ClickEvent event) {
        if (widget.isEditingCell()) {
            formulaBarWidget.setInlineEdit(true);
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
            case KeyCodes.KEY_DELETE:
                widget.handleInputElementValueChange(true);
                formulaBarWidget.updateFormulaSelectionStyles();
                formulaBarWidget.updateEditorCaretPos(true);
                formulaBarWidget.checkKeyboardNavigation();

                formulaBarWidget.checkEmptyValue();

                break;
            case KeyCodes.KEY_ESCAPE:
                handler.onCellInputCancel();
                formulaBarWidget.clearFormulaSelection();
                break;
            case KeyCodes.KEY_TAB:
                handler.onCellInputTab(widget.getInlineEditor().getValue(),
                        event.isShiftKeyDown());
                formulaBarWidget.clearFormulaSelection();
                // prevent the default tab from happening (switches
                // focus)
                event.preventDefault();
                break;
            case KeyCodes.KEY_UP:
                if (formulaBarWidget.hasLightFocus()) {
                    handler.onCellInputEnter(
                            widget.getInlineEditor().getValue(), true);
                    event.preventDefault();
                } else if (formulaBarWidget.isKeyboardNavigationEnabled()) {
                    formulaBarWidget.moveFormulaCellSelection(
                            event.isShiftKeyDown(), true, false, false);
                    event.preventDefault();
                }
                break;
            case KeyCodes.KEY_DOWN:
                if (formulaBarWidget.hasLightFocus()) {
                    handler.onCellInputEnter(
                            widget.getInlineEditor().getValue(), false);
                    event.preventDefault();
                } else if (formulaBarWidget.isKeyboardNavigationEnabled()) {
                    formulaBarWidget.moveFormulaCellSelection(
                            event.isShiftKeyDown(), false, false, true);
                    event.preventDefault();
                }
                break;
            case KeyCodes.KEY_LEFT:
                if (formulaBarWidget.hasLightFocus()) {
                    handler.onCellInputTab(widget.getInlineEditor().getValue(),
                            true);
                    event.preventDefault();
                } else if (formulaBarWidget.isKeyboardNavigationEnabled()) {
                    formulaBarWidget.moveFormulaCellSelection(
                            event.isShiftKeyDown(), false, false, false);
                    event.preventDefault();
                } else if (formulaBarWidget.isInlineEdit()) {
                    formulaBarWidget.updateEditorCaretPos(true);
                    // prevent scrolling
                    if (widget.getInlineEditor().getCursorPos() == 0) {
                        event.preventDefault();
                    }
                }
                break;
            case KeyCodes.KEY_RIGHT:
                if (formulaBarWidget.hasLightFocus()) {
                    handler.onCellInputTab(widget.getInlineEditor().getValue(),
                            false);
                    event.preventDefault();
                } else if (formulaBarWidget.isKeyboardNavigationEnabled()) {
                    formulaBarWidget.moveFormulaCellSelection(
                            event.isShiftKeyDown(), false, true, false);
                    event.preventDefault();
                } else if (formulaBarWidget.isInlineEdit()) {
                    formulaBarWidget.updateEditorCaretPos(true);
                    // prevent scrolling
                    int cursorPos = widget.getInlineEditor().getCursorPos();
                    int length = widget.getInlineEditor().getValue().length();
                    if (cursorPos == length) {
                        event.preventDefault();
                    }
                }
                break;
            default:
                break;
            }

        } else {
            handler.onSheetKeyPress(event.getNativeEvent(),
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
                widget.getSheetHandler().onCellInputEnter(
                        widget.getInlineEditor().getValue(),
                        event.isShiftKeyDown());
            } else {
                widget.handleInputElementValueChange(true);

                // check if this keypress changes value to a formula
                formulaBarWidget.updateEditorCaretPos(true);
                formulaBarWidget.startInlineEdit(true);

                // update 'selection'
                formulaBarWidget.updateFormulaSelectionStyles();

                formulaBarWidget.checkKeyboardNavigation();
            }
        }
        event.stopPropagation();
    }

    @Override
    public void onFocus(FocusEvent event) {
        if (!formulaBarWidget.isEditingFormula()) {
            widget.setFocused(true);
            widget.getSheetHandler().onCellInputFocus();
        }
        event.stopPropagation();
    }

    @Override
    public void onBlur(BlurEvent event) {

        widget.setFocused(false);
        if (formulaBarWidget.isEditingFormula()) {
            formulaBarWidget.updateEditorCaretPos(false);
        } else if (widget.isEditingCell()) {
            widget.getSheetHandler()
                    .onCellInputBlur(widget.getInlineEditor().getValue());
            formulaBarWidget.clearFormulaSelection();
        }
        event.stopPropagation();
    }

}
