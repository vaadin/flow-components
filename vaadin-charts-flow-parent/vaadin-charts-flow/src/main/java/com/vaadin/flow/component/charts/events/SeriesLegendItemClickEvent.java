package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
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
