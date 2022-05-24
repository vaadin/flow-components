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
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;
import elemental.json.JsonArray;

/**
 * The ChartAddSeriesEvent class stores data about new series added to an
 * existing chart.
 */
@DomEvent("chart-add-series")
public class ChartAddSeriesEvent extends ComponentEvent<Chart> {

    private final String name;
    private final Number[] data;

    /**
     * Constructs a ChartAddSeriesEvent
     *
     * @param source
     * @param fromClient
     * @param name
     * @param data
     */
    public ChartAddSeriesEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.options.name") String name,
            @EventData("event.detail.originalEvent.options.data") JsonArray data) {
        super(source, fromClient);
        this.name = name;
        this.data = new Number[data.length()];
        for (int a = 0; a < data.length(); a++) {
            this.data[a] = data.getObject(a).getNumber("y");
        }
    }

    /**
     * Gets the series name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the series data
     *
     * @return
     */
    public Number[] getData() {
        return data;
    }
}
