/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

/**
 * What lives as long as one sheet is shown, and only as long: made anew each
 * time a sheet is shown rather than cleared, so nothing of the last sheet is
 * left over, whatever is added here later. What the client has of it is taken
 * off when it closes.
 */
final class SheetScope implements Serializable {

    /** The rows and columns the client shows: none until it reports them. */
    final Viewport viewport;
    final SheetOverlayManager overlays;
    final CustomComponentLoader components;

    SheetScope(Spreadsheet spreadsheet) {
        viewport = new Viewport(new SheetView(spreadsheet));
        overlays = new SheetOverlayManager(spreadsheet);
        components = new CustomComponentLoader(spreadsheet);
    }

    /** The sheet is no longer shown: what the client has of it goes. */
    void close() {
        overlays.clear();
        components.unloadAll();
    }
}
