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

import java.util.ArrayList;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataController;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SizeChangeListener;
import com.vaadin.flow.shared.Registration;

/**
 * DataController for Grid data communication.
 *
 * @param <T>
 *         item type
 * @since
 */
public class GridDataController<T> implements DataController<T> {

    private Grid<T> grid;
    private DataCommunicator<T> dataCommunicator;
    private ArrayList<SizeChangeListener> listeners;

    /**
     * Constructs a new data controller for Grid.
     *
     * @param grid
     *         Grid component for data controller.
     */
    public GridDataController(Grid<T> grid) {
        this.grid = grid;
        this.dataCommunicator = grid.getDataCommunicator();
    }

    /**
     * Get the currently active sorted items that have been sent to the client.
     *
     * @return items sent to the client
     */
    public Stream<T> getCurrentItems() {
        final DataKeyMapper<T> keyMapper = dataCommunicator.getKeyMapper();
        return dataCommunicator.getActiveKeyOrdering().stream()
                .map(keyMapper::get);
    }

    @Override
    public ListDataProvider<T> getDataProvider() {
        return (ListDataProvider<T>) dataCommunicator.getDataProvider();
    }

    @Override
    public Registration addSizeChangeListener(SizeChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>(0);
        }
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    @Override
    public int getDataSize() {
        return dataCommunicator.getDataSize();
    }

    @Override
    public Stream<T> getAllItems() {
        return getDataProvider()
                .fetch(dataCommunicator.buildQuery(0, Integer.MAX_VALUE));
    }

    /**
     * Select given item in controller grid component.
     *
     * @param item
     *         item to select
     * @param rowIndex
     *         index of item
     */
    public void selectAndScrollTo(T item, int rowIndex) {
        grid.select(item);
        grid.scrollToIndex(rowIndex);
    }
}
