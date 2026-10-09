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
import java.util.HashMap;

import com.vaadin.flow.component.spreadsheet.SheetOverlayWrapper.OverlayChangeListener;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;

/**
 * Orchestrates loading and refreshing the active sheet's overlays (images,
 * charts, embedded components placed on cells). Composes
 * {@link SheetOverlayStorage} (the wrappers), {@link SheetOverlayVisibility}
 * (is this overlay in the viewport?), and {@link SheetOverlayInfoBuilder} (turn
 * a wrapper into the client-side {@code OverlayInfo} payload).
 *
 * <p>
 * The {@code reloadFromPoi} flag is set externally (after row/column resizing
 * on POI) to force a full reload on the next call to {@link #loadOrUpdate}.
 */
final class SheetOverlayManager implements Serializable {

    private final Spreadsheet spreadsheet;
    private final SheetOverlayStorage storage;
    private final SheetOverlayVisibility visibility;
    private final SheetOverlayInfoBuilder infoBuilder;
    private boolean reloadFromPoi;

    SheetOverlayManager(Spreadsheet spreadsheet, SheetOverlayStorage storage,
            SheetOverlayVisibility visibility,
            SheetOverlayInfoBuilder infoBuilder) {
        this.spreadsheet = spreadsheet;
        this.storage = storage;
        this.visibility = visibility;
        this.infoBuilder = infoBuilder;
    }

    boolean hasOverlays() {
        return !storage.isEmpty();
    }

    void markReloadFromPoi() {
        this.reloadFromPoi = true;
    }

    void add(SheetOverlayWrapper overlay) {
        storage.add(overlay);
    }

    void loadOrUpdate() {
        // Fixes the issue of overlays being lost when creating or removing
        // frozen rows/columns. More like a kludge, a real solution is yet
        // to be found.
        if (!hasOverlays()) {
            SpreadsheetFactory.loadSheetOverlays(spreadsheet);
        }

        if (!hasOverlays()) {
            return;
        }

        if (reloadFromPoi) {
            // reload images from POI because row / column sizes have
            // changed; currently doesn't affect anything because POI
            // doesn't update the image anchor data after resizing.
            clear();
            SpreadsheetFactory.loadSheetOverlays(spreadsheet);
            reloadFromPoi = false;
        }

        for (final SheetOverlayWrapper overlay : storage.all()) {
            if (visibility.isVisible(overlay)) {
                addOverlayData(overlay);
                overlay.setVisible(true);
            } else if (overlay.isVisible()) {
                // was visible but went out of visibility
                removeOverlayData(overlay);
                overlay.setVisible(false);
            }
        }
    }

    void clear() {
        for (SheetOverlayWrapper image : storage.all()) {
            removeOverlayData(image);
        }
        storage.clearOverlays();
    }

    private void addOverlayData(SheetOverlayWrapper overlay) {
        if (overlay.getComponent(true) != null) {
            spreadsheet.registerCustomComponent(overlay.getComponent(true));
        }

        if (overlay.getId() != null && overlay.getResourceHandler() != null
                && !spreadsheet.getResources().containsKey(overlay.getId())) {
            spreadsheet.setResource(overlay.getId(),
                    overlay.getResourceHandler());
        }

        if (overlay.getId() != null) {
            HashMap<String, OverlayInfo> updated = spreadsheet
                    .getOverlays() != null
                            ? new HashMap<>(spreadsheet.getOverlays())
                            : new HashMap<>();
            updated.put(overlay.getId(), infoBuilder.build(overlay));
            spreadsheet.setOverlays(updated);

            overlay.setOverlayChangeListener(new OverlayChangeListener() {
                @Override
                public void overlayChanged() {
                    loadOrUpdate();
                }
            });
        }
    }

    private void removeOverlayData(SheetOverlayWrapper overlay) {
        if (overlay.getId() != null) {
            if (spreadsheet.getOverlays() != null) {
                HashMap<String, OverlayInfo> updated = spreadsheet
                        .getOverlays();
                updated.remove(overlay.getId());
                spreadsheet.setOverlays(updated);
            }
            spreadsheet.setResource(overlay.getId(), null);
        }

        if (overlay.getComponent(false) != null) {
            spreadsheet.unRegisterCustomComponent(overlay.getComponent(false));
        }
    }
}
