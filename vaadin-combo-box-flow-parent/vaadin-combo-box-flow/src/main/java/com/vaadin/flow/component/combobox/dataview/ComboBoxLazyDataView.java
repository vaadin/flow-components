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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.provider.AbstractLazyDataView;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.ItemCountChangeEvent;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;

/**
 * Data view implementation for ComboBox with lazy data fetching. Provides
 * information on the data and allows operations on it.
 *
 * @param <T>
 *            the type of the items in ComboBox
 */
public class ComboBoxLazyDataView<T> extends AbstractLazyDataView<T> {

    /**
     * Creates a new lazy data view for ComboBox and verifies the passed data
     * provider is compatible with this data view implementation.
     *
     * @param dataCommunicator
     *            the data communicator of the component
     * @param component
     *            the ComboBox
     */
    public ComboBoxLazyDataView(DataCommunicator<T> dataCommunicator,
            Component component) {
        super(dataCommunicator, component);
    }

    /**
     * Sets a callback that the combo box uses to get the exact item count in
     * the backend. Use this when it is cheap to get the exact item count and it
     * is desired that the user sees the "full scrollbar size".
     * <p>
     * The given callback will be queried for the count instead of the data
     * provider {@link DataProvider#size(Query)} method when the component has a
     * distinct data provider set with
     * {@link HasLazyDataView#setItems(BackEndDataProvider)}.
     *
     * @param callback
     *            the callback to use for determining item count in the backend,
     *            not {@code null}
     * @see #setItemCountFromDataProvider()
     * @see #setItemCountUnknown()
     */
    public void setItemCountCallback(
            CallbackDataProvider.CountCallback<T, String> callback) {
        getDataCommunicator().setCountCallback(callback);
    }

    /**
     * @inheritDoc
     *             <p>
     *             Calling this method will clear any previously set count
     *             callback with the
     *             {@link #setItemCountCallback(CallbackDataProvider.CountCallback)}
     *             method.
     */
    @Override
    public void setItemCountFromDataProvider() {
        super.setItemCountFromDataProvider();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calling this method will clear any previously set count callback
     * {@link #setItemCountCallback(CallbackDataProvider.CountCallback)}.
     */
    @Override
    public void setItemCountEstimate(int itemCountEstimate) {
        super.setItemCountEstimate(itemCountEstimate);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calling this method will clear any previously set count callback
     * {@link #setItemCountCallback(CallbackDataProvider.CountCallback)}.
     */
    @Override
    public void setItemCountUnknown() {
        super.setItemCountUnknown();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Combo box fires {@link ItemCountChangeEvent} and notifies all the
     * listeners added by this method, if the items count changed, for instance,
     * due to fetching more items while scrolling with unknown item count.
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
     * Gets the item at the given index from the data available in the
     * ComboBox's server-side.
     * <p>
     * This method does not take into account the ComboBox client-side
     * filtering, since it doesn't change the item count on the server-side, but
     * only makes it easier for users to search through the items in the UI.
     *
     * @param index
     *            item index number
     * @return item on index
     * @throws IndexOutOfBoundsException
     *             requested index is outside of the data set
     */
    @Override
    public T getItem(int index) {
        return ItemFetchHelper.getItem(getDataCommunicator(), index);
    }

    /**
     * Gets the items available on the ComboBox's server-side.
     * <p>
     * This method does not take into account the ComboBox client-filtering,
     * since it doesn't change the item count on the server-side, but only makes
     * it easier for users to search through the items in the UI.
     *
     * @return items available on the server-side
     */
    @Override
    public Stream<T> getItems() {
        return ItemFetchHelper.getItems(getDataCommunicator());
    }
}
