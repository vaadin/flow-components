package com.vaadin.flow.component.charts.events.internal;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2018 Vaadin Ltd
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

import com.vaadin.flow.component.charts.model.Series;

/**
 * Event for information about a new series to be added
 * 
 * @since 4.0
 *
 */
public class SeriesAddedEvent extends AbstractSeriesEvent {

    /**
     * Constructs the event.
     *
     * @param series
     *            The series that is to be added
     */
    public SeriesAddedEvent(Series series) {
        super(series);
    }

}
