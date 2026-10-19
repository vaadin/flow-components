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
 * The scroll position of each pane of a frozen sheet, which a sheet switch
 * restores.
 *
 * <p>
 * A frozen pane divides the sheet into up to four scroll regions (top-left,
 * top-right, bottom-left, bottom-right); the two arrays remember the last-seen
 * scroll offset of each region. Where the panes split is the sheet's own:
 * {@link SheetClientModel} holds it.
 *
 * <p>
 * Every setter gives the element the value at once, as JSON.
 */
final class FreezePaneState implements Serializable {

    private final ClientProperty<int[]> verticalScrollPositions;
    private final ClientProperty<int[]> horizontalScrollPositions;

    FreezePaneState(Element element) {
        verticalScrollPositions = ClientProperty.json(element,
                "verticalScrollPositions");
        horizontalScrollPositions = ClientProperty.json(element,
                "horizontalScrollPositions");
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
