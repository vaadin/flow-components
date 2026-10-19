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

import com.vaadin.flow.dom.Element;

/**
 * What the client is given of the workbook shown, as properties of the element:
 * each one declared here, by its name on the wire. It lives with the workbook's
 * scope. Showing a workbook sets every one of them, so unlike the sheet's model
 * it has nothing to set back.
 * <p>
 * Whether the workbook changed is not here: the client is told by a toggle that
 * flips from its last value, workbook after workbook, which the component
 * keeps.
 */
final class WorkbookClientModel implements Serializable {

    /** The names of the sheets shown as tabs: the hidden ones left out. */
    final ClientProperty<String[]> sheetNames;
    /** The tab of the sheet shown, counted from 1: the first until set. */
    final ClientProperty<Integer> sheetIndex;
    /**
     * The CSS of each cell style of the workbook, by POI cell style index. The
     * component's protected field of the same name is kept in step with it.
     */
    final ClientProperty<HashMap<Integer, String>> cellStyleToCSSStyle;
    /**
     * Whether the structure of the workbook is protected: no sheet can be
     * added, deleted, renamed or moved.
     */
    final ClientProperty<Boolean> workbookProtected;
    /**
     * Where each sheet shown as a tab was last scrolled to, in the order of the
     * tabs, for the client to scroll it back there.
     */
    final ClientProperty<int[]> verticalScrollPositions;
    final ClientProperty<int[]> horizontalScrollPositions;

    WorkbookClientModel(Element element) {
        sheetNames = ClientProperty.json(element, "sheetNames");
        sheetIndex = ClientProperty.integer(element, "sheetIndex", 1);
        cellStyleToCSSStyle = ClientProperty.json(element,
                "cellStyleToCSSStyle");
        workbookProtected = ClientProperty.bool(element, "workbookProtected",
                false);
        verticalScrollPositions = ClientProperty.json(element,
                "verticalScrollPositions");
        horizontalScrollPositions = ClientProperty.json(element,
                "horizontalScrollPositions");
    }
}
