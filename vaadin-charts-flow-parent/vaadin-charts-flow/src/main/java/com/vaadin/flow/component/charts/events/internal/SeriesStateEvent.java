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

/**
 * Listener class for Series enabling and disabling events.
 *
 * @since 2.0
 */
public class SeriesStateEvent extends AbstractSeriesEvent {

    private static final long serialVersionUID = 20141117;

    /** Series was enabled */
    private final boolean enabled;

    /**
     * Constructs the event with given series and its state information.
     *
     * @param series
     *            Series.
     * @param enabled
     *            Whether or not series is enabled.
     */
    public SeriesStateEvent(Series series, boolean enabled) {
        super(series);
        this.enabled = enabled;
    }

    /**
     * Returns whether or not series is enabled.
     *
     * @return <b>true</b> when given series is enabled, <b>false</b> otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

}
