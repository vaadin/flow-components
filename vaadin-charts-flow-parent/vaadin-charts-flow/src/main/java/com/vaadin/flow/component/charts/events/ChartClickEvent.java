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
 * The ChartClickEvent class stores information on click events anywhere on the
 * area of the chart.
 */
@DomEvent("chart-click")
public class ChartClickEvent extends ComponentEvent<Chart>
        implements ClickEvent {

    private final MouseEventDetails details;

    /**
     * Constructs a ChartClickEvent
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
     */
    public ChartClickEvent(Chart source, boolean fromClient,
            @EventData("event.detail.xValue") Double x,
            @EventData("event.detail.yValue") Double y,
            @EventData("event.detail.originalEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button) {
        super(source, fromClient);

        details = new MouseEventDetails();
        if (x != null) {
            details.setxValue(x);
        }
        if (y != null) {
            details.setyValue(y);
        }
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
}
