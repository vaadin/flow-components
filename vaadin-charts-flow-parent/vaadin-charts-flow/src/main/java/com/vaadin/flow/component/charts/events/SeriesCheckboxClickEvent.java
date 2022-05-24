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

/**
 * SeriesCheckboxClickEvent triggered when a checkbox in a legend is clicked
 */
@DomEvent("series-checkbox-click")
public class SeriesCheckboxClickEvent extends ComponentEvent<Chart>
        implements HasSeries {

    private final boolean checked;
    private final int seriesIndex;

    /**
     * Constructs a SeriesCheckboxClickEvent
     *
     * @param source
     * @param fromClient
     * @param isChecked
     * @param seriesIndex
     */
    public SeriesCheckboxClickEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.checked") boolean isChecked,
            @EventData("event.detail.originalEvent.item.index") int seriesIndex) {
        super(source, fromClient);
        this.checked = isChecked;
        this.seriesIndex = seriesIndex;
    }

    /**
     * Checks if the checkbox is checked
     *
     * @return
     */
    public boolean isChecked() {
        return checked;
    }

    @Override
    public int getSeriesItemIndex() {
        return seriesIndex;
    }
}
