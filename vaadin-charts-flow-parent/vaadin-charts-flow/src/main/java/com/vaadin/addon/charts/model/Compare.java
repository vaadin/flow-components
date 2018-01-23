package com.vaadin.addon.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2012 - 2016 Vaadin Ltd
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
