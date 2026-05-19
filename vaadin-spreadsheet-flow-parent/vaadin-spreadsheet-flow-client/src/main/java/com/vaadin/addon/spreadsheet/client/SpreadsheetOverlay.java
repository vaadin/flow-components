/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.vaadin.client.ui.VContextMenu;
import com.vaadin.client.ui.VOverlay;

/**
 * A VOverlay implementation that attaches the overlay to the per-instance
 * container created by the {@code <vaadin-spreadsheet>} custom element. The
 * container lives inside the spreadsheet element (light DOM), so the overlay
 * inherits {@code pointer-events: auto} from a containing modal Dialog overlay
 * — fixing the regression where context menus were visually shown (via native
 * popover) but not clickable.
 */
@SuppressWarnings({ "deprecation", "java:S1699" })
public class SpreadsheetOverlay extends VOverlay {

    private static final String POPOVER_ATTRIBUTE = "popover";

    private Element overlayContainer;

    /**
     * A VContextMenu implementation that attaches the overlay to the
     * per-instance container.
     */
    public static class SpreadsheetContextMenu extends VContextMenu {

        private Element overlayContainer;

        public SpreadsheetContextMenu(Element overlayContainer) {
            this.overlayContainer = overlayContainer;
            DOM.setElementProperty(getElement(), "id", "PID_VAADIN_CM");
            setPopover(getElement());
        }

        @Override
        public com.google.gwt.user.client.Element getOverlayContainer() {
            return asUserElement(overlayContainer);
        }

        public void setOverlayContainer(Element overlayContainer) {
            this.overlayContainer = overlayContainer;
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

    public void setOverlayContainer(Element overlayContainer) {
        this.overlayContainer = overlayContainer;
    }

    @Override
    public com.google.gwt.user.client.Element getOverlayContainer() {
        return asUserElement(overlayContainer);
    }

    @Override
    public void show() {
        super.show();
        showPopover(getElement());
    }

    private static void setPopover(Element el) {
        if (el != null) {
            el.setAttribute(POPOVER_ATTRIBUTE, "manual");
        }
    }

    private static com.google.gwt.user.client.Element asUserElement(
            Element el) {
        return el == null ? null
                : el.<com.google.gwt.user.client.Element> cast();
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
