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
