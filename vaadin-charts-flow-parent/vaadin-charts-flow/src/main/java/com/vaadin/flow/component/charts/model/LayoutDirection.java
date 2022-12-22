/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * The layout of the legend items. Can be one of HORIZONTAL("horizontal") or
 * VERTICAL("vertical"). Defaults to HORIZONTAL.
 */
public enum LayoutDirection implements ChartEnum {

    VERTICAL("vertical"), HORIZONTAL("horizontal");

    LayoutDirection(String type) {
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return type;
    }
}
