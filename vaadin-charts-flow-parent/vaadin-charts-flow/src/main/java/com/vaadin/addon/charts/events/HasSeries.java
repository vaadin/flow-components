package com.vaadin.addon.charts.events;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Series;

/**
 * Indicates that an event has an associated series
 */
public interface HasSeries {

    Chart getSource();

    /**
     * Returns the index of the series
     *
     * @return
     */
    int getSeriesItemIndex();

    /**
     * Returns the series
     *
     * @return
     */
    default Series getSeries() {
        return getSource().getConfiguration().getSeries().get(getSeriesItemIndex());
    }
}
