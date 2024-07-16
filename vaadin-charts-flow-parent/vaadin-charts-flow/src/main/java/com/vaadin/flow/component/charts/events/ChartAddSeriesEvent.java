/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
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
