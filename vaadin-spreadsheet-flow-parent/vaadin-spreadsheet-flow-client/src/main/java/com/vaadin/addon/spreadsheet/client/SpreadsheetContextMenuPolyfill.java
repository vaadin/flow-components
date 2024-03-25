/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.Timer;

/**
 * Handles the context menu event.
 * <p>
 * This class is a polyfill for the context menu event on touch devices. It
 * implements touch event handlers to trigger a context menu event after a long
 * press.
 * </p>
 * <p>
 * On touch start, a timer is started. If the user releases the touch before the
 * timer expires, the timer is cancelled. If the user moves the touch before the
 * timer expires, the timer is cancelled. If the timer expires, a context menu
 * event is triggered.
 * </p>
 *
 */
final class SpreadsheetContextMenuPolyfill implements TouchStartHandler,
        TouchCancelHandler, TouchEndHandler, TouchMoveHandler {
    private final SheetWidget sheetWidget;
    /* default */ private Timer timer;

    /**
     * @param widget
     */
    public SpreadsheetContextMenuPolyfill(SheetWidget widget) {
        this.sheetWidget = widget;
    }

    /**
     * @see TouchCancelHandler#onTouchCancel(TouchCancelEvent)
     */
    @Override
    public void onTouchCancel(TouchCancelEvent event) {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * @see TouchEndHandler#onTouchEnd(TouchEndEvent)
     */
    @Override
    public void onTouchEnd(TouchEndEvent event) {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * @see TouchMoveHandler#onTouchMove(TouchMoveEvent)
     */
    @Override
    public void onTouchMove(TouchMoveEvent event) {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * @see TouchStartHandler#onTouchStart(TouchStartEvent)
     */
    @Override
    public void onTouchStart(TouchStartEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        Element target = nativeEvent.getEventTarget().cast();

        if (target == null) {
            target = sheetWidget.getElement();
        }

        final Element finalTarget = target;

        JsArray<Touch> targetTouches = nativeEvent.getTargetTouches();
        Touch touch = null;
        if (targetTouches != null && targetTouches.length() > 0) {
            touch = targetTouches.get(0);
        }

        final int screenX = touch != null ? touch.getScreenX()
                : nativeEvent.getScreenX();
        final int screenY = touch == null ? nativeEvent.getScreenY()
                : touch.getScreenY();
        final int clientX = touch != null ? touch.getClientX()
                : nativeEvent.getClientX();
        final int clientY = touch != null ? touch.getClientY()
                : nativeEvent.getClientY();
        final EventTarget relatedEventTarget = touch != null ? touch.getTarget()
                : nativeEvent.getEventTarget();
        final Element relatedTarget = relatedEventTarget != null
                ? Element.as(relatedEventTarget)
                : null;

        timer = new Timer() {
            @Override
            public void run() {
                NativeEvent contextMenuEvent = Document.get().createMouseEvent(
                        BrowserEvents.CONTEXTMENU, true, true, 0, screenX,
                        screenY, clientX, clientY, false, false, false, false,
                        NativeEvent.BUTTON_RIGHT, relatedTarget);
                finalTarget.dispatchEvent(contextMenuEvent);
            }
        };

        timer.schedule(750);
    }
}
