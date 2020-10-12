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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.data.provider.ItemCountChangeEvent;
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
     */
    public ComboBoxListDataView(DataCommunicator<T> dataCommunicator,
            ComboBox<T> comboBox) {
        super(dataCommunicator::getDataProvider, comboBox);
        this.dataCommunicator = dataCommunicator;
    }

    @Override
    public Stream<T> getItems() {
        return getDataProvider().fetch(dataCommunicator.buildQuery(0,
                Integer.MAX_VALUE));
    }

    @Override
    public int getItemCount() {
        return dataCommunicator.getItemCount();
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
