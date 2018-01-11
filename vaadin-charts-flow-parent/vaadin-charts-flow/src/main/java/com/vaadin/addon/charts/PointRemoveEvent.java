package com.vaadin.addon.charts;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * The PointRemoveEvent class stores data for remove events on the points of the
 * chart
 */
@DomEvent("point-remove")
public class PointRemoveEvent extends ComponentEvent<Chart> implements HasItem {

    private final int seriesIndex;
    private final String category;
    private final double x;
    private final double y;
    private final int pointIndex;

    public PointRemoveEvent(Chart source, boolean fromClient,
                            @EventData("event.detail.originalEvent.target.series.index") int seriesIndex,
                            @EventData("event.detail.originalEvent.target.category") String category,
                            @EventData("event.detail.originalEvent.target.x") double x,
                            @EventData("event.detail.originalEvent.target.y") double y,
                            @EventData("event.detail.originalEvent.target.index") int pointIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
        this.category = category;
        this.x = x;
        this.y = y;
        this.pointIndex = pointIndex;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public int getItemIndex() {
        return pointIndex;
    }

    public double getxValue() {
        return x;
    }

    public double getyValue() {
        return y;
    }
}
