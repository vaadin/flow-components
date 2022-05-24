package com.vaadin.flow.component.charts.events.internal;

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

import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Series;

/**
 * Base class for series item events. Contains the information about series and
 * exactly one of item or value.
 *
 * @since 2.0
 *
 */
public abstract class AbstractSeriesItemEvent extends AbstractSeriesEvent {
    private static final long serialVersionUID = 20141117;

    /** The item added. May be null if value != null */
    private final DataSeriesItem item;

    /** The value added. May be null if item != null */
    private final Number value;

    /**
     * Constructs the event with a value (without the series item).
     *
     * @param series
     *            Series.
     * @param value
     *            Value.
     */
    public AbstractSeriesItemEvent(Series series, Number value) {
        super(series);
        if (value == null) {
            throw new IllegalArgumentException("Value may not be null");
        } else {
            this.value = value;
            item = null;
        }
    }

    /**
     * Constructs the event with a series item (without the value).
     *
     * @param series
     *            Series.
     * @param item
     *            Series item..
     */
    public AbstractSeriesItemEvent(Series series, DataSeriesItem item) {
        super(series);
        if (item == null) {
            throw new IllegalArgumentException("Item may not be null");
        } else {
            this.item = item;
            value = null;
        }
    }

    /**
     * The item added. May be null if value != null.
     *
     * @return The item added.
     */
    public DataSeriesItem getItem() {
        return item;
    }

    /**
     * The value added. May be null if item != null.
     *
     * @return The value added.
     */
    public Number getValue() {
        return value;
    }

}
