/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

import java.time.Instant;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.util.Util;

/**
 * Fired when the point is dropped.
 */
@DomEvent("point-drop")
public class PointDropEvent extends ComponentEvent<Chart> implements HasItem {

    private final String category;
    private final Instant start;
    private final Instant end;
    private final String parent;
    private final Double x;
    private final Double y;
    private final int seriesIndex;
    private final int pointIndex;
    private final String pointId;

    public PointDropEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.target.series.index") int seriesIndex,
            @EventData("event.detail.originalEvent.target.index") int pointIndex,
            @EventData("event.detail.originalEvent.target.id") String pointId,
            @EventData("event.detail.originalEvent.target.category") String category,
            @EventData("event.detail.originalEvent.target.start") Double start,
            @EventData("event.detail.originalEvent.target.end") Double end,
            @EventData("event.detail.originalEvent.target.parent") String parent,
            @EventData("event.detail.originalEvent.target.x") Double x,
            @EventData("event.detail.originalEvent.target.y") Double y) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
        this.pointIndex = pointIndex;
        this.pointId = pointId;
        this.category = category;
        this.start = start != null ? Util.toServerInstant(start) : null;
        this.end = end != null ? Util.toServerInstant(end) : null;
        this.parent = parent;
        this.x = x;
        this.y = y;
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

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }

    /**
     * @return Supported only in Gantt charts: Returns the start date of the point at the moment the point was
     *         dropped.
     */
    public Instant getStart() {
        return start;
    }

    /**
     * @return Supported only in Gantt charts: Returns the end date of the point at the moment the point was
     *         dropped.
     */
    public Instant getEnd() {
        return end;
    }

    /**
     * @return Returns the parent id of the point at the moment the Drag event
     *         was fired.
     */
    public String getParent() {
        return parent;
    }

    /**
     * @return Returns the x value of the point at the moment the dragging
     *         started.
     */
    public Double getxValue() {
        return x;
    }

    /**
     * @return Returns the y value of the point at the moment the dragging
     *         started.
     */
    public Double getyValue() {
        return y;
    }

}
