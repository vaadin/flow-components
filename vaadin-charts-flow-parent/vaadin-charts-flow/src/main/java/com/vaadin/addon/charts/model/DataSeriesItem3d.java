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
 * DataSeriesItem that can hold also Z value. Used in e.g. bubble charts.
 */
public class DataSeriesItem3d extends DataSeriesItem {

    private Number z;

    public DataSeriesItem3d() {
        super();
    }

    /**
     * Constructs an item with X, Y and Z values
     * 
     * @param x
     * @param y
     * @param z
     */
    public DataSeriesItem3d(Number x, Number y, Number z) {
        super(x, y);
        setZ(z);
    }

    /**
     * Sets the z value of the point.
     * 
     * @param z
     */
    public void setZ(Number z) {
        this.z = z;
        makeCustomized();
    }

    /**
     * @return the z value
     */
    public Number getZ() {
        return z;
    }

}
