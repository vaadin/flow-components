package com.vaadin.flow.component.charts.events;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */


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
