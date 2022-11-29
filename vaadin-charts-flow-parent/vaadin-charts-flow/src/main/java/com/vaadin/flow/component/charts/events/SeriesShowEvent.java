package com.vaadin.flow.component.charts.events;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;

/**
 * Fired when a series is shown
 */
@DomEvent("series-show")
public class SeriesShowEvent extends ComponentEvent<Chart>
        implements HasSeries {

    private final int seriesIndex;

    public SeriesShowEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.target.index") int seriesIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
    }

    public int getSeriesItemIndex() {
        return seriesIndex;
    }
}
