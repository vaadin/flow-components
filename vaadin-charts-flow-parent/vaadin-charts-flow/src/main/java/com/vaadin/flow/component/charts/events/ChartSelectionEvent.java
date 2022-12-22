/**
 * Copyright 2000-2022 Vaadin Ltd.
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
import com.vaadin.flow.component.charts.model.ChartModel;
import com.vaadin.flow.component.charts.model.Dimension;

/**
 * The ChartSelectionEvent class stores information on selection events on the
 * chart's area.
 */
@DomEvent("chart-selection")
public class ChartSelectionEvent extends ComponentEvent<Chart> {

    private final Double selectionStart;
    private final Double selectionEnd;
    private final Double valueStart;
    private final Double valueEnd;

    /**
     * Construct a ChartSelectionEvent
     *
     * @param source
     * @param selectionStart
     * @param selectionEnd
     * @param valueStart
     * @param valueEnd
     */
    public ChartSelectionEvent(Chart source, boolean fromClient,
            @EventData("event.detail.xAxisMin") Double selectionStart,
            @EventData("event.detail.xAxisMax") Double selectionEnd,
            @EventData("event.detail.yAxisMin") Double valueStart,
            @EventData("event.detail.yAxisMax") Double valueEnd) {
        super(source, fromClient);
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
        this.valueStart = valueStart;
        this.valueEnd = valueEnd;
    }

    /**
     * This value is undefined and shouldn't be considered if
     * {@link ChartModel#setZoomType(Dimension)} was set to {@link Dimension#Y}
     *
     * @return the X coordinate where the selection started if ZoomType is
     *         {@link Dimension#X} or {@link Dimension#XY}.
     */
    public Double getSelectionStart() {
        return selectionStart;
    }

    /**
     * This value is undefined and shouldn't be considered if
     * {@link ChartModel#setZoomType(Dimension)} was set to {@link Dimension#Y}
     *
     * @return the X coordinate where the selection ended if ZoomType is
     *         {@link Dimension#X} or {@link Dimension#XY}.
     */
    public Double getSelectionEnd() {
        return selectionEnd;
    }

    /**
     * This value is undefined and shouldn't be considered if
     * {@link ChartModel#setZoomType(Dimension)} was set to {@link Dimension#X}
     *
     * @return the Y coordinate where the selection started if ZoomType is
     *         {@link Dimension#Y} or {@link Dimension#XY}.
     */
    public Double getValueStart() {
        return valueStart;
    }

    /**
     * This value is undefined and shouldn't be considered if
     * {@link ChartModel#setZoomType(Dimension)} was set to {@link Dimension#X}
     *
     * @return the Y coordinate where the selection ended if ZoomType is
     *         {@link Dimension#Y} or {@link Dimension#XY}.
     */
    public Double getValueEnd() {
        return valueEnd;
    }
}
