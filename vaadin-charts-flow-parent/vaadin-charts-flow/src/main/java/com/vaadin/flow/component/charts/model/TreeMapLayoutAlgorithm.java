/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * The layout algorithm used by {@link ChartType#TREEMAP} charts.
 */
public enum TreeMapLayoutAlgorithm implements ChartEnum {

    SLICEANDDICE("sliceAndDice"), STRIPES("stripes"), SQUARIFIED(
            "squarified"), STRIP("strip");

    private String type;

    TreeMapLayoutAlgorithm(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
