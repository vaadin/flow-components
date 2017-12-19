package com.vaadin.addon.charts;

import com.vaadin.ui.event.ComponentEvent;
import com.vaadin.ui.event.DomEvent;

/**
 * Fired after a chart is redrawn
 */
@DomEvent("chart-redraw")
public class ChartRedrawEvent extends ComponentEvent<Chart> {

    public ChartRedrawEvent(Chart source, boolean fromClient) {
        super(source, fromClient);
    }
}
