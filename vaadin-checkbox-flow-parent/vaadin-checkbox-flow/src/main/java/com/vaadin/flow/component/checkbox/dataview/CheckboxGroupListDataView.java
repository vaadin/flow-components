/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.dataview;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Data view implementation for Checkbox Group with in-memory list data.
 * Provides information on the data and allows operations on it.
 *
 * @param <T>
 *            data type
 * @since
 */
public class CheckboxGroupListDataView<T> extends AbstractListDataView<T> {

    private SerializableConsumer<IdentifierProvider<T>> identifierChangedCallback;

    /**
     * Creates a new in-memory data view for Checkbox Group and verifies the
     * passed data provider is compatible with this data view implementation.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param checkboxGroup
     *            checkbox group instance for this DataView
     * @param filterOrSortingChangedCallback
     *            callback, which is being invoked when the CheckboxGroup's
     *            filtering or sorting changes, not <code>null</code>
     */
    public CheckboxGroupListDataView(
            SerializableSupplier<DataProvider<T, ?>> dataProviderSupplier,
            CheckboxGroup<T> checkboxGroup,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataProviderSupplier, checkboxGroup,
                filterOrSortingChangedCallback);
    }

    /**
     * Creates a new in-memory data view for Checkbox Group and verifies the
     * passed data provider is compatible with this data view implementation.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param checkboxGroup
     *            checkbox group instance for this DataView
     * @param identifierChangedCallback
     *            callback method which should be called when identifierProvider
     *            is changed
     * @param filterOrSortingChangedCallback
     *            callback, which is being invoked when the CheckboxGroup's
     *            filtering or sorting changes, not <code>null</code>
     */
    public CheckboxGroupListDataView(
            SerializableSupplier<DataProvider<T, ?>> dataProviderSupplier,
            CheckboxGroup<T> checkboxGroup,
            SerializableConsumer<IdentifierProvider<T>> identifierChangedCallback,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataProviderSupplier, checkboxGroup,
                filterOrSortingChangedCallback);
        this.identifierChangedCallback = identifierChangedCallback;
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
