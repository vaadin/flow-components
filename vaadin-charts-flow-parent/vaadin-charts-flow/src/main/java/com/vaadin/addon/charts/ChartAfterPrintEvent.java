package com.vaadin.addon.charts;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;

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
