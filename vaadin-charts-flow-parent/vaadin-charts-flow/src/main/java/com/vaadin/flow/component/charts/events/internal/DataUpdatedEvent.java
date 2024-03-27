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
 * Event for updating existing data series.
 *
 * @since 2.0
 *
 */
public class DataUpdatedEvent extends AbstractSeriesItemEvent {

    private static final long serialVersionUID = 20141117;

    private final int pointIndex;

    /**
     * Constructs the event with given series, number and point index.
     *
     * @param series
     *            Series.
     * @param value
     *            Value.
     * @param pointIndex
     *            Point index.
     */
    public DataUpdatedEvent(Series series, Number value, int pointIndex) {
        super(series, value);
        this.pointIndex = pointIndex;
    }

    /**
     * Constructs the event with given series, item and point index.
     *
     * @param series
     *            Series.
     * @param item
     *            Series item.
     * @param pointIndex
     *            Point index.
     */
    public DataUpdatedEvent(Series series, DataSeriesItem item,
            int pointIndex) {
        super(series, item);
        this.pointIndex = pointIndex;
    }

    /**
     * Returns the point index.
     *
     * @return Point index.
     */
    public int getPointIndex() {
        return pointIndex;
    }
}
