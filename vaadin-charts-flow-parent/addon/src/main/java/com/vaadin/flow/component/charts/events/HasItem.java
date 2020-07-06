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

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;

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
