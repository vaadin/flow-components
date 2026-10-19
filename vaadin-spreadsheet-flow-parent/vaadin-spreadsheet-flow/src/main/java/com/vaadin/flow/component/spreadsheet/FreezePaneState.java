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

import com.vaadin.flow.dom.Element;

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
 * Every setter gives the element the value at once, the scroll-position arrays
 * as JSON.
 */
final class FreezePaneState implements Serializable {

    private final ClientProperty<Integer> verticalSplitPosition;
    private final ClientProperty<Integer> horizontalSplitPosition;
    private final ClientProperty<int[]> verticalScrollPositions;
    private final ClientProperty<int[]> horizontalScrollPositions;

    FreezePaneState(Element element) {
        verticalSplitPosition = ClientProperty.integer(element,
                "verticalSplitPosition", 0);
        horizontalSplitPosition = ClientProperty.integer(element,
                "horizontalSplitPosition", 0);
        verticalScrollPositions = ClientProperty.json(element,
                "verticalScrollPositions");
        horizontalScrollPositions = ClientProperty.json(element,
                "horizontalScrollPositions");
    }

    int getVerticalSplitPosition() {
        return verticalSplitPosition.get();
    }

    void setVerticalSplitPosition(int verticalSplitPosition) {
        this.verticalSplitPosition.set(verticalSplitPosition);
    }

    int getHorizontalSplitPosition() {
        return horizontalSplitPosition.get();
    }

    void setHorizontalSplitPosition(int horizontalSplitPosition) {
        this.horizontalSplitPosition.set(horizontalSplitPosition);
    }

    int[] getVerticalScrollPositions() {
        return verticalScrollPositions.get();
    }

    void setVerticalScrollPositions(int[] verticalScrollPositions) {
        this.verticalScrollPositions.set(verticalScrollPositions);
    }

    int[] getHorizontalScrollPositions() {
        return horizontalScrollPositions.get();
    }

    void setHorizontalScrollPositions(int[] horizontalScrollPositions) {
        this.horizontalScrollPositions.set(horizontalScrollPositions);
    }
}
