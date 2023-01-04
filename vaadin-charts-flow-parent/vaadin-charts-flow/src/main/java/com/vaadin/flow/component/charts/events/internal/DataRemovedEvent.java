/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.events.internal;

import com.vaadin.flow.component.charts.model.Series;

/**
 * Event when the data was removed.
 *
 * @since 2.0
 *
 */
public class DataRemovedEvent extends AbstractSeriesEvent {

    private static final long serialVersionUID = 20141117;

    private final int index;

    /**
     * Constructs the event with given series and index of the removed data.
     *
     * @param series
     *            Series.
     * @param index
     *            Index.
     */
    public DataRemovedEvent(Series series, int index) {
        super(series);
        this.index = index;
    }

    /**
     * Returns index of the removed data point.
     *
     * @return index of the removed data point
     */
    public int getIndex() {
        return index;
    }

}
