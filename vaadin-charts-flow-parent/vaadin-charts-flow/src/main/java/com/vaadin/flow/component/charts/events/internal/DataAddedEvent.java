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
