/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * The text alignment for the label. While align determines where the texts
 * anchor point is placed within the plot band, textAlign determines how the
 * text is aligned against its anchor point. Defaults to the same as the align
 * option.
 *
 * Defaults to undefined.
 */
public enum TextAlign implements ChartEnum {

    LEFT("left"), CENTER("center"), RIGHT("right");

    private final String alignment;

    private TextAlign(String alignment) {
        this.alignment = alignment;
    }

    @Override
    public String toString() {
        return alignment;
    }
}
