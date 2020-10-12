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

import java.util.stream.Stream;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.data.provider.ItemCountChangeEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;

/**
 * Implementation of generic data view for ComboBox. This implementation does
 * not depend on a certain type of data provider, i.e. whether is it of
 * in-memory or backend type. It can be used if the type of data provider
 * is not either {@link ListDataProvider} or {@link BackEndDataProvider}.
 *
 * @param <T> the item type
 * @since
 */
public class ComboBoxDataView<T> extends AbstractDataView<T> {

    private DataCommunicator<T> dataCommunicator;

    /**
     * Creates a new generic data view for ComboBox and verifies the passed data
     * provider is compatible with this data view implementation.
     *
     * @param dataCommunicator
     *            the data communicator of the component
     * @param comboBox
     *            the ComboBox
     */
    public ComboBoxDataView(DataCommunicator<T> dataCommunicator,
            ComboBox<T> comboBox) {
        super(dataCommunicator::getDataProvider, comboBox);
        this.dataCommunicator = dataCommunicator;
    }

    @Override
    public T getItem(int index) {
        return dataCommunicator.getItem(index);
    }

    @Override
    protected Class<?> getSupportedDataProviderType() {
        return DataProvider.class;
    }

    @Override
    public Stream<T> getItems() {
        return dataCommunicator.getDataProvider()
                .fetch(dataCommunicator.buildQuery(0, Integer.MAX_VALUE));
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
     * listeners added by this method, if the items count changed due to combo
     * box's client filter applied by user.
     */
    @Override
    public Registration addItemCountChangeListener(
            ComponentEventListener<ItemCountChangeEvent<?>> listener) {
        return super.addItemCountChangeListener(listener);
    }
}
