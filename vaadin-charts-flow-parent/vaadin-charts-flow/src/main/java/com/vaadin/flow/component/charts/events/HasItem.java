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

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Series;

/**
 * Indicates that an event has an associated item
 */
public interface HasItem extends HasSeries {

    Chart getSource();

    String getCategory();

    /**
     * Returns the index of {@link #getItem()} in {@link #getSeries()}.
     *
     * @return
     */
    int getItemIndex();

    /**
     * Returns the data series item that this event is associated with.
     *
     * This method only works with series of type DataSeries. For other series an UnsupportedOperationException will be thrown.
     *
     * @return the DataSeriesItem that is associated with this event
     * @throws UnsupportedOperationException
     *             when using this method with a series that is not a DataSeries
     */
    default DataSeriesItem getItem() {
        Series series = getSeries();
        if(!(series instanceof DataSeries)) {
            String seriesClassName = series.getClass().getSimpleName();
            throw new UnsupportedOperationException(String.format("HasItem.getItem does not support series of type: %s. Only series of type com.vaadin.flow.component.charts.model.DataSeries are supported. Please check the API docs for further information.", seriesClassName));
        }
        return ((DataSeries) series).get(getItemIndex());
    }
}
