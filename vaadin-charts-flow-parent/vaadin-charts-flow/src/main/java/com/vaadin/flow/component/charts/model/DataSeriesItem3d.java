package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
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
