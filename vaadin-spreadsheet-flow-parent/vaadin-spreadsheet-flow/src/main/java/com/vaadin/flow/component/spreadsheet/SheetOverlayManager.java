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
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;

/**
 * The overlays of the active sheet (pictures, charts and other components
 * placed on cells), and giving the client those shown: where each starts and
 * how big it is, its image and its component.
 * <p>
 * The overlays are read from POI when there are none. POI moves none of them
 * with the rows and columns under them, which are shifted, deleted or resized,
 * so they are only placed again then, under the keys the client has them by.
 */
final class SheetOverlayManager implements Serializable {

    private final Spreadsheet spreadsheet;
    private final Set<SheetOverlayWrapper> overlays = new HashSet<>();

    SheetOverlayManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    boolean hasOverlays() {
        return !overlays.isEmpty();
    }

    void add(SheetOverlayWrapper overlay) {
        overlays.add(overlay);
    }

    void loadOrUpdate() {
        // Fixes the issue of overlays being lost when creating or removing
        // frozen rows/columns. More like a kludge, a real solution is yet
        // to be found.
        if (!hasOverlays()) {
            SpreadsheetFactory.loadSheetOverlays(spreadsheet);
        }

        HashMap<String, OverlayInfo> shown = new HashMap<>();
        for (final SheetOverlayWrapper overlay : overlays) {
            boolean visible = isVisible(overlay);
            if (visible) {
                addOverlayData(overlay, shown);
            } else {
                removeOverlayData(overlay);
            }
            overlay.setVisible(visible);
        }
        spreadsheet.setOverlays(shown);
    }

    /**
     * Takes the images and components of the overlays off the client, and
     * forgets the overlays. Whoever clears them gives the client the overlays
     * left right after.
     */
    void clear() {
        for (SheetOverlayWrapper overlay : overlays) {
            removeOverlayData(overlay);
        }
        overlays.clear();
    }

    private void addOverlayData(SheetOverlayWrapper overlay,
            HashMap<String, OverlayInfo> shown) {
        if (overlay.getComponent(true) != null) {
            spreadsheet.registerCustomComponent(overlay.getComponent(true));
        }

        if (overlay.getId() != null && overlay.getResourceHandler() != null
                && !spreadsheet.getResources().containsKey(overlay.getId())) {
            spreadsheet.setResource(overlay.getId(),
                    overlay.getResourceHandler());
        }

        if (overlay.getId() != null) {
            shown.put(overlay.getId(), buildInfo(overlay));
            overlay.setOverlayChangeListener(this::loadOrUpdate);
        }
    }

    private void removeOverlayData(SheetOverlayWrapper overlay) {
        if (overlay.getId() != null) {
            spreadsheet.setResource(overlay.getId(), null);
        }

        if (overlay.getComponent(false) != null) {
            spreadsheet.unRegisterCustomComponent(overlay.getComponent(false));
        }
    }

    /** Whether a pane shown shows the overlay. */
    private boolean isVisible(SheetOverlayWrapper overlay) {
        var anchor = overlay.getAnchor();

        // Need special handling for XSSFClientAnchor anchors of type
        // DONT_MOVE_AND_RESIZE.
        // See https://github.com/vaadin/flow-components/issues/3261
        if (isFixedXssf(anchor)) {
            // Since there's no way to know if an arbitrary x/y coordinate is
            // inside the current viewport, always return true for these
            // anchors.
            return true;
        }

        int col1 = anchor.getCol1();
        int col2 = anchor.getCol2();
        int row1 = anchor.getRow1();
        int row2 = anchor.getRow2();

        // type=2 doesn't size with cells
        final boolean isType2 = (col2 == 0 && row2 == 0);

        if (!isType2) {
            // to ensure compatibility with grouping/hidden columns
            if (isColumnRangeHidden(col1, col2)
                    || isRowRangeHidden(row1, row2)) {
                return false;
            }
        }

        boolean[] visible = { false };
        // The overlay numbers rows and columns from 1.
        spreadsheet.viewport()
                .forEachPane((r1, c1, r2, c2) -> visible[0] |= overlay
                        .isVisible(r1 + 1, c1 + 1, r2 + 1, c2 + 1));
        return visible[0];
    }

    private static boolean isFixedXssf(ClientAnchor anchor) {
        return AnchorType.DONT_MOVE_AND_RESIZE.equals(anchor.getAnchorType())
                && anchor instanceof XSSFClientAnchor;
    }

    private boolean isRowRangeHidden(int row1, int row2) {
        for (int row = row1; row <= row2; row++) {
            if (!spreadsheet.isRowHidden(row)) {
                return false;
            }
        }
        return true;
    }

    private boolean isColumnRangeHidden(int col1, int col2) {
        for (int col = col1; col <= col2; col++) {
            if (!spreadsheet.isColumnHidden(col)) {
                return false;
            }
        }
        return true;
    }

    /** Where the overlay starts and how big it is, as the client has it. */
    private OverlayInfo buildInfo(SheetOverlayWrapper overlayWrapper) {
        OverlayInfo info = new OverlayInfo(overlayWrapper.getType());
        Sheet sheet = spreadsheet.getActiveSheet();
        var anchor = overlayWrapper.getAnchor();

        // Need special handling for XSSFClientAnchor anchors of type
        // DONT_MOVE_AND_RESIZE.
        // See https://github.com/vaadin/flow-components/issues/3261
        if (isFixedXssf(anchor)) {
            info.col = 1;
            info.row = 1;
            var xssfAnchor = (XSSFClientAnchor) anchor;
            info.dx = (Long) xssfAnchor.getPosition().getX()
                    / Units.EMU_PER_PIXEL;
            info.dy = (Long) xssfAnchor.getPosition().getY()
                    / Units.EMU_PER_POINT;
            info.width = xssfAnchor.getSize().getCx() / Units.EMU_PER_PIXEL;
            info.height = xssfAnchor.getSize().getCy() / Units.EMU_PER_POINT;
            return info;
        }

        int col = anchor.getCol1();
        while (spreadsheet.isColumnHidden(col)) {
            col++;
        }
        int row = anchor.getRow1();
        while (spreadsheet.isRowHidden(row)) {
            row++;
        }

        info.col = col + 1; // 1-based
        info.row = row + 1; // 1-based

        info.height = overlayWrapper.getHeight(sheet, spreadsheet.getRowH());
        info.width = overlayWrapper.getWidth(sheet, spreadsheet.getColW(),
                spreadsheet.getDefColW());

        // FIXME: height and width can be -1, it is never handled anywhere

        // if original start row/column is hidden, use 0 dy/dx
        if (col == anchor.getCol1()) {
            info.dx = overlayWrapper.getDx1(sheet);
        }
        if (row == anchor.getRow1()) {
            info.dy = overlayWrapper.getDy1(sheet);
        }

        return info;
    }
}
