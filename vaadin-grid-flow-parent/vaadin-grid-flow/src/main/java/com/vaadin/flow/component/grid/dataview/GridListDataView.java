/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Data view implementation for Grid with in-memory list data. Provides
 * information on the data and allows operations on it.
 *
 * @param <T>
 *            data type
 * @since
 */
public class GridListDataView<T> extends AbstractListDataView<T> {
    private DataCommunicator<T> dataCommunicator;

    /**
     * Creates a new instance of Grid in-memory data view and verifies the
     * passed data provider is compatible with this data view implementation.
     *
     * @param dataCommunicator
     *            the data communicator of the Grid, not <code>null</code>
     * @param grid
     *            the Grid component, not <code>null</code>
     * @param filterOrSortingChangedCallback
     *            callback, which is being invoked when the Grid's filtering or
     *            sorting changes, not <code>null</code>
     */
    public GridListDataView(DataCommunicator<T> dataCommunicator, Grid<T> grid,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataCommunicator::getDataProvider, grid,
                filterOrSortingChangedCallback);
        this.dataCommunicator = dataCommunicator;
    }

    @Override
    public Stream<T> getItems() {
        return getDataProvider()
                .fetch(dataCommunicator.buildQuery(0, Integer.MAX_VALUE));
    }

    @Override
    public int getItemCount() {
        return dataCommunicator.getItemCount();
    }

    @Override
    public void setIdentifierProvider(
            IdentifierProvider<T> identifierProvider) {
        super.setIdentifierProvider(identifierProvider);
        dataCommunicator.getKeyMapper().setIdentifierGetter(identifierProvider);
    }
}
