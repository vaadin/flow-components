/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;

/**
 * The PointUpdateEvent class stores data for update events on the points of the
 * chart
 */
@DomEvent("point-update")
public class PointUpdateEvent extends ComponentEvent<Chart> implements HasItem {

    private final int seriesIndex;
    private final String category;
    private final Double oldXValue;
    private final Double oldYValue;
    private final Double newXValue;
    private final Double newYValue;
    private final int pointIndex;
    private final String pointId;

    public PointUpdateEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.target.series.index") int seriesIndex,
            @EventData("event.detail.originalEvent.target.category") String category,
            @EventData("event.detail.originalEvent.target.index") int pointIndex,
            @EventData("event.detail.originalEvent.target.id") String pointId,
            @EventData("event.detail.originalEvent.target.x") Double oldXValue,
            @EventData("event.detail.originalEvent.target.y") Double oldYValue,
            @EventData("event.detail.originalEvent.options.x") Double newXValue,
            @EventData("event.detail.originalEvent.options.y") Double newYValue) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
        this.category = category;
        this.oldXValue = oldXValue;
        this.oldYValue = oldYValue;
        this.newXValue = newXValue;
        this.newYValue = newYValue;
        this.pointIndex = pointIndex;
        this.pointId = pointId;
    }

    public Double getOldXValue() {
        return oldXValue;
    }

    public Double getOldYValue() {
        return oldYValue;
    }

    public Double getxValue() {
        return newXValue;
    }

    public Double getyValue() {
        return newYValue;
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

    @Override
    public String getItemId() {
        return pointId;
    }
}
