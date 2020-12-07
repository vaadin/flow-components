package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
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
