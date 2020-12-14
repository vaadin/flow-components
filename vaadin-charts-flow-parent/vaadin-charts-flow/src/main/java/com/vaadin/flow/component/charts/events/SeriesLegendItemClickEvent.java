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
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;


/**
 * The SeriesLegendItemClickEvent class stores information on click events on
 * the charts's legend items that correspond to a chart series.
 */
@DomEvent("series-legend-item-click")
public class SeriesLegendItemClickEvent extends ComponentEvent<Chart> implements HasSeries {

    private final int seriesIndex;

    /**
     * Constructs a SeriesLegendItemClickEvent
     * 
     * @param source
     * @param fromClient
     */
    public SeriesLegendItemClickEvent(Chart source, boolean fromClient,
            @EventData("event.detail.series.index") int seriesIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }
}
