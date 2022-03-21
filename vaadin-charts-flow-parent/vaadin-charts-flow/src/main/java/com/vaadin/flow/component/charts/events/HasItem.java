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

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AbstractSeriesItem;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Node;
import com.vaadin.flow.component.charts.model.NodeSeries;
import com.vaadin.flow.component.charts.model.Series;

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
     * Example for {@link NodeSeries}:
     *
     * <pre>
     * String id = this.getItemId();
     * NodeSeries series = (NodeSeries) this.getSeries();
     * Optional&lt;Node&gt; nodeForId = series.getNodes().stream()
     *   .filter(node -> node.getId().equals(id))
     *   .findFirst();
     * </pre>
     * <p>
     * Only {@link AbstractSeriesItem} and {@link Node} support setting an ID.
     * For other types of series items this property will always return null.
     * For {@link AbstractSeriesItem} the ID is optional. Unless the developer
     * has explicitly set an ID for the item associated with the event, this
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
