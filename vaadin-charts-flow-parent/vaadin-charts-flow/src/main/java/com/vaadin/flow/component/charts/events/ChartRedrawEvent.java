package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.charts.Chart;

/**
 * Fired after a chart is redrawn
 */
@DomEvent("chart-redraw")
public class ChartRedrawEvent extends ComponentEvent<Chart> {

    public ChartRedrawEvent(Chart source, boolean fromClient) {
        super(source, fromClient);
    }
}
