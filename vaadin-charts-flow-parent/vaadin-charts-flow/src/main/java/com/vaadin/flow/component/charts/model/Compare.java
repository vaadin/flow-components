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
 * The {@link YAxis} will show percentage or absolute change depending on
 * whether compare is set to {@link Compare#PERCENT} or {@link Compare#VALUE}
 */
public enum Compare implements ChartEnum {
    PERCENT("percent"), VALUE("value");

    private String compare;

    private Compare(String compare) {
        this.compare = compare;
    }

    @Override
    public String toString() {
        return compare;
    }

}
