package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
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

import java.time.Instant;

import com.vaadin.flow.component.charts.util.Util;

/**
 * DataSeriesItem that can hold also x2 and partialFill values. Used in e.g.
 * xrange series.
 */
public class DataSeriesItemXrange extends DataSeriesItem {

    private Number x2;
    private Number partialFill;

    /**
     * Constructs an empty item
     */
    public DataSeriesItemXrange() {
        super();
    }

    /**
     * Constructs an item with X, X2 and Y
     * 
     * @param x
     * @param x2
     * @param y
     */
    public DataSeriesItemXrange(Number x, Number x2, Number y) {
        super(x, y);
        setX2(x2);
    }

    /**
     * Constructs an item with X, X2 and Y
     * 
     * @param x
     * @param x2
     * @param y
     */
    public DataSeriesItemXrange(Instant x, Instant x2, Number y) {
        super(x, y);
        setX2(x2);
    }

    /**
     * Constructs an item with X, X2, Y and partialFill
     * 
     * @param x
     * @param x2
     * @param y
     * @param partialFill
     */
    public DataSeriesItemXrange(Number x, Number x2, Number y,
            Number partialFill) {
        this(x, x2, y);
        setPartialFill(partialFill);
    }

    /**
     * Constructs an item with X, X2, Y and partialFill
     * 
     * @param x
     * @param x2
     * @param y
     * @param partialFill
     */
    public DataSeriesItemXrange(Instant x, Instant x2, Number y,
            Number partialFill) {
        this(x, x2, y);
        setPartialFill(partialFill);
    }

    /**
     * Returns the X2-value of the item.
     *
     * @see #setX2(Number)
     * @return The X2 value of this data item.
     */
    public Number getX2() {
        return x2;
    }

    /**
     * Sets the X2 value of this data item. Defaults to null.
     *
     * @param x
     *            X-value of the item.
     */
    public void setX2(Number x2) {
        this.x2 = x2;
        makeCustomized();
    }

    /**
     * Sets the given instant as the x2 value.
     *
     * @param instant
     *            Instant to set.
     */
    public void setX2(Instant instant) {
        setX2(Util.toHighchartsTS(instant));
    }

    /**
     * Returns the partialFill-value of the item.
     *
     * @see #setPartialFill(Number)
     * @return The partialFill value of this data item.
     */
    public Number getPartialFill() {
        return partialFill;
    }

    /**
     * Partial fill value for this point, typically used to visualize how much
     * of a task is performed.
     * 
     * @param partialFill
     *            Number value between 0 and 1
     */
    public void setPartialFill(Number partialFill) {
        this.partialFill = partialFill;
        makeCustomized();
    }

}
