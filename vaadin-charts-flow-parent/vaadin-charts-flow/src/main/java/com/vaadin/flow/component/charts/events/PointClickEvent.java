/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

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
     * @param fromClient
     * @param pageX
     * @param pageY
     * @param altKey
     * @param ctrlKey
     * @param metaKey
     * @param shiftKey
     * @param button
     * @param seriesIndex
     * @param category
     * @param pointIndex
     * @param pointId
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
