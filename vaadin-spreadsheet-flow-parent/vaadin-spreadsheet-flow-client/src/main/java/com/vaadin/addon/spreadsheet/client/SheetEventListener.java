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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.vaadin.client.BrowserInfo;

public class SheetEventListener implements EventListener {

    private SheetWidget widget;

    private boolean sheetFocused;

    private boolean isMac;

    public void setSheetWidget(SheetWidget sheetWidget) {
        widget = sheetWidget;
        isMac = sheetWidget.isMac();
    }

    public void setSheetPaneElement(Element topLeftPane, Element topRightPane,
            Element bottomLeftPane, Element bottomRightPane) {
        listenToEventsOnPane(topLeftPane);
        listenToEventsOnPane(topRightPane);
        listenToEventsOnPane(bottomLeftPane);
        listenToEventsOnPane(bottomRightPane);
    }

    protected void listenToEventsOnPane(Element sheetElement) {
        Event.sinkEvents(sheetElement,
                Event.ONSCROLL | Event.ONMOUSEDOWN | Event.ONMOUSEMOVE
                        | Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONMOUSEUP
                        | Event.TOUCHEVENTS | Event.ONLOSECAPTURE
                        | Event.ONCLICK | Event.ONDBLCLICK | Event.ONKEYPRESS
                        | Event.ONKEYDOWN | Event.FOCUSEVENTS);
        Event.setEventListener(sheetElement, this);
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (((Element) event.getEventTarget().cast()).getAttribute("class")
                .contains(PopupButtonWidget.BUTTON_CLASSNAME)) {
            widget.setFocused(true);
            return;
        }
        final int typeInt = event.getTypeInt();

        if (typeInt == Event.ONFOCUS) {
            widget.setFocused(true);
            sheetFocused = true;
        } else if (typeInt == Event.ONBLUR) {
            widget.setFocused(false);
            sheetFocused = false;
        } else if (typeInt == Event.ONTOUCHMOVE) {
            // just let the browser scroll, ONSCROLL will result in correct
            // headers
            event.stopPropagation();
        } else if (widget.isMouseButtonDownAndSelecting()) {
            if (Event.ONSCROLL == typeInt) {
                widget.onSheetScroll(event);
            }
            onSelectingCellsEvent(event);
        } else {
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
        Element target = event.getEventTarget().cast();
        String targetClassName = target.getAttribute("class");

        // click target is the inner div because IE10 and 9 are not compatible
        // with 'pointer-events: none'
        if ((BrowserInfo.get().isIE9() || BrowserInfo.get().isIE10())
                && (targetClassName == null || targetClassName.isEmpty())) {
            Element parentElement = target.getParentElement();
            String parentClassName = parentElement.getAttribute("class");
            if (parentClassName.contains("cell")) {
                target = parentElement;
                targetClassName = parentClassName;
            }
        }

        if (target.getParentElement().getAttribute("class").contains("sheet")
                && targetClassName != null
                && targetClassName.contains("cell")) {
            SheetJsniUtil jsniUtil = widget.getSheetJsniUtil();
            if (jsniUtil.isHeader(targetClassName) == 0) {
                jsniUtil.parseColRow(targetClassName);
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
                if ((!isMac && event.getCtrlKey()) || event.getMetaKey()) {
                    widget.getSheetHandler().onRedoPress();
                    event.preventDefault();
                    event.stopPropagation();
                }
                break;
            case 90: // z
                if ((!isMac && event.getCtrlKey()) || event.getMetaKey()) {
                    widget.getSheetHandler().onUndoPress();
                    event.preventDefault();
                    event.stopPropagation();
                }
                break;
            case 65: // a
                if ((!isMac && event.getCtrlKey()) || event.getMetaKey()) {
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

                case KeyCodes.KEY_ENTER:
                    // happens on FF (other browsers have charcode as 13)
                    widget.getSheetHandler().onSheetKeyPress(event,
                            widget.getSheetJsniUtil()
                                    .convertUnicodeIntoCharacter(charCode));
                    break;
                }
            } else if (!event.getCtrlKey() && !event.getMetaKey()) {
                widget.getSheetHandler().onSheetKeyPress(event,
                        widget.getSheetJsniUtil()
                                .convertUnicodeIntoCharacter(charCode));
            }
        }
    }

}
