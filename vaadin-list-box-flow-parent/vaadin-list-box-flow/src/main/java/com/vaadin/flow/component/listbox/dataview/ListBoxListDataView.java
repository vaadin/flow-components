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
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Data view implementation for ListBox with in-memory list data. Provides
 * information on the data and allows operations on it.
 *
 * @param <T>
 *            data type
 * @since
 */
public class ListBoxListDataView<T> extends AbstractListDataView<T> {

    /**
     * Creates a new in-memory data view for ListBox and verifies the passed
     * data provider is compatible with this data view implementation.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param listBox
     *            listBox instance for this DataView
     * @param filterOrSortingChangedCallback
     *            callback, which is being invoked when the ListBox's filtering
     *            or sorting changes, not <code>null</code>
     */
    public ListBoxListDataView(
            SerializableSupplier<? extends DataProvider<T, ?>> dataProviderSupplier,
            ListBoxBase listBox,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataProviderSupplier, listBox, filterOrSortingChangedCallback);
    }
}
