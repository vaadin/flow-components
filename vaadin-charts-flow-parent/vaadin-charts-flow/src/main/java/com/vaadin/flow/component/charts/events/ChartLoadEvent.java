/**
 * Copyright 2000-2024 Vaadin Ltd.
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
