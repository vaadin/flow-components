/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DrilldownCallback;
import com.vaadin.flow.component.charts.model.Series;

/**
 * The DrilldownEvent class stores information on click events in drilldown
 * points
 * 
 * @since 6.0.1
 */
@DomEvent("chart-drilldown")
public class DrilldownEvent extends ComponentEvent<Chart> implements HasItem {

    private final String drilldown;
    private final String category;
    private final Double x;
    private final Double y;
    private final int pointIndex;
    private final String pointId;
    private final int seriesIndex;

    /**
     * Construct a ChartDrilldownEvent
     *
     * @param source
     * @since 22.0.1
     */
    public DrilldownEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.point.drilldown") String drilldown,
            @EventData("event.detail.originalEvent.point.category") String category,
            @EventData("event.detail.originalEvent.point.x") Double x,
            @EventData("event.detail.originalEvent.point.y") Double y,
            @EventData("event.detail.originalEvent.point.index") int pointIndex,
            @EventData("event.detail.originalEvent.point.id") String pointId,
            @EventData("event.detail.originalEvent.point.series.index") int seriesIndex) {
        super(source, fromClient);

        this.drilldown = drilldown;
        this.category = category;
        this.x = x;
        this.y = y;
        this.pointIndex = pointIndex;
        this.pointId = pointId;
        this.seriesIndex = seriesIndex;
    }

    /**
     * Gets the drilldown value of the clicked point. For a point that you add
     * with {@link DataSeries#addItemWithDrilldown(DataSeriesItem, Series)},
     * this is the {@link Series#getId() ID} of the drilldown series. For a
     * point that you add with
     * {@link DataSeries#addItemWithDrilldown(DataSeriesItem)}, this is
     * {@code "true"}.
     *
     * @return the ID of the drilldown series, or {@code "true"} for a point
     *         that uses a {@link DrilldownCallback}
     * @deprecated Use {@link #getDrilldownSeries()} instead.
     */
    @Deprecated(since = "25.4")
    public String getDrilldown() {
        return drilldown;
    }

    /**
     * Gets the drilldown series of the clicked point. This is the series that
     * you pass to
     * {@link DataSeries#addItemWithDrilldown(DataSeriesItem, Series)}.
     * <p>
     * For a point that you add with
     * {@link DataSeries#addItemWithDrilldown(DataSeriesItem)}, this method
     * returns {@code null}. For such a point, the {@link DrilldownCallback}
     * gives the drilldown series.
     *
     * @return the drilldown series of the clicked point, or {@code null} if the
     *         point uses a {@link DrilldownCallback}
     * @since 25.4
     */
    public Series getDrilldownSeries() {
        if (drilldown == null) {
            return null;
        }
        return getSource().getConfiguration().getDrilldown().getSeries()
                .stream().filter(series -> drilldown.equals(series.getId()))
                .findFirst().orElse(null);
    }

    public Double getxValue() {
        return x;
    }

    public Double getyValue() {
        return y;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }

    @Override
    public int getItemIndex() {
        return pointIndex;
    }

    @Override
    public String getItemId() {
        return pointId;
    }
}
