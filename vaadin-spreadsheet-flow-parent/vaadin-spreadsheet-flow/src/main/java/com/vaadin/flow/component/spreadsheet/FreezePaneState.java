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
 * Freeze-pane split coordinates plus per-pane scroll positions.
 *
 * <p>
 * A frozen pane divides the sheet into up to four scroll regions (top-left,
 * top-right, bottom-left, bottom-right). The two split positions name where the
 * divider sits (row index for the horizontal split, column index for the
 * vertical split); the two scroll-position arrays remember the last-seen scroll
 * offset of each region so a sheet switch can restore them.
 *
 * <p>
 * Every setter mirrors the value into the corresponding element property; the
 * scroll-position arrays go through {@link Serializer#toJson} since they're
 * non-primitive payloads.
 */
final class FreezePaneState implements Serializable {

    private final Spreadsheet spreadsheet;

    private int verticalSplitPosition = 0;
    private int horizontalSplitPosition = 0;
    private int[] verticalScrollPositions;
    private int[] horizontalScrollPositions;

    FreezePaneState(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    int getVerticalSplitPosition() {
        return verticalSplitPosition;
    }

    void setVerticalSplitPosition(int verticalSplitPosition) {
        this.verticalSplitPosition = verticalSplitPosition;
        spreadsheet.getElement().setProperty("verticalSplitPosition",
                verticalSplitPosition);
    }

    int getHorizontalSplitPosition() {
        return horizontalSplitPosition;
    }

    void setHorizontalSplitPosition(int horizontalSplitPosition) {
        this.horizontalSplitPosition = horizontalSplitPosition;
        spreadsheet.getElement().setProperty("horizontalSplitPosition",
                horizontalSplitPosition);
    }

    int[] getVerticalScrollPositions() {
        return verticalScrollPositions;
    }

    void setVerticalScrollPositions(int[] verticalScrollPositions) {
        this.verticalScrollPositions = verticalScrollPositions;
        spreadsheet.getElement().setPropertyJson("verticalScrollPositions",
                Serializer.toJson(verticalScrollPositions));
    }

    int[] getHorizontalScrollPositions() {
        return horizontalScrollPositions;
    }

    void setHorizontalScrollPositions(int[] horizontalScrollPositions) {
        this.horizontalScrollPositions = horizontalScrollPositions;
        spreadsheet.getElement().setPropertyJson("horizontalScrollPositions",
                Serializer.toJson(horizontalScrollPositions));
    }
}
