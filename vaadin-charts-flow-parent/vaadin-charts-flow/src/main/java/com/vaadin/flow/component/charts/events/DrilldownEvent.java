/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;

/**
 * The DrilldownEvent class stores information on click events in drilldown
 * points
 */
@DomEvent("chart-drilldown")
public class DrilldownEvent extends ComponentEvent<Chart> implements HasItem {

    private final String drilldown;
    private final String category;
    private final Double x;
    private final Double y;
    private final int pointIndex;
    private final String pointId;
    private final int seriesIndex;

    /**
     * Construct a ChartDrilldownEvent
     *
     * @param source
     */
    public DrilldownEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.point.drilldown") String drilldown,
            @EventData("event.detail.originalEvent.point.category") String category,
            @EventData("event.detail.originalEvent.point.x") Double x,
            @EventData("event.detail.originalEvent.point.y") Double y,
            @EventData("event.detail.originalEvent.point.index") int pointIndex,
            @EventData("event.detail.originalEvent.point.id") String pointId,
            @EventData("event.detail.originalEvent.point.series.index") int seriesIndex) {
        super(source, fromClient);

        this.drilldown = drilldown;
        this.category = category;
        this.x = x;
        this.y = y;
        this.pointIndex = pointIndex;
        this.pointId = pointId;
        this.seriesIndex = seriesIndex;
    }

    /**
     * Gets the name of the drilldown
     *
     * @return
     */
    public String getDrilldown() {
        return drilldown;
    }

    public Double getxValue() {
        return x;
    }

    public Double getyValue() {
        return y;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
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
