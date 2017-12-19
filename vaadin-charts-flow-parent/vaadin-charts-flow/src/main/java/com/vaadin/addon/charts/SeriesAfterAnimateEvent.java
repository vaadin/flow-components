package com.vaadin.addon.charts;

import com.vaadin.ui.event.ComponentEvent;
import com.vaadin.ui.event.DomEvent;
import com.vaadin.ui.event.EventData;

/**
 * Fired after a chart series is animated
 */
@DomEvent("series-after-animate")
public class SeriesAfterAnimateEvent extends ComponentEvent<Chart> implements HasSeries {

    private final int seriesIndex;

    public SeriesAfterAnimateEvent(Chart source, boolean fromClient,
                                   @EventData("event.detail.originalEvent.target.index") int seriesIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }
}
