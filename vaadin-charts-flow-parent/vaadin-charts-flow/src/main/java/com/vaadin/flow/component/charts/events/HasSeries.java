package com.vaadin.flow.component.charts.events;

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
