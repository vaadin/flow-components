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
