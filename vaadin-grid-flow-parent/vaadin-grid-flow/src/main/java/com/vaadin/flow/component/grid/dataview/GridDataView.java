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

import java.util.stream.Stream;

import com.vaadin.flow.data.provider.DataView;

/**
 * GridDataView interface that extends the base data view features.
 *
 * @param <T>
 *         data type
 * @since
 */
public interface GridDataView<T> extends DataView<T> {

    /**
     * Get the items that are active on the client.
     *
     * @return Stream of items that the client side has currently in memory
     */
    Stream<T> getCurrentItems();

    /**
     * Get the item at the given row in the sorted and filetered data set.
     *
     * @param rowIndex
     *         row to get item at
     * @return item on row
     * @throws IndexOutOfBoundsException
     *         requested row is outside of the available data set.
     */
    T getItemOnRow(int rowIndex);

    /**
     * Select an item at the given row index and scroll row into view.
     *
     * @param rowIndex
     *         row index to select item at
     * @throws IndexOutOfBoundsException
     *         requested row is outside of the available data set.
     */
    void selectItemOnRow(int rowIndex);
}
