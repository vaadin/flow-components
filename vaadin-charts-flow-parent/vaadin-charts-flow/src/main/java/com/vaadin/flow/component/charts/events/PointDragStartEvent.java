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
 * Fired when starting to drag a point.
 */
@DomEvent("point-drag-start")
public class PointDragStartEvent extends ComponentEvent<Chart>
        implements HasItem {

    private final String category;
    private final Instant start;
    private final Instant end;
    private final String parent;
    private final Double x;
    private final Double y;
    private final int seriesIndex;
    private final int pointIndex;
    private final String pointId;

    public PointDragStartEvent(Chart source, boolean fromClient,
            @EventData("event.detail.point.series.index") int seriesIndex,
            @EventData("event.detail.point.index") int pointIndex,
            @EventData("event.detail.point.id") String pointId,
            @EventData("event.detail.point.category") String category,
            @EventData("event.detail.point.start") Double start,
            @EventData("event.detail.point.end") Double end,
            @EventData("event.detail.point.parent") String parent,
            @EventData("event.detail.point.x") Double x,
            @EventData("event.detail.point.y") Double y) {
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
     * @return Supported only in Gantt charts: Returns the start date of the
     *         point at the moment the dragging started.
     */
    public Instant getStart() {
        return start;
    }

    /**
     * @return Supported only in Gantt charts: Returns the end date of the point
     *         at the moment the dragging started.
     */
    public Instant getEnd() {
        return end;
    }

    /**
     * @return Returns the id of the point parent at the moment the dragging
     *         started.
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
