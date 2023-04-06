/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

import java.io.Serializable;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Series;

/**
 * Indicates that an event has an associated series
 */
public interface HasSeries extends Serializable {

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
        return getSource().getConfiguration().getSeries()
                .get(getSeriesItemIndex());
    }
}
