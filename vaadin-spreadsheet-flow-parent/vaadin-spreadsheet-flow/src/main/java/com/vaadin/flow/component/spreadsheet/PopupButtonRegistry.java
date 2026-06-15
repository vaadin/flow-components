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
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;

/**
 * Owns the by-cell map of pop-up buttons for the currently active sheet. Pure
 * storage — no DOM attachment, no JS bridging, no event firing.
 *
 * <p>
 * Cell references stored here are <strong>absolute</strong> (sheet-qualified)
 * so they survive sheet-name changes; callers are responsible for converting
 * relative references via {@code SpreadsheetUtil.relativeToAbsolute} before
 * calling.
 */
@SuppressWarnings("serial")
final class PopupButtonRegistry implements Serializable {

    private Map<CellReference, PopupButton> buttonsByCell = new HashMap<>();

    PopupButton get(CellReference absoluteCellReference) {
        return buttonsByCell.get(absoluteCellReference);
    }

    void put(CellReference absoluteCellReference, PopupButton button) {
        buttonsByCell.put(absoluteCellReference, button);
    }

    PopupButton remove(CellReference absoluteCellReference) {
        return buttonsByCell.remove(absoluteCellReference);
    }

    Collection<PopupButton> values() {
        return buttonsByCell.values();
    }

    boolean isEmpty() {
        return buttonsByCell.isEmpty();
    }

    void clear() {
        buttonsByCell.clear();
    }

    /**
     * Atomically replaces the underlying map. Used by
     * {@link PopupButtonRowShifter} when row indices are remapped en masse.
     */
    void replaceAll(Map<CellReference, PopupButton> updated) {
        this.buttonsByCell = updated;
    }
}
