package com.vaadin.flow.component.charts.events;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
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
