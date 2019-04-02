package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
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
 * The text alignment for the label. While align determines where the texts anchor point is placed within the plot band,
 * textAlign determines how the text is aligned against its anchor point.
 * Defaults to the same as the align option.
 *
 * Defaults to undefined.
 */
public enum TextAlign implements ChartEnum {

    LEFT("left"),
    CENTER("center"),
    RIGHT("right");

    private final String alignment;

    private TextAlign(String alignment) {
        this.alignment = alignment;
    }

    @Override
    public String toString() {
        return alignment;
    }
}
