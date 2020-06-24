package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
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
 * Possible axis dimensions with their indexes in client-side
 */
public enum AxisDimension {
    X_AXIS(0), Y_AXIS(1), Z_AXIS(2), COLOR_AXIS(3);

    private final int index;

    private AxisDimension(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
