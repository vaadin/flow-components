package com.vaadin.flow.component.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

/**
 * Allows setting a key to switch between zooming and panning. The keys are mapped directly to the key properties
 * of the click event argument (event.altKey, event.ctrlKey, event.metaKey and event.shiftKey).
 *
 * Defaults to undefined.
 */
public enum PanKey implements ChartEnum {

    ALT("alt"),
    CTRL("ctrl"),
    META("meta"),
    SHIFT("shift");

    private final String key;

    /**
     * Constructs a new PanKey.
     * 
     * @param key
     *            the actual key string passed for client side
     */
    private PanKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
