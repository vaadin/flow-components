/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.listbox.dataview;

import com.vaadin.flow.component.listbox.ListBoxBase;
import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Implementation of generic data view for ListBox.
 *
 * @param <T>
 *            the item type
 * @since
 */
public class ListBoxDataView<T> extends AbstractDataView<T> {

    /**
     * Constructs a new generic data view for ListBox and verifies the passed
     * data provider is compatible with this data view implementation.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param listBox
     *            list box instance for this DataView
     */
    public ListBoxDataView(
            SerializableSupplier<? extends DataProvider<T, ?>> dataProviderSupplier,
            ListBoxBase listBox) {
        super(dataProviderSupplier, listBox);
    }

    @Override
    protected Class<?> getSupportedDataProviderType() {
        return DataProvider.class;
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
}
