/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.dataview;

import java.util.Optional;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Data view implementation for RadioButtonGroup with in-memory list data.
 * Provides information on the data and allows operations on it.
 *
 * @param <T>
 *            data type
 * @since
 */
public class RadioButtonGroupListDataView<T> extends AbstractListDataView<T> {

    private SerializableConsumer<IdentifierProvider<T>> identifierChangedCallback;

    /**
     * Creates a new in-memory data view for RadioButtonGroup and verifies the
     * passed data provider is compatible with this data view implementation.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param radioButtonGroup
     *            radioButton Group instance for this DataView
     * @param filterOrSortingChangedCallback
     *            callback, which is being invoked when the RadioButtonGroup's
     *            filtering or sorting changes, not <code>null</code>
     */
    public RadioButtonGroupListDataView(
            SerializableSupplier<? extends DataProvider<T, ?>> dataProviderSupplier,
            RadioButtonGroup radioButtonGroup,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataProviderSupplier, radioButtonGroup,
                filterOrSortingChangedCallback);
    }

    /**
     * Creates a new in-memory data view for RadioButtonGroup and verifies the
     * passed data provider is compatible with this data view implementation.
     *
     * @param dataProviderSupplier
     *            data provider supplier
     * @param radioButtonGroup
     *            radioButton Group instance for this DataView
     * @param identifierChangedCallback
     *            callback method which should be called when identifierProvider
     *            is changed
     * @param filterOrSortingChangedCallback
     *            callback, which is being invoked when the RadioButtonGroup's
     *            filtering or sorting changes, not <code>null</code>
     */
    public RadioButtonGroupListDataView(
            SerializableSupplier<? extends DataProvider<T, ?>> dataProviderSupplier,
            RadioButtonGroup radioButtonGroup,
            SerializableConsumer<IdentifierProvider<T>> identifierChangedCallback,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataProviderSupplier, radioButtonGroup,
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
