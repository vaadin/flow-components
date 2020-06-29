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

package com.vaadin.flow.component.select.data;

import java.util.Objects;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Implementation of generic data view for {@link Select}.
 *
 * @param <T>
 *            item type
 * @since
 */
public class SelectDataViewImpl<T> extends AbstractDataView<T>
        implements SelectDataView<T> {

    /**
     * Constructs a new DataView.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param select
     *            select instance for this DataView
     */
    public SelectDataViewImpl(
            SerializableSupplier<DataProvider<T, ?>> dataProviderSupplier,
            Select<T> select) {
        super(dataProviderSupplier, select);
    }

    @Override
    public T getItemOnIndex(int index) {
        final int dataSize = getSize();
        if (index < 0 || index >= dataSize) {
            throw new IndexOutOfBoundsException(String.format(
                    "Given index %d is outside of the accepted range '0 - %d'",
                    index, dataSize - 1));
        }
        return getItems().skip(index).findFirst().orElse(null);
    }

    @Override
    public boolean contains(T item) {
        final IdentifierProvider<T> identifierProvider =
                getIdentifierProvider();

        Object itemIdentifier = identifierProvider.apply(item);
        Objects.requireNonNull(itemIdentifier,
                "Identity provider should not return null");
        //@formatter:off
        return getItems().anyMatch(
                i -> itemIdentifier.equals(
                        identifierProvider.apply(i)));
        //@formatter:on
    }

    @Override
    protected Class<?> getSupportedDataProviderType() {
        return DataProvider.class;
    }
}
