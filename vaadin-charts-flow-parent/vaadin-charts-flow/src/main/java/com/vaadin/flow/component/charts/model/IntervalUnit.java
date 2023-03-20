/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
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
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

/**
 * Irregular time unit used to define point interval unit.
 */
public enum IntervalUnit implements ChartEnum {

    DAY("day"), MONTH("month"), YEAR("year");

    private IntervalUnit(String unit) {
        this.unit = unit;
    }

    private String unit;

    @Override
    public String toString() {
        return unit;
    }
}
