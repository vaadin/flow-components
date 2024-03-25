/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.events.internal;

import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Series;

/**
 * Event triggered when data was added to the series.
 *
 * @since 2.0
 *
 */
public class DataAddedEvent extends AbstractSeriesItemEvent {

    private static final long serialVersionUID = 20141117;

    /** true if the data addition was a shift and first item was removed */
    private final boolean shift;

    /**
     * Constructs the event with given series and number.
     *
     * @param series
     *            Data series.
     * @param value
     *            A value.
     */
    public DataAddedEvent(Series series, Number value) {
        super(series, value);
        shift = false;
    }

    /**
     * Constructs the event with given series, item and a shift information.
     *
     * @param series
     *            Series.
     * @param item
     *            Series item.
     * @param shift
     *            true if the data addition was a shift and first item was
     *            removed
     */
    public DataAddedEvent(Series series, DataSeriesItem item, boolean shift) {
        super(series, item);
        this.shift = shift;
    }

    /**
     * Whether or not the data addition was a shift and first item was removed
     *
     * @return true if the data addition was a shift and first item was removed
     */
    public boolean isShift() {
        return shift;
    }
}
