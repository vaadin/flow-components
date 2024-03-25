/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.combobox.dataview;

import java.util.stream.Stream;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBoxBase;
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
 * in-memory or backend type. It can be used if the type of data provider is not
 * either {@link ListDataProvider} or {@link BackEndDataProvider}.
 *
 * @param <T>
 *            the item type
 */
public class ComboBoxDataView<T> extends AbstractDataView<T> {

    private final DataCommunicator<T> dataCommunicator;

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
            ComboBoxBase<?, T, ?> comboBox) {
        super(dataCommunicator::getDataProvider, comboBox);
        this.dataCommunicator = dataCommunicator;
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
        return ItemFetchHelper.getItem(dataCommunicator, index);
    }

    @Override
    protected Class<?> getSupportedDataProviderType() {
        return DataProvider.class;
    }

    /**
     * Gets the items available on the ComboBox's server-side.
     * <p>
     * This method does not take into account the ComboBox client-side
     * filtering, since it doesn't change the item count on the server-side, but
     * only makes it easier for users to search through the items in the UI.
     *
     * @return items available on the server-side
     */
    @Override
    public Stream<T> getItems() {
        return ItemFetchHelper.getItems(dataCommunicator);
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
     * listeners added by this method, if the items count changed, for instance,
     * due to adding or removing an item(s).
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
}
