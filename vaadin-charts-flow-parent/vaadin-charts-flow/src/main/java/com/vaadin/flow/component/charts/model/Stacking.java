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
 * Whether to stack the values of each series on top of each other. Possible
 * values are null to disable, NORMAL to stack by value or PERCENT. Defaults to
 * null.
 */
public enum Stacking implements ChartEnum {

    NONE(""), NORMAL("normal"), PERCENT("percent");

    private Stacking(String type) {
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return type;
    }
}
