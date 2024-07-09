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
