package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class SheetEventListener implements EventListener {

    private SheetWidget widget;

    private boolean sheetFocused;

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
        if (widget.isSelectingCells()) {
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
                widget.onSheetTouchStart(event);
            case Event.ONMOUSEDOWN:
                widget.onSheetMouseDown(event);
                break;
            case Event.ONDBLCLICK:
                onSheetDoubleClick(event);
                break;
            case Event.ONFOCUS:
                sheetFocused = true;
                break;
            case Event.ONBLUR:
                sheetFocused = false;
                break;
            case Event.ONMOUSEOUT:
            case Event.ONMOUSEOVER:
                widget.onSheetMouseOverOrOut(event);
                break;
            case Event.ONTOUCHMOVE:
            case Event.ONMOUSEMOVE:
                // TODO implement proper touch support
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
            jsniUtil.parseColRow(target.getClassName());
            widget.getSheetHandler().onCellDoubleClick(jsniUtil.getParsedCol(),
                    jsniUtil.getParsedRow(), target.getInnerText());
            event.stopPropagation();
        }
    }

    private void onSelectingCellsEvent(Event event) {
        switch (event.getTypeInt()) {
        case Event.ONMOUSEUP:
        case Event.ONTOUCHEND:
        case Event.ONTOUCHCANCEL:
        case Event.ONLOSECAPTURE:
            widget.stoppedSelectingCellsWithDrag(event);
            break;
        case Event.ONTOUCHMOVE: // FIXME remove
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
                widget.getSheetHandler().onSheetKeyPress(event, "");
                // prevent the default browser action, i.e. Chrome would
                // try to navigate to previous page...
                event.preventDefault();
                event.stopPropagation();
                break;
            case 190: // period, which gives same (46) as delete in key press
                widget.getSheetHandler().onSheetKeyPress(event, ".");
                event.preventDefault();
                event.stopPropagation();
            case KeyCodes.KEY_UP:
            case KeyCodes.KEY_DOWN:
            case KeyCodes.KEY_LEFT:
            case KeyCodes.KEY_RIGHT:
            case KeyCodes.KEY_TAB:
            case KeyCodes.KEY_DELETE:
                widget.getSheetHandler().onSheetKeyPress(event, "");
                // prevent the default browser action (scroll to key
                // direction) or switch focus (tab)
                event.preventDefault();
                event.stopPropagation();
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
                event.preventDefault();
                event.stopPropagation();
                break;
            default:
                widget.getSheetHandler().onSheetKeyPress(
                        event,
                        widget.getSheetJsniUtil().convertUnicodeIntoCharacter(
                                charCode));
            }
        }
    }

}
