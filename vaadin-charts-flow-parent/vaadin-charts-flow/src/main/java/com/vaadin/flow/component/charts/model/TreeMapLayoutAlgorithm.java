package com.vaadin.flow.component.charts.model;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

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
