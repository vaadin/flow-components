package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.charts.Chart;

/**
 * Fired after a chart is loaded
 */
@DomEvent("chart-load")
public class ChartLoadEvent extends ComponentEvent<Chart> {

    /**
     * Constructs a ChartLoadEvent
     *
     * @param source
     * @param fromClient
     */
    public ChartLoadEvent(Chart source, boolean fromClient) {
        super(source, fromClient);
    }
}
