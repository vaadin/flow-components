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
 * The position of the tick marks relative to the axis line. Can be one of
 * INSIDE and OUTSIDE. Defaults to OUTSIDE.
 */
public enum TickPosition implements ChartEnum {
    INSIDE("inside"), OUTSIDE("outside");

    private final String tickPosition;

    private TickPosition(String tickPosition) {
        this.tickPosition = tickPosition;
    }

    @Override
    public String toString() {
        return tickPosition;
    }
}
