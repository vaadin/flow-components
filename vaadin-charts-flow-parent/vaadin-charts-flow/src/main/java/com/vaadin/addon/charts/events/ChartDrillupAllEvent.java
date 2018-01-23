package com.vaadin.addon.charts.events;

import com.vaadin.addon.charts.Chart;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;

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
