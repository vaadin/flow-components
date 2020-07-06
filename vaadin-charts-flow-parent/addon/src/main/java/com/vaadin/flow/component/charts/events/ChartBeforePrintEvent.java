package com.vaadin.flow.component.charts.events;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.charts.Chart;

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

