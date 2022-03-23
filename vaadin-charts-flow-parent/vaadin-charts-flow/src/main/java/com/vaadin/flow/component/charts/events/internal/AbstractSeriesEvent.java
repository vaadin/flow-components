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

import com.vaadin.flow.component.charts.model.Series;

import java.io.Serializable;

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
