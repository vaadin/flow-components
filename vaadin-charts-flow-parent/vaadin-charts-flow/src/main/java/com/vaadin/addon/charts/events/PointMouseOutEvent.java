package com.vaadin.addon.charts.events;

import com.vaadin.addon.charts.Chart;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Fired when the mouse exits the neighborhood of a point
 */
@DomEvent("point-mouse-out")
public class PointMouseOutEvent extends ComponentEvent<Chart> implements HasItem {

    private final String category;
    private final int seriesIndex;
    private final int pointIndex;

    public PointMouseOutEvent(Chart source, boolean fromClient,
                              @EventData("event.detail.originalEvent.target.series.index") int seriesIndex,
                              @EventData("event.detail.originalEvent.target.index") int pointIndex,
                              @EventData("event.detail.originalEvent.target.category") String category) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
        this.pointIndex = pointIndex;
        this.category = category;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public int getItemIndex() {
        return pointIndex;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }
}
