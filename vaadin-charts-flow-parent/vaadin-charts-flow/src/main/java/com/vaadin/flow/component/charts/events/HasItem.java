/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
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
import com.vaadin.flow.component.charts.model.AbstractSeriesItem;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.model.TreeSeries;

/**
 * Indicates that an event has an associated item
 */
public interface HasItem extends HasSeries {

    Chart getSource();

    String getCategory();

    /**
     * Returns the index of the series item, that is associated with this event,
     * in {@link #getSeries()}. Can be used to identify the item within the
     * series.
     * <p>
     * Example for {@link ListSeries}:
     *
     * <pre>
     * int itemIndex = event.getItemIndex();
     * ListSeries series = (ListSeries) event.getSeries();
     * Number datum = series.getData()[itemIndex];
     * </pre>
     *
     * @return the index of the item in the series
     * @see #getItem()
     * @see #getItemId()
     */
    int getItemIndex();

    /**
     * The ID of the series item that is associated with the event. Can be used
     * to identify the item within the series.
     * <p>
     * Example for {@link TreeSeries}:
     *
     * <pre>
     * String id = this.getItemId();
     * TreeSeries series = (TreeSeries) this.getSeries();
     * Optional&lt;TreeSeriesItem&gt; treeItem = series.getData().stream()
     *   .filter(item -> item.getId().equals(id))
     *   .findFirst();
     * </pre>
     * <p>
     * Only {@link AbstractSeriesItem} supports setting an ID. For other types
     * of series items this property will always return null. For
     * {@link AbstractSeriesItem} the ID is optional. Unless the developer has
     * explicitly set an ID for the item associated with the event, this
     * property will be null. See {@link #getItem()} or {@link #getItemIndex()}
     * for alternatives.
     *
     * @return the ID of the series item associated with the event, or null if
     *         the series item has no ID
     *
     * @see #getItem()
     * @see #getItemIndex()
     */
    String getItemId();

    /**
     * Returns the data series item that this event is associated with.
     * <p>
     * <b>NOTE:</b> This method only works with series of type
     * {@link DataSeries}. For other series an
     * {@link UnsupportedOperationException} will be thrown. See
     * {@link #getItemIndex()} or {@link #getItemId()} for alternatives.
     *
     * @return the {@link DataSeriesItem} that is associated with this event
     * @throws UnsupportedOperationException
     *             when using this method with a series that is not a DataSeries
     * @see #getItemIndex()
     * @see #getItemId()
     */
    default DataSeriesItem getItem() {
        Series series = getSeries();
        if (!(series instanceof DataSeries)) {
            String seriesClassName = series.getClass().getSimpleName();
            throw new UnsupportedOperationException(String.format(
                    "HasItem.getItem does not support series of type: %s. Only series of type com.vaadin.flow.component.charts.model.DataSeries are supported. Please check the API docs for further information.",
                    seriesClassName));
        }
        return ((DataSeries) series).get(getItemIndex());
    }
}
