package com.vaadin.addon.charts.events;

import com.vaadin.addon.charts.model.Series;

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
