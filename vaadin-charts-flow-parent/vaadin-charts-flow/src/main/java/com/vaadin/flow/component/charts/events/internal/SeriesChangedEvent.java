/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.events.internal;

import com.vaadin.flow.component.charts.model.Series;

/**
 * Event for information about changes in data of series
 *
 * @since 4.0
 *
 */
public class SeriesChangedEvent extends AbstractSeriesEvent {

    /**
     * Constructs the event.
     *
     * @param series
     *            The series that has changed
     */
    public SeriesChangedEvent(Series series) {
        super(series);
    }

}
