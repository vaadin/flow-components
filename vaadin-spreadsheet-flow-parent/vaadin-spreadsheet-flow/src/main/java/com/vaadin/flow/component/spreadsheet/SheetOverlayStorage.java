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
import java.util.Set;

import com.vaadin.flow.component.Component;

/**
 * Holds the sheet's overlay wrappers and the Components currently attached
 * because of them. Pure storage — no POI access, no DOM attachment, no
 * visibility logic.
 *
 * <p>
 * The {@code overlays} field is reset to an empty set in {@link #initialize};
 * before that call {@link #isInitialized} returns {@code false}, matching the
 * Spreadsheet field's pre-workbook-load state.
 */
@SuppressWarnings("serial")
final class SheetOverlayStorage implements Serializable {

    private HashSet<SheetOverlayWrapper> overlays;
    private final Set<Component> overlayComponents = new HashSet<>();

    void initialize() {
        overlays = new HashSet<>();
    }

    boolean isInitialized() {
        return overlays != null;
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

    void addComponent(Component component) {
        overlayComponents.add(component);
    }

    void removeComponent(Component component) {
        overlayComponents.remove(component);
    }
}
