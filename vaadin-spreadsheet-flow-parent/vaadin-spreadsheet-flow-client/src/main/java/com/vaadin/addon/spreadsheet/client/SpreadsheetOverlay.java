/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.client.ui.VContextMenu;
import com.vaadin.client.ui.VOverlay;

/**
 * A VOverlay Implementation that attaches the overlay to the container added by
 * vaadin-spreadsheet webcomponent
 */
@SuppressWarnings({ "deprecation", "java:S1699" })
public class SpreadsheetOverlay extends VOverlay {

    private static final String POPOVER_ATTRIBUTE = "popover";

    /**
     * A VContextMenu Implementation that attaches the overlay to the container
     * added by vaadin-spreadsheet webcomponent
     */
    public static class SpreadsheetContextMenu extends VContextMenu {

        public SpreadsheetContextMenu() {
            DOM.setElementProperty(getElement(), "id", "PID_VAADIN_CM");
            setPopover(getElement());
        }

        @Override
        public Element getOverlayContainer() {
            return getOverlayContainerElement();
        }

        @Override
        public void show() {
            super.show();
            showPopover(getElement());
        }

        @Override
        public void showAt(int x, int y) {
            super.showAt(x, y);
            showPopover(getElement());
        }
    }

    public SpreadsheetOverlay() {
        super();
        setPopover(getElement());
    }

    public SpreadsheetOverlay(boolean autoHide, boolean modal) {
        super(autoHide, modal);
        setPopover(getElement());
    }

    public SpreadsheetOverlay(boolean autoHide) {
        super(autoHide);
        setPopover(getElement());
    }

    private static void setPopover(Element el) {
        if (el != null) {
            el.setAttribute(POPOVER_ATTRIBUTE, "manual");
        }
    }

    @Override
    public void show() {
        super.show();
        showPopover(getElement());
    }

    @Override
    public Element getOverlayContainer() {
        return getOverlayContainerElement();
    }

    private static Element getOverlayContainerElement() {
        Element overlays = DOM.getElementById("spreadsheet-overlays");
        return overlays == null ? RootPanel.getBodyElement() : overlays;
    }

    // @formatter:off
    private static native void showPopover(Element el) 
    /*-{
        var fn = el && el.showPopover;
        if (typeof fn === "function") {
            fn.call(el);
        }
    }-*/;
    // @formatter:on
}
