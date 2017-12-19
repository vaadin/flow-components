package com.vaadin.addon.charts;

import com.vaadin.ui.event.ComponentEvent;
import com.vaadin.ui.event.DomEvent;

/**
 * ChartDrillupEvent triggered after all the series  has been drilled up
 * if chart has multiple drilldown series
 */
@DomEvent("chart-drillupall")
public class ChartDrillupAllEvent extends ComponentEvent<Chart> {

    /**
     * Constructs a ChartDrillupAllEvent
     *
     * @param source
     * @param fromClient
     */
    public ChartDrillupAllEvent(Chart source, boolean fromClient) {
        super(source, fromClient);
    }
}
