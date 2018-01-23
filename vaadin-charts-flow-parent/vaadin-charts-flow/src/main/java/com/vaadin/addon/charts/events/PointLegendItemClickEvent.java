package com.vaadin.addon.charts.events;

import com.vaadin.addon.charts.Chart;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

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
