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
 * The PointLegendItemClickEvent class stores information on click events on the
 * charts's legend items that correspond to a chart point.
 */
@DomEvent("point-legend-item-click")
public class PointLegendItemClickEvent extends ComponentEvent<Chart>
        implements HasItem {

    private final int seriesIndex;
    private final String category;
    private final int pointIndex;

    /**
     * Constructs a SeriesLegendItemClickEvent
     * 
     * @param source
     * @param fromClient
     */
    public PointLegendItemClickEvent(Chart source, boolean fromClient,
            @EventData("event.detail.point.series.index") int seriesIndex,
            @EventData("event.detail.point.category") String category,
            @EventData("event.detail.point.index") int pointIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
        this.category = category;
        this.pointIndex = pointIndex;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public int getItemIndex() {
        return pointIndex;
    }
}
