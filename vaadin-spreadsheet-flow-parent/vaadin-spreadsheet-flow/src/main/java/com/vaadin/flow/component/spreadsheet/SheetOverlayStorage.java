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
import java.util.Collection;
import java.util.HashSet;

/**
 * Holds the sheet's overlay wrappers. Pure storage — no POI access, no DOM
 * attachment, no visibility logic.
 *
 * <p>
 * The {@code overlays} field is reset to an empty set in {@link #initialize};
 * before that call {@link #all} returns {@code null}, matching the
 * Spreadsheet's pre-workbook-load state.
 */
final class SheetOverlayStorage implements Serializable {

    private HashSet<SheetOverlayWrapper> overlays;

    void initialize() {
        overlays = new HashSet<>();
    }

    boolean isEmpty() {
        return overlays == null || overlays.isEmpty();
    }

    void add(SheetOverlayWrapper overlay) {
        overlays.add(overlay);
    }

    Collection<SheetOverlayWrapper> all() {
        return overlays;
    }

    void clearOverlays() {
        overlays.clear();
    }
}
