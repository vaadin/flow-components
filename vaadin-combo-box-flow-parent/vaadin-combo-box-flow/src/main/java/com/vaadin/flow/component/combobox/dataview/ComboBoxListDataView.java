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

import java.util.stream.Stream;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.data.provider.ItemCountChangeEvent;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * Data view implementation for ComboBox with in-memory list data. Provides
 * information on the data and allows operations on it.
 *
 * @param <T>
 *            the type of the items in ComboBox
 * @since
 */
public class ComboBoxListDataView<T> extends AbstractListDataView<T> {
    private DataCommunicator<T> dataCommunicator;

    /**
     * Creates a new instance of ComboBox in-memory data view and verifies the
     * passed data provider is compatible with this data view implementation.
     *
     * @param dataCommunicator
     *            the data communicator of the ComboBox, not <code>null</code>
     * @param comboBox
     *            the ComboBox component, not <code>null</code>
     * @param filterOrSortingChangedCallback
     *            callback, which is being invoked when the ComboBox's filtering
     *            or sorting changes, not <code>null</code>
     */
    public ComboBoxListDataView(DataCommunicator<T> dataCommunicator,
            ComboBox<T> comboBox,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataCommunicator::getDataProvider, comboBox,
                filterOrSortingChangedCallback);
        this.dataCommunicator = dataCommunicator;
    }

    /**
     * Gets the items available on the ComboBox's server-side.
     * <p>
     * Data is sorted the same way as in the ComboBox, but it does not take into
     * account the ComboBox client-side filtering, since it doesn't change the
     * item count on the server-side, but only makes it easier for users to
     * search through the items in the UI. Only the server-side filtering
     * considered, which is set by: {@link #setFilter(SerializablePredicate)} or
     * {@link #addFilter(SerializablePredicate)}.
     *
     * @return filtered and sorted items available in server-side
     */
    @SuppressWarnings("unchecked")
    @Override
    public Stream<T> getItems() {
        return getDataProvider()
                .fetch(ItemFetchHelper.getQuery(dataCommunicator));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method takes into account only the server-side filtering, which is
     * set by: {@link #setFilter(SerializablePredicate)} or
     * {@link #addFilter(SerializablePredicate)}. ComboBox's client-side filter
     * is not considered, since it doesn't change the item count on the
     * server-side, but only makes it easier for users to search through the
     * items in the UI.
     *
     * @return filtered item count
     */
    @SuppressWarnings("unchecked")
    @Override
    public int getItemCount() {
        return getDataProvider()
                .size(ItemFetchHelper.getQuery(dataCommunicator));
    }

    @Override
    public void setIdentifierProvider(
            IdentifierProvider<T> identifierProvider) {
        super.setIdentifierProvider(identifierProvider);
        dataCommunicator.getKeyMapper().setIdentifierGetter(identifierProvider);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Combo box fires {@link ItemCountChangeEvent} and notifies all the
     * listeners added by this method, if the items count changed due to adding
     * or removing an item(s), or by changing the server-side filtering with
     * {@link #setFilter(SerializablePredicate)} or
     * {@link #addFilter(SerializablePredicate)}.
     * <p>
     * ComboBox's client-side filter change won't fire
     * {@link ItemCountChangeEvent}, since it doesn't change the item count on
     * the server-side, but only makes it easier for users to search through the
     * items in the UI.
     */
    @Override
    public Registration addItemCountChangeListener(
            ComponentEventListener<ItemCountChangeEvent<?>> listener) {
        return super.addItemCountChangeListener(listener);
    }

    /**
     * Adds a filter to be applied to all queries. The filter will be used in
     * addition to any filter that has been set or added previously through
     * {@link #setFilter} or {@link #addFilter}. This filter is applied to data
     * set permanently until it's changed through {@link #setFilter} or
     * {@link #removeFilters}, in contrary with the client-side filter that can
     * be typed in by user and does not modify the data set on server-side, but
     * only defines which items are shown for a single request and erases on
     * drop down close.
     * <p>
     * This filter is bound to the component. Thus, any other component using
     * the same {@link DataProvider} object would not be affected by setting a
     * filter through data view of another component. A filter set by this
     * method won't be retained when a new {@link DataProvider} is set to the
     * component.
     *
     * @param filter
     *            the filter to add, not <code>null</code>
     * @return ComboBoxListDataView instance
     *
     * @see #setFilter(SerializablePredicate)
     * @see #removeFilters()
     */
    @Override
    public ComboBoxListDataView<T> addFilter(SerializablePredicate<T> filter) {
        return (ComboBoxListDataView<T>) super.addFilter(filter);
    }

    /**
     * Removes all in-memory filters set or added.
     *
     * @return ComboBoxListDataView instance
     *
     * @see #addFilter(SerializablePredicate)
     * @see #setFilter(SerializablePredicate)
     */
    @Override
    public ComboBoxListDataView<T> removeFilters() {
        return (ComboBoxListDataView<T>) super.removeFilters();
    }

    /**
     * Sets a filter to be applied to the data. The filter replaces any filter
     * that has been set or added previously. {@code null} will clear all
     * filters. This filter is applied to data set permanently until it's
     * changed through {@link #setFilter} or {@link #removeFilters}, in contrary
     * with the client-side filter that can be typed in by user and does not
     * modify the data set on server-side, but only defines which items are
     * shown for a single request and erases on drop down close.
     * <p>
     * This filter is bound to the component. Thus, any other component using
     * the same {@link DataProvider} object would not be affected by setting a
     * filter through data view of another component. A filter set by this
     * method won't be retained when a new {@link DataProvider} is set to the
     * component.
     *
     * @param filter
     *            filter to be set, or <code>null</code> to clear any previously
     *            set filters
     * @return ComboBoxListDataView instance
     *
     * @see #addFilter(SerializablePredicate)
     * @see #removeFilters()
     */
    @Override
    public ComboBoxListDataView<T> setFilter(SerializablePredicate<T> filter) {
        return (ComboBoxListDataView<T>) super.setFilter(filter);
    }
}
