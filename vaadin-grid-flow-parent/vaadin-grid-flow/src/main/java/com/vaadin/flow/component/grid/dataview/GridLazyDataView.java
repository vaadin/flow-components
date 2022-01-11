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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractLazyDataView;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.Query;

/**
 * Data view implementation for Grid with lazy data fetching. Provides
 * information on the data and allows operations on it.
 *
 * @param <T>
 *            the type of the items in grid
 */
public class GridLazyDataView<T> extends AbstractLazyDataView<T> {

    /**
     * Creates a new lazy data view for grid and verifies the passed data
     * provider is compatible with this data view implementation.
     *
     * @param dataCommunicator
     *            the data communicator of the component
     * @param component
     *            the grid
     */
    public GridLazyDataView(DataCommunicator<T> dataCommunicator,
            Grid<T> component) {
        super(dataCommunicator, component);
    }

    /**
     * Sets a callback that the Grid uses to get the exact item count in the
     * backend. Use this when it is cheap to get the exact item count and it is
     * desired that the user sees the "full scrollbar size".
     * <p>
     * The given callback will be queried for the count instead of the data
     * provider {@link DataProvider#size(Query)} method when the component has a
     * distinct data provider set with
     * {@link HasLazyDataView#setItems(BackEndDataProvider)}.
     *
     * @param callback
     *            the callback to use for determining item count in the backend,
     *            not {@code null}
     * @see #setItemCountFromDataProvider()
     * @see #setItemCountUnknown()
     */
    public void setItemCountCallback(
            CallbackDataProvider.CountCallback<T, Void> callback) {
        getDataCommunicator().setCountCallback(callback);
    }

    /**
     * @inheritDoc
     *             <p>
     *             Calling this method will clear any previously set count
     *             callback with the
     *             {@link #setItemCountCallback(CallbackDataProvider.CountCallback)}
     *             method.
     */
    @Override
    public void setItemCountFromDataProvider() {
        super.setItemCountFromDataProvider();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calling this method will clear any previously set count callback
     * {@link #setItemCountCallback(CallbackDataProvider.CountCallback)}.
     */
    @Override
    public void setItemCountEstimate(int itemCountEstimate) {
        super.setItemCountEstimate(itemCountEstimate);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calling this method will clear any previously set count callback
     * {@link #setItemCountCallback(CallbackDataProvider.CountCallback)}.
     */
    @Override
    public void setItemCountUnknown() {
        super.setItemCountUnknown();
    }
}
