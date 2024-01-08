/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.style;

import com.vaadin.flow.component.charts.model.ChartEnum;

/**
 * Font weight used by Style class
 */
public enum FontWeight implements ChartEnum {

    /**
     * Normal text
     */
    NORMAL("normal"),

    /**
     * Bold text
     */
    BOLD("bold");

    private String type;

    private FontWeight(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

}
