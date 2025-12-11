/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid.dataview;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;

/**
 * Data view implementation for Grid with lazy data fetching and typed filter
 * support. Extends {@link GridLazyDataView} to provide filtering capabilities
 * with custom filter types.
 * <p>
 * This data view is returned when setting items on Grid using callbacks or data
 * providers that accept a typed filter parameter. It allows programmatically
 * setting a filter that will be passed to the fetch and count callbacks.
 * <p>
 * Example usage:
 *
 * <pre>
 * Grid&lt;Person&gt; grid = new Grid&lt;&gt;();
 * GridLazyFilterDataView&lt;Person, PersonFilter&gt; dataView = grid.setItems(
 *         query -&gt; {
 *             PersonFilter filter = query.getFilter().orElse(null);
 *             return personService.fetch(query.getOffset(), query.getLimit(),
 *                     filter);
 *         }, query -&gt; {
 *             PersonFilter filter = query.getFilter().orElse(null);
 *             return personService.count(filter);
 *         });
 *
 * // Set a filter - automatically refreshes the grid
 * dataView.setFilter(new PersonFilter("John", 25));
 *
 * // Clear the filter
 * dataView.setFilter(null);
 * </pre>
 *
 * @param <T>
 *            the type of the items in grid
 * @param <F>
 *            the type of the filter object used to filter items
 */
public class GridLazyFilterDataView<T, F> extends GridLazyDataView<T> {

    private final ConfigurableFilterDataProvider<T, Void, F> filterDataProvider;
    private F currentFilter;

    /**
     * Creates a new lazy data view for grid with typed filter support.
     *
     * @param dataCommunicator
     *            the data communicator of the component
     * @param component
     *            the grid
     * @param filterDataProvider
     *            the configurable filter data provider that wraps the actual
     *            data provider and handles filter management
     */
    public GridLazyFilterDataView(DataCommunicator<T> dataCommunicator,
            Grid<T> component,
            ConfigurableFilterDataProvider<T, Void, F> filterDataProvider) {
        super(dataCommunicator, component);
        this.filterDataProvider = filterDataProvider;
    }

    /**
     * Sets a filter for the grid's data provider. The filter will be passed to
     * the fetch and count callbacks via the {@code Query} object's
     * {@code getFilter()} method.
     * <p>
     * Setting a filter automatically triggers a refresh of the grid, causing
     * the fetch callback to be invoked with the new filter.
     * <p>
     * Setting the filter to {@code null} clears any previously set filter.
     *
     * @param filter
     *            the filter to apply, or {@code null} to clear the filter
     */
    public void setFilter(F filter) {
        this.currentFilter = filter;
        filterDataProvider.setFilter(filter);
    }

    /**
     * Sets a callback that the Grid uses to get the exact item count in the
     * backend with access to the typed filter. Use this when it is cheap to
     * get the exact item count and it is desired that the user sees the "full
     * scrollbar size".
     * <p>
     * The given callback will be queried for the count and will receive the
     * same filter that is passed to the fetch callback. This typed version
     * allows the count callback to access the filter without type casting.
     *
     * @param callback
     *            the callback to use for determining item count in the
     *            backend, not {@code null}
     * @see #setItemCountFromDataProvider()
     * @see #setItemCountUnknown()
     */
    public void setItemCountCallbackWithFilter(
            CallbackDataProvider.CountCallback<T, F> callback) {
        getDataCommunicator().setCountCallback(
                query -> callback.count(new com.vaadin.flow.data.provider.Query<>(
                        query.getOffset(), query.getLimit(),
                        query.getSortOrders(), query.getInMemorySorting(),
                        currentFilter)));
    }

    /**
     * Gets the current filter set on this data view.
     *
     * @return the current filter, or {@code null} if no filter is set
     */
    public F getFilter() {
        return currentFilter;
    }
}
