/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.events.internal;

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
