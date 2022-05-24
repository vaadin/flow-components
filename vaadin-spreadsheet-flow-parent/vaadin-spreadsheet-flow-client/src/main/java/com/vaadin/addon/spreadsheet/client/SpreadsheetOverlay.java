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
@SuppressWarnings("deprecation")
public class SpreadsheetOverlay extends VOverlay {

    /**
     * A VContextMenu Implementation that attaches the overlay to the container
     * added by vaadin-spreadsheet webcomponent
     */
    public static class SpreadsheetContextMenu extends VContextMenu {
        public SpreadsheetContextMenu() {
            DOM.setElementProperty(getElement(), "id", "PID_VAADIN_CM");
        }

        @Override
        public Element getOverlayContainer() {
            return getOverlayContainerElement();
        }
    }

    public SpreadsheetOverlay() {
        super();
    }

    public SpreadsheetOverlay(boolean autoHide, boolean modal) {
        super(autoHide, modal);
    }

    public SpreadsheetOverlay(boolean autoHide) {
        super(autoHide);
    }

    @Override
    public Element getOverlayContainer() {
        return getOverlayContainerElement();
    }

    private static Element getOverlayContainerElement() {
        Element overlays = DOM.getElementById("spreadsheet-overlays");
        return overlays == null ? RootPanel.getBodyElement() : overlays;
    }
}
