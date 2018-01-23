package com.vaadin.addon.charts.model;

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
