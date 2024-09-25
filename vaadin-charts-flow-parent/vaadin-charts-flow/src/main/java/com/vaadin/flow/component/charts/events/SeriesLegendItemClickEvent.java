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
 * The SeriesLegendItemClickEvent class stores information on click events on
 * the charts's legend items that correspond to a chart series.
 */
@DomEvent("series-legend-item-click")
public class SeriesLegendItemClickEvent extends ComponentEvent<Chart>
        implements ClickEvent, HasSeries {

    private final int seriesIndex;
    private final MouseEventDetails details;

    /**
     * Constructs a SeriesLegendItemClickEvent
     *
     * @param source
     * @param fromClient
     */
    public SeriesLegendItemClickEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.browserEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.browserEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.browserEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.browserEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.browserEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.browserEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button,
            @EventData("event.detail.series.index") int seriesIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;

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
    public MouseEventDetails getMouseDetails() {
        return details;
    }
}
