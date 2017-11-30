package com.vaadin.addon.charts.events;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
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

import java.io.Serializable;

import com.vaadin.addon.charts.model.Series;

/**
 * Base class for series events.
 * 
 * @since 2.0
 *
 */
public abstract class AbstractSeriesEvent implements Serializable {
    private static final long serialVersionUID = 20141117;

    /** The affected series */
    private final Series series;

    /**
     * Constructs the event, storing the information about the series.
     * 
     * @param series
     *            Series the event deals with.
     */
    public AbstractSeriesEvent(Series series) {
        if (series == null) {
            throw new IllegalArgumentException("Series may not be null");
        } else {
            this.series = series;
        }
    }

    /**
     * Returns the affected series
     * 
     * @return The affected series.
     */
    public Series getSeries() {
        return series;
    }

}
