/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.XAxis;

/**
 * The XAxesExtremesSetEvent class stores data for set extremes events on the x
 * axes of the chart.
 */
@DomEvent("xaxes-extremes-set")
public class XAxesExtremesSetEvent extends ComponentEvent<Chart>
        implements HasAxis<XAxis> {

    private double minimum;
    private double maximum;
    private final int axisIndex;

    public XAxesExtremesSetEvent(Chart source, boolean fromClient,
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
    public XAxis getAxis() {
        return getSource().getConfiguration().getxAxis(axisIndex);
    }
}
