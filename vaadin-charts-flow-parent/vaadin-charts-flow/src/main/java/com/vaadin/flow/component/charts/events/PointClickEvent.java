/**
 * Copyright 2000-2026 Vaadin Ltd.
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
 * The PointClickEvent class stores data for click events on the points of the
 * chart.
 */
@DomEvent("point-click")
public class PointClickEvent extends ComponentEvent<Chart>
        implements ClickEvent, HasItem {

    private final int seriesIndex;
    private final String category;
    private final int pointIndex;
    private final String pointId;
    private final MouseEventDetails details;

    /**
     * Constructs a PointClickEvent
     *
     * @param source
     *            the event source
     * @param fromClient
     *            whether the event originated from the client
     * @param pageX
     *            the absolute X coordinate
     * @param pageY
     *            the absolute Y coordinate
     * @param altKey
     *            whether the Alt key was pressed
     * @param ctrlKey
     *            whether the Ctrl key was pressed
     * @param metaKey
     *            whether the Meta key was pressed
     * @param shiftKey
     *            whether the Shift key was pressed
     * @param button
     *            the mouse button
     * @param seriesIndex
     *            the series index
     * @param category
     *            the category
     * @param pointIndex
     *            the point index
     * @param pointId
     *            the point id
     */
    public PointClickEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button,
            @EventData("event.detail.originalEvent.point.x") double x,
            @EventData("event.detail.originalEvent.point.y") double y,
            @EventData("event.detail.originalEvent.point.series.index") int seriesIndex,
            @EventData("event.detail.originalEvent.point.category") String category,
            @EventData("event.detail.originalEvent.point.index") int pointIndex,
            @EventData("event.detail.originalEvent.point.id") String pointId) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
        this.category = category;
        this.pointIndex = pointIndex;
        this.pointId = pointId;

        details = new MouseEventDetails();
        details.setxValue(x);
        details.setyValue(y);
        details.setAbsoluteX(pageX);
        details.setAbsoluteY(pageY);
        details.setButton(MouseEventDetails.MouseButton.of(button));
        details.setAltKey(altKey);
        details.setCtrlKey(ctrlKey);
        details.setMetaKey(metaKey);
        details.setShiftKey(shiftKey);
    }

    @Override
    public MouseEventDetails getMouseDetails() {
        return details;
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
