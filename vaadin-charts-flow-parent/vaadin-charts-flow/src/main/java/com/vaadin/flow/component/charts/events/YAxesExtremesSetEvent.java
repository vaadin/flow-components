package com.vaadin.flow.component.charts.events;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2018 Vaadin Ltd
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
import com.vaadin.flow.component.charts.model.YAxis;

/**
 * The YAxesExtremesSetEvent class stores data for set extremes events
 * on the y axes of the chart.
 */
@DomEvent("yaxes-extremes-set")
public class YAxesExtremesSetEvent extends ComponentEvent<Chart> implements HasAxis<YAxis> {

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
