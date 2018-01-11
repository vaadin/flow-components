package com.vaadin.addon.charts;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;

/**
 * ChartDrillupEvent triggered when the 'Back to previous series' button is
 * clicked
 */
@DomEvent("chart-drillup")
public class ChartDrillupEvent extends ComponentEvent<Chart> {

    /**
     * Constructs a ChartDrillupEvent
     *
     * @param source
     * @param fromClient
     */
    public ChartDrillupEvent(Chart source, boolean fromClient) {
        super(source, fromClient);
    }
}
