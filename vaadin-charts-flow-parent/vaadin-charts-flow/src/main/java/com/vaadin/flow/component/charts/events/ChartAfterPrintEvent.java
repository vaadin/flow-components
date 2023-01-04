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
