/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

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
