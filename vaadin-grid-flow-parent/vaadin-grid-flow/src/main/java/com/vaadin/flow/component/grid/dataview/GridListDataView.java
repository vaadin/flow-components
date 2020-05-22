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

import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.ListDataProvider;
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

    /**
     * Construct a new GridListDataView.
     *
     * @param grid
     *         DataView Grid instance
     */
    public GridListDataView(Grid grid) {
        super(new GridDataController<>(grid));
    }

    @Override
    public Stream<T> getCurrentItems() {
        return getDataController().getCurrentItems();
    }

    @Override
    public T getItemOnRow(int rowIndex) {
        validateRowIndex(rowIndex);
        return getAllItemsAsList().get(rowIndex);
    }

    @Override
    public void selectItemOnRow(int rowIndex) {
        getDataController().selectAndScrollTo(getItemOnRow(rowIndex), rowIndex);
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
        return getAllItemsAsList();
    }

    private void validateRowIndex(int rowIndex) {
        if (getDataSize() == 0) {
            throw new IndexOutOfBoundsException(
                    String.format("Requested row %d on empty data.", rowIndex));
        }
        if (rowIndex < 0 || rowIndex >= getDataSize()) {
            throw new IndexOutOfBoundsException(String.format(
                    "Give row %d is outside of the accepted range '0 - %d'",
                    rowIndex, getDataSize() - 1));
        }
    }

    private GridDataController<T> getDataController() {
        return (GridDataController) dataController;
    }
}
