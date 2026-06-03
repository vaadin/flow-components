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
