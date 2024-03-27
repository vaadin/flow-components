/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.select.data;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Implementation of generic data view for {@link Select}.
 *
 * @param <T>
 *            item type
 * @since
 */
public class SelectDataView<T> extends AbstractDataView<T> {

    private SerializableConsumer<IdentifierProvider<T>> identifierChangedCallback;

    /**
     * Constructs a new DataView.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param select
     *            select instance for this DataView
     */
    public SelectDataView(
            SerializableSupplier<DataProvider<T, ?>> dataProviderSupplier,
            Select<T> select) {
        super(dataProviderSupplier, select);
    }

    /**
     * Constructs a new DataView.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param select
     *            select instance for this DataView
     * @param identifierChangedCallback
     *            callback method which should be called when identifierProvider
     *            is changed
     */
    public SelectDataView(
            SerializableSupplier<DataProvider<T, ?>> dataProviderSupplier,
            Select<T> select,
            SerializableConsumer<IdentifierProvider<T>> identifierChangedCallback) {
        super(dataProviderSupplier, select);
        this.identifierChangedCallback = identifierChangedCallback;
    }

    @Override
    public T getItem(int index) {
        final int dataSize = dataProviderSupplier.get().size(new Query<>());
        if (dataSize == 0) {
            throw new IndexOutOfBoundsException(
                    String.format("Requested index %d on empty data.", index));
        }
        if (index < 0 || index >= dataSize) {
            throw new IndexOutOfBoundsException(String.format(
                    "Given index %d is outside of the accepted range '0 - %d'",
                    index, dataSize - 1));
        }
        return getItems().skip(index).findFirst().orElse(null);
    }

    @Override
    protected Class<?> getSupportedDataProviderType() {
        return DataProvider.class;
    }

    @Override
    public void setIdentifierProvider(
            IdentifierProvider<T> identifierProvider) {
        super.setIdentifierProvider(identifierProvider);

        if (identifierChangedCallback != null) {
            identifierChangedCallback.accept(identifierProvider);
        }
    }
}
