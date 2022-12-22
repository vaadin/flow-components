/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.style;

import com.vaadin.flow.component.charts.model.ChartEnum;

public enum TickIntervalStyle implements ChartEnum {

    AUTO("auto"), NONE("");

    private TickIntervalStyle(String type) {
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return type;
    }
}
