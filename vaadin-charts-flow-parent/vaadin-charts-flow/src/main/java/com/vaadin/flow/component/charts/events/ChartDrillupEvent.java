/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.charts.Chart;

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
