package com.vaadin.addon.charts;

import com.vaadin.ui.event.ComponentEvent;
import com.vaadin.ui.event.DomEvent;

/**
 * Fired before a chart is printed using the print menu
 */
@DomEvent("chart-before-print")
public class ChartBeforePrintEvent extends ComponentEvent<Chart> {

    /**
     * Constructs a ChartBeforePrintEvent
     *
     * @param source
     * @param fromClient
     */
    public ChartBeforePrintEvent(Chart source, boolean fromClient) {
        super(source, fromClient);
    }
}

