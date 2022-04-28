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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.TextArea;

/**
 * TextArea that handles cut, copy and paste events inside the spreadsheet.
 * <p>
 * It works by intercepting the keyboard shortcuts for cut, copy and paste and
 * moving focus to a hidden textfield. In the textfield we can listen to oncopy
 * and oncut events, and put our string into the HTML clipboard instead of the
 * normal one.
 *
 */
public class CopyPasteTextBox extends TextArea implements NativePreviewHandler {

    /**
     * Handler interface for dealing with the data that is transferred in copy,
     * cut and paste operations.
     *
     * @author Thomas Mattsson / Vaadin Ltd.
     */
    public interface CopyPasteHandler {

        /**
         * Called when user has performed a copy operation.
         */
        public void onCut();

        /**
         * Called when user has performed a copy operation.
         */
        public void onCopy();

        /**
         * Called when the user has performed a paste operation.
         *
         * @param text
         *            the pasted text
         */
        public void onPaste(String text);

        /**
         * @return Textual representation to be put on the systems text
         *         clipboard.
         */
        public String getClipboardText();

    }

    private SheetWidget widget;
    private CopyPasteHandler handler;
    private HandlerRegistration nativePreviewHandlerRegistration;

    public CopyPasteTextBox(SheetWidget widget, CopyPasteHandler handler) {

        this.widget = widget;
        this.handler = handler;

        getElement().getStyle().setPosition(Position.ABSOLUTE);
        getElement().getStyle().setZIndex(1);
        getElement().getStyle().setLeft(-1000, Unit.PX);

        // gets round browser security (field must be 'visible' when copying)
        getElement().getStyle().setOpacity(0);
    }

    /**
     * Register this instance to listen to all native events
     */
    public void registerHandler() {
        if (nativePreviewHandlerRegistration == null) {
            nativePreviewHandlerRegistration = Event
                    .addNativePreviewHandler(this);
        }
    }

    @Override
    public void onPreviewNativeEvent(NativePreviewEvent event) {
        final NativeEvent nativeEvent = event.getNativeEvent();
        switch (Event.getTypeInt(nativeEvent.getType())) {
        case Event.ONKEYDOWN:
            onKeyDown(nativeEvent);
            break;

        default:
            break;
        }
    }

    private void onKeyDown(final NativeEvent event) {
        boolean metaOrCtrlPressed = (!widget.isMac() && event.getCtrlKey())
                || event.getMetaKey();
        Element cast = event.getEventTarget().cast();
        boolean sheetFocused = widget.isSheetElement(cast);
        if (!metaOrCtrlPressed || !sheetFocused) {
            return;
        }

        // copy
        if (event.getKeyCode() == 67 || event.getKeyCode() == 88) { // C or X

            // before copy goes through, move the focus and selection to this
            // field
            setFocus(true);
            this.setValue(handler.getClipboardText());
            selectAll();

            // also, we need to move the field into the visible area of the
            // browser
            getElement().getStyle().setLeft(100, Unit.PX);

            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                @Override
                public boolean execute() {

                    // after copy, move focus back
                    widget.focusSheet();

                    // hide element
                    getElement().getStyle().setLeft(-1000, Unit.PX);

                    if (event.getKeyCode() == 67) {
                        handler.onCopy();
                    } else if (event.getKeyCode() == 88) {
                        handler.onCut();
                    }
                    return false;
                }
            }, 100);

            // browser copy happens here
        }

        // paste
        if (event.getKeyCode() == 86) {// V
            setText("");
            setFocus(true);

            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                @Override
                public boolean execute() {

                    // get pasted data
                    String data = getValue();
                    handler.onPaste(data);

                    // .. and move focus back
                    widget.focusSheet();

                    return false;
                }
            }, 100);

            // paste happens here
        }
    }

    /**
     * Removes the registration of the handler responsible for native events
     */
    public void onDestroy() {
        if (nativePreviewHandlerRegistration != null) {
            nativePreviewHandlerRegistration.removeHandler();
            nativePreviewHandlerRegistration = null;
        }
    }
}
