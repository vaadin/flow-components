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
 * The layout of the legend items. Can be one of HORIZONTAL("horizontal") or
 * VERTICAL("vertical"). Defaults to HORIZONTAL.
 */
public enum LayoutDirection implements ChartEnum {

    VERTICAL("vertical"), HORIZONTAL("horizontal");

    LayoutDirection(String type) {
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return type;
    }
}
