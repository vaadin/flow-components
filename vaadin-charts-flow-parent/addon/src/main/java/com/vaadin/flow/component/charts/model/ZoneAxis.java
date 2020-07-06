package com.vaadin.flow.component.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

/**
 * Defines the Axis on which the zones are applied.
 *
 * Defaults to y.
 */
public enum ZoneAxis implements ChartEnum {

    X("x"),
    Y("y");

    private final String axis;

    private ZoneAxis(String axis) {
        this.axis = axis;
    }

    @Override
    public String toString() {
        return axis;
    }
}
