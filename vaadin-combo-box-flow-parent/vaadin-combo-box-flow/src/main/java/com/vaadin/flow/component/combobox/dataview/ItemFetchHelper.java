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

package com.vaadin.flow.component.combobox.dataview;

import java.io.Serializable;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.Query;

/**
 * Helper methods for fetching the ComboBox's items in data views.
 */
final class ItemFetchHelper implements Serializable {

    private ItemFetchHelper() {
    }

    /**
     * Gets the items available in the ComboBox's service-side by requesting the
     * data provider directly, and not using the data communicator's cache.
     *
     * @param dataCommunicator
     *            data communicator of the ComboBox
     * @param <T>
     *            item type
     * @return stream of items available for the ComboBox
     */
    @SuppressWarnings("unchecked")
    static <T> Stream<T> getItems(DataCommunicator<T> dataCommunicator) {
        return dataCommunicator.getDataProvider()
                .fetch(getQuery(dataCommunicator));
    }

    /**
     * Gets the item at the given index from the data available in the
     * ComboBox's server-side by requesting the data provider directly, and not
     * using the data communicator's cache.
     *
     * @param dataCommunicator
     *            data communicator of the ComboBox
     * @param index
     *            item index number
     * @param <T>
     *            item type
     * @return item on index
     * @throws IndexOutOfBoundsException
     *             requested index is outside of the data set
     */
    @SuppressWarnings("unchecked")
    static <T> T getItem(DataCommunicator<T> dataCommunicator, int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index must be non-negative");
        }
        if (dataCommunicator.isDefinedSize()) {
            final int itemCount = dataCommunicator.getDataProviderSize();
            if (itemCount == 0) {
                throw new IndexOutOfBoundsException(String
                        .format("Requested index %d on empty data.", index));
            } else if (index >= itemCount) {
                throw new IndexOutOfBoundsException(String.format(
                        "Given index %d should be less than the item count '%d'",
                        index, itemCount));
            }
        }
        return (T) dataCommunicator.getDataProvider()
                .fetch(getQuery(dataCommunicator, index, 1)).findFirst()
                .orElse(null);
    }

    /**
     * Creates a query to be used by data provider for fetching a whole range of
     * items taking into account the filtering and sorting of a given data
     * communicator.
     *
     * @param dataCommunicator
     *            data communicator of the ComboBox
     * @param <T>
     *            item type
     * @return query object
     */
    @SuppressWarnings("rawtypes")
    static <T> Query getQuery(DataCommunicator<T> dataCommunicator) {
        return getQuery(dataCommunicator, 0, Integer.MAX_VALUE);
    }

    /**
     * Creates a query to be used by data provider for fetching a given range of
     * items taking into account the filtering and sorting of a given data
     * communicator.
     *
     * @param dataCommunicator
     *            data communicator of the ComboBox
     * @param offset
     *            offset for data request
     * @param limit
     *            number of items to fetch
     * @param <T>
     *            item type
     * @return query object
     */
    @SuppressWarnings("rawtypes")
    static <T> Query getQuery(DataCommunicator<T> dataCommunicator, int offset,
            int limit) {
        return dataCommunicator.buildQuery(offset, limit);
    }
}
