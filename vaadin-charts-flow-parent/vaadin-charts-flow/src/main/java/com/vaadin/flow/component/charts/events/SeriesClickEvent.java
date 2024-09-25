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
 * The SeriesClickEvent class stores data for click events on the series of the
 * chart.
 */
@DomEvent("series-click")
public class SeriesClickEvent extends ComponentEvent<Chart>
        implements ClickEvent, HasSeries {

    private final int seriesIndex;
    private final MouseEventDetails details;

    public SeriesClickEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button,
            @EventData("event.detail.originalEvent.point.x") double x,
            @EventData("event.detail.originalEvent.point.y") double y,
            @EventData("event.detail.originalEvent.point.series.index") int seriesIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;

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
    public int getSeriesItemIndex() {
        return seriesIndex;
    }

    @Override
    public MouseEventDetails getMouseDetails() {
        return details;
    }
}
