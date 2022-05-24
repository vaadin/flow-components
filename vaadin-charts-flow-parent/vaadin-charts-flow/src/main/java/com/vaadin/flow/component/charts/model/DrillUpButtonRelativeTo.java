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
 * What box to align the button to.
 */
public enum DrillUpButtonRelativeTo implements ChartEnum {

    PLOTBOX("plotBox"), SPACINGBOX("spacingBox");

    DrillUpButtonRelativeTo(String box) {
        this.box = box;
    }

    private String box;

    @Override
    public String toString() {
        return box;
    }
}
