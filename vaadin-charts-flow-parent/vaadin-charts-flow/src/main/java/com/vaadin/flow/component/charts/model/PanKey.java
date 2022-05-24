package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

/**
 * Allows setting a key to switch between zooming and panning. The keys are
 * mapped directly to the key properties of the click event argument
 * (event.altKey, event.ctrlKey, event.metaKey and event.shiftKey).
 *
 * Defaults to undefined.
 */
public enum PanKey implements ChartEnum {

    ALT("alt"), CTRL("ctrl"), META("meta"), SHIFT("shift");

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
