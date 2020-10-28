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

    @SuppressWarnings("unchecked")
    static <T> Stream<T> getItems(DataCommunicator<T> dataCommunicator) {
        return dataCommunicator.getDataProvider()
                .fetch(getQuery(dataCommunicator));
    }

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
                        "Given index %d is outside of the accepted range '0 - %d'",
                        index, itemCount - 1));
            }
        }
        return (T) dataCommunicator.getDataProvider()
                .fetch(getQuery(dataCommunicator, index, 1)).findFirst()
                .orElse(null);
    }

    @SuppressWarnings("rawtypes")
    static <T> Query getQuery(DataCommunicator<T> dataCommunicator) {
        return getQuery(dataCommunicator, 0, Integer.MAX_VALUE);
    }

    @SuppressWarnings("rawtypes")
    static <T> Query getQuery(DataCommunicator<T> dataCommunicator, int offset,
            int limit) {
        return dataCommunicator.buildQuery(offset, limit);
    }
}
