/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.style;

import com.vaadin.flow.component.charts.model.ChartEnum;

/**
 * CSS position attribute, ABSOLUTE or RELATIVE
 */
public enum StylePosition implements ChartEnum {
    ABSOLUTE("absolute"), RELATIVE("relative");

    private String position;

    private StylePosition(String position) {
        this.position = position;
    }

    public String toString() {
        return position;
    }

}
