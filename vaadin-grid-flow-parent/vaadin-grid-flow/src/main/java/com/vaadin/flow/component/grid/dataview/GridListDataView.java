/*
 * Copyright 2000-2020 Vaadin Ltd.
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

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * GridListDataView for in-memory list data handling.
 *
 * @param <T>
 *         data type
 * @since
 */
public class GridListDataView<T> extends AbstractListDataView<T>
        implements GridDataView<T> {
    private DataCommunicator<T> dataCommunicator;
    private Grid<T> grid;

    public GridListDataView(DataCommunicator<T> dataCommunicator,
            Grid<T> grid) {
        super(dataCommunicator::getDataProvider, grid);
        this.dataCommunicator = dataCommunicator;
        this.grid = grid;
    }

    @Override
    public Stream<T> getCurrentItems() {
        final DataKeyMapper<T> keyMapper = dataCommunicator.getKeyMapper();
        return dataCommunicator.getActiveKeyOrdering().stream()
                .map(keyMapper::get);
    }

    @Override
    public T getItemOnRow(int rowIndex) {
        validateItemIndex(rowIndex);
        return getAllItems().skip(rowIndex).findFirst().orElse(null);
    }

    @Override
    public void selectItemOnRow(int rowIndex) {
        grid.select(getItemOnRow(rowIndex));
        grid.scrollToIndex(rowIndex);
    }

    /**
     * Add a new data filter.
     * To be removed after #8313
     *
     * @param filter
     *         the filter to add, not <code>null</code>
     * @return this
     */
    public GridListDataView<T> addFilter(SerializablePredicate<T> filter) {
        getDataProvider().addFilter(filter);
        return this;
    }

    /**
     * Remove all in-memory filters.
     * To be removed after #8313
     *
     * @return this
     */
    public GridListDataView<T> clearFilters() {
        getDataProvider().clearFilters();
        return this;
    }

    /**
     * Get all items as a List.
     *
     * @return List of all items
     */
    public List<T> getItems() {
        final Collection<T> items = getDataProvider().getItems();
        if (items instanceof List) {
            return (List) items;
        }
        throw new IllegalArgumentException(
                "DataProvider collection is not a list.");
    }

    @Override
    public Stream<T> getAllItems() {
        return getDataProvider()
                .fetch(dataCommunicator.buildQuery(0, Integer.MAX_VALUE));
    }

    @Override
    public int getDataSize() {
        return dataCommunicator.getDataSize();
    }
}
