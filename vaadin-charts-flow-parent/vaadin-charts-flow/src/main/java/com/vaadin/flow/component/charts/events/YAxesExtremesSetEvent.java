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
import com.vaadin.flow.component.charts.model.YAxis;

/**
 * The YAxesExtremesSetEvent class stores data for set extremes events on the y
 * axes of the chart.
 */
@DomEvent("yaxes-extremes-set")
public class YAxesExtremesSetEvent extends ComponentEvent<Chart>
        implements HasAxis<YAxis> {

    private double minimum;
    private double maximum;
    private int axisIndex;

    public YAxesExtremesSetEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.min") double min,
            @EventData("event.detail.originalEvent.max") double max,
            @EventData("event.detail.axis.userOptions.axisIndex") int axisIndex) {
        super(source, fromClient);
        this.minimum = min;
        this.maximum = max;
        this.axisIndex = axisIndex;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    @Override
    public int getAxisIndex() {
        return axisIndex;
    }

    @Override
    public YAxis getAxis() {
        return getSource().getConfiguration().getyAxis(axisIndex);
    }
}
