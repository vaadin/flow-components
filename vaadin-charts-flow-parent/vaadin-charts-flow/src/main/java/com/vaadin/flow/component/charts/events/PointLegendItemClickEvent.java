package com.vaadin.flow.component.charts.events;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;

/**
 * The PointLegendItemClickEvent class stores information on click events on the
 * charts's legend items that correspond to a chart point.
 */
@DomEvent("point-legend-item-click")
public class PointLegendItemClickEvent extends ComponentEvent<Chart>
        implements ClickEvent, HasItem {

    private final int seriesIndex;
    private final String category;
    private final int pointIndex;
    private final String pointId;
    private final MouseEventDetails details;

    /**
     * Constructs a SeriesLegendItemClickEvent
     *
     * @param source
     * @param fromClient
     */
    public PointLegendItemClickEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.browserEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.browserEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.browserEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.browserEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.browserEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.browserEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button,
            @EventData("event.detail.point.series.index") int seriesIndex,
            @EventData("event.detail.point.category") String category,
            @EventData("event.detail.point.index") int pointIndex,
            @EventData("event.detail.point.id") String pointId) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
        this.category = category;
        this.pointIndex = pointIndex;
        this.pointId = pointId;

        details = new MouseEventDetails();
        details.setAbsoluteX(pageX);
        details.setAbsoluteY(pageY);
        details.setButton(MouseEventDetails.MouseButton.of(button));
        details.setAltKey(altKey);
        details.setCtrlKey(ctrlKey);
        details.setMetaKey(metaKey);
        details.setShiftKey(shiftKey);
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

    @Override
    public MouseEventDetails getMouseDetails() {
        return details;
    }
}
