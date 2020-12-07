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
 * The name of a symbol to use for the border in {@link ChartType#FLAGS} series.
 */
public enum FlagShape implements ChartEnum {

    CALLOUT("callout"), FLAG("flag"), CIRCLEPIN("circlepin"), SQUAREPIN(
            "squarepin");

    FlagShape(String type) {
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return type;
    }
}
