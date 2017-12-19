package com.vaadin.addon.charts;

import com.vaadin.ui.event.ComponentEvent;
import com.vaadin.ui.event.DomEvent;
import com.vaadin.ui.event.EventData;

/**
 * Fired when the mouse moves within the neighborhood of a series
 */
@DomEvent("series-mouse-over")
public class SeriesMouseOverEvent extends ComponentEvent<Chart> implements HasSeries {

    private final int seriesIndex;

    public SeriesMouseOverEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.target.index") int seriesIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }
}
