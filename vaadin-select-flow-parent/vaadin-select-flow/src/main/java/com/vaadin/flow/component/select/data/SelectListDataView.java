package com.vaadin.flow.component.select.data;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Data view implementation for {@link Select} with in-memory list data.
 * Provides information on the data and allows operations on it.
 *
 * @param <T>
 *            item type
 */
public class SelectListDataView<T> extends AbstractListDataView<T> {

    /**
     * Creates a new in-memory data view for Select and verifies the passed data
     * provider is compatible with this data view implementation.
     *
     * @param dataProviderSupplier
     *            supplier from which the DataProvider can be gotten
     * @param select
     *            select component that the dataView is bound to
     */
    public SelectListDataView(
            SerializableSupplier<DataProvider<T, ?>> dataProviderSupplier,
            Select<T> select) {
        super(dataProviderSupplier, select);
    }
}
