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

import java.time.Instant;

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.util.Util;

/**
 * DataSeriesItem that can hold also x2 and partialFill amount and color. Used
 * in e.g. xrange series.
 * <p>
 * To change partial fill amount or color use {@link #getPartialFill()} to get
 * the configuration object.
 */
public class DataSeriesItemXrange extends DataSeriesItem {

    private Number x2;
    private ItemPartialFill partialFill;

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
     * Constructs an item with X, X2, Y and partialFillAmount.
     *
     * @param x
     * @param x2
     * @param y
     * @param partialFillAmount
     */
    public DataSeriesItemXrange(Number x, Number x2, Number y,
            Number partialFillAmount) {
        this(x, x2, y);
        setPartialFill(new ItemPartialFill(partialFillAmount));
    }

    /**
     * Constructs an item with X, X2, Y and partialFillAmount.
     *
     * @param x
     * @param x2
     * @param y
     * @param partialFillAmount
     */
    public DataSeriesItemXrange(Instant x, Instant x2, Number y,
            Number partialFillAmount) {
        this(x, x2, y);
        setPartialFill(new ItemPartialFill(partialFillAmount));
    }

    /**
     * Constructs an item with X, X2, Y, partialFillAmount and partialFillColor.
     *
     * @param x
     * @param x2
     * @param y
     * @param partialFillAmount
     * @param partialFillColor
     */
    public DataSeriesItemXrange(Number x, Number x2, Number y,
            Number partialFillAmount, Color partialFillColor) {
        this(x, x2, y);
        setPartialFill(
                new ItemPartialFill(partialFillAmount, partialFillColor));
    }

    /**
     * Constructs an item with X, X2, Y, partialFillAmount and partialFillColor.
     *
     * @param x
     * @param x2
     * @param y
     * @param partialFillAmount
     * @param partialFillColor
     */
    public DataSeriesItemXrange(Instant x, Instant x2, Number y,
            Number partialFillAmount, Color partialFillColor) {
        this(x, x2, y);
        setPartialFill(
                new ItemPartialFill(partialFillAmount, partialFillColor));
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
     * @see #setPartialFill(ItemPartialFill)
     */
    public ItemPartialFill getPartialFill() {
        if (partialFill == null) {
            partialFill = new ItemPartialFill();
            makeCustomized();
        }
        return partialFill;
    }

    /**
     * Partial fill configuration for series points, typically used to visualize
     * how much of a task is performed.
     */
    public void setPartialFill(ItemPartialFill partialFill) {
        this.partialFill = partialFill;
        makeCustomized();
    }

}
