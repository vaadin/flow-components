package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.charts.Chart;

/**
 * Fired after a chart is printed using the print menu
 */
@DomEvent("chart-after-print")
public class ChartAfterPrintEvent extends ComponentEvent<Chart> {

    /**
     * Constructs a ChartAfterPrintEvent
     *
     * @param source
     * @param fromClient
     */
    public ChartAfterPrintEvent(Chart source, boolean fromClient) {
        super(source, fromClient);
    }
}
