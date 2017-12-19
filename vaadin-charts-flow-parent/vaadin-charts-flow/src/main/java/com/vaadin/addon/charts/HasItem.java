package com.vaadin.addon.charts;

import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;

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
     * Returns the item that was clicked
     *
     * @return
     */
    default DataSeriesItem getItem() {
        return ((DataSeries) getSeries()).get(getItemIndex());
    }
}
