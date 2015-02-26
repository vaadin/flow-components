package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class SheetEventListener implements EventListener {

    private SheetWidget widget;

    private boolean sheetFocused;

    private boolean scrolling;

    public SheetEventListener() {
    }

    public void setSheetWidget(SheetWidget sheetWidget) {
        widget = sheetWidget;
    }

    public void setSheetPaneElement(Element topLeftPane, Element topRightPane,
            Element bottomLeftPane, Element bottomRightPane) {
        listenToEventsOnPane(topLeftPane);
        listenToEventsOnPane(topRightPane);
        listenToEventsOnPane(bottomLeftPane);
        listenToEventsOnPane(bottomRightPane);
    }

    protected void listenToEventsOnPane(Element sheetElement) {
        Event.sinkEvents(sheetElement, Event.ONSCROLL | Event.ONMOUSEDOWN
                | Event.ONMOUSEMOVE | Event.ONMOUSEOVER | Event.ONMOUSEOUT
                | Event.ONMOUSEUP | Event.TOUCHEVENTS | Event.ONLOSECAPTURE
                | Event.ONCLICK | Event.ONDBLCLICK | Event.ONKEYPRESS
                | Event.ONKEYDOWN | Event.FOCUSEVENTS);
        Event.setEventListener(sheetElement, this);
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (((Element) event.getEventTarget().cast()).getClassName().contains(
                PopupButtonWidget.BUTTON_CLASSNAME)) {
            return;
        }

        if (event.getTypeInt() == Event.ONFOCUS) {
            sheetFocused = true;
        } else if (event.getTypeInt() == Event.ONBLUR) {
            sheetFocused = false;
        } else if (event.getTypeInt() == Event.ONTOUCHMOVE) {
            // just let the browser scroll, ONSCROLL will result in correct
            // headers
            event.stopPropagation();

        } else if (widget.isMouseButtonDownAndSelecting()) {
            onSelectingCellsEvent(event);
        } else {
            final int typeInt = event.getTypeInt();
            switch (typeInt) {
            case Event.ONSCROLL:
                widget.onSheetScroll(event);
                break;
            case Event.ONKEYPRESS:
                onKeyPress(event);
                break;
            case Event.ONKEYDOWN:
                onKeyDown(event);
                break;
            case Event.ONTOUCHSTART:
                // store pos for comparison on touchMove
                scrolling = false;
                break;
            case Event.ONMOUSEDOWN:
                if (event.getButton() != NativeEvent.BUTTON_RIGHT) {
                    widget.onSheetMouseDown(event);
                }
                break;
            case Event.ONMOUSEUP:
                if (event.getButton() == NativeEvent.BUTTON_RIGHT) {
                    // Context menu is displayed on mouse up to prevent
                    // contextmenu event on VContextMenu
                    widget.onSheetMouseDown(event);
                }
                break;
            case Event.ONDBLCLICK:
                onSheetDoubleClick(event);
                break;
            case Event.ONMOUSEOUT:
            case Event.ONMOUSEOVER:
                widget.onSheetMouseOverOrOut(event);
                break;
            case Event.ONMOUSEMOVE:
                widget.onSheetMouseMove(event);
            default:
                break;
            }
        }
    }

    private void onSheetDoubleClick(Event event) {
        final Element target = event.getEventTarget().cast();
        if (target.getParentElement().getClassName().contains("sheet")) {
            SheetJsniUtil jsniUtil = widget.getSheetJsniUtil();
            if (jsniUtil.isHeader(target.getClassName()) == 0) {
                jsniUtil.parseColRow(target.getClassName());
                widget.getSheetHandler().onCellDoubleClick(
                        jsniUtil.getParsedCol(), jsniUtil.getParsedRow(),
                        target.getInnerText());
            }
            event.stopPropagation();
        }
    }

    private void onSelectingCellsEvent(Event event) {
        switch (event.getTypeInt()) {
        case Event.ONTOUCHEND:
        case Event.ONTOUCHCANCEL:
            // scrolling check
            if (scrolling) {
                // don't click when ending scroll
                scrolling = false;
                event.stopPropagation();
                event.preventDefault();
                break;
            }

            // if not moving, select cells:
            widget.onSheetMouseDown(event);
        case Event.ONMOUSEUP:
        case Event.ONLOSECAPTURE:
            widget.stoppedSelectingCellsWithDrag(event);
            break;
        case Event.ONMOUSEMOVE:
            widget.onMouseMoveWhenSelectingCells(event);
            break;
        default:
            break;
        }
    }

    private void onKeyDown(Event event) {
        if (!widget.isEditingCell()) {
            if (!sheetFocused) {
                return; // focus in input or custom editor
            }
            final int keyCode = event.getKeyCode();
            switch (keyCode) {
            case KeyCodes.KEY_BACKSPACE:
            case KeyCodes.KEY_F2:
            case KeyCodes.KEY_UP:
            case KeyCodes.KEY_DOWN:
            case KeyCodes.KEY_LEFT:
            case KeyCodes.KEY_RIGHT:
            case KeyCodes.KEY_TAB:
            case KeyCodes.KEY_DELETE:
            case KeyCodes.KEY_SPACE:
                if (event.getCharCode() == 0) {
                    widget.getSheetHandler().onSheetKeyPress(event, "");
                    // prevent the default browser action (scroll to key
                    // direction) or switch focus (tab)
                    event.preventDefault();
                    event.stopPropagation();
                }
                break;
            case 89: // y
                if (event.getCtrlKey() || event.getMetaKey()) {
                    widget.getSheetHandler().onRedoPress();
                    event.preventDefault();
                    event.stopPropagation();
                }
                break;
            case 90: // z
                if (event.getCtrlKey() || event.getMetaKey()) {
                    widget.getSheetHandler().onUndoPress();
                    event.preventDefault();
                    event.stopPropagation();
                }
                break;
            case 65: // a
                if (event.getCtrlKey() || event.getMetaKey()) {
                    widget.getSheetHandler().selectAll();
                    event.preventDefault();
                    event.stopPropagation();
                }
                break;
            default:
                break;
            }
        }
    }

    private void onKeyPress(Event event) {
        if (!widget.isEditingCell()) {
            if (!sheetFocused) {
                return; // focus in input or custom editor
            }
            final int keyCode = event.getKeyCode();
            final int charCode = event.getCharCode();
            // these have been handled with onKeyDown (FF causes both
            // for some reason!)
            if ((charCode == 122 || charCode == 121)
                    && (event.getCtrlKey() || event.getMetaKey())) {
                event.preventDefault();
                event.stopPropagation();
                return;
            }
            if (charCode == 0) {
                switch (keyCode) {
                // these have been handled with onKeyDown (FF causes both
                // for some reason!)
                case KeyCodes.KEY_UP:
                case KeyCodes.KEY_DOWN:
                case KeyCodes.KEY_LEFT:
                case KeyCodes.KEY_RIGHT:
                case KeyCodes.KEY_TAB:
                case KeyCodes.KEY_BACKSPACE:
                case KeyCodes.KEY_DELETE:
                case KeyCodes.KEY_SPACE:
                    event.preventDefault();
                    event.stopPropagation();
                    break;
                }
            } else {
                widget.getSheetHandler().onSheetKeyPress(
                        event,
                        widget.getSheetJsniUtil().convertUnicodeIntoCharacter(
                                charCode));
            }
        }
    }

}
