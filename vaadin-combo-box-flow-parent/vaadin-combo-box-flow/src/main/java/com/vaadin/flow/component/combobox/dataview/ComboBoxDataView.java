package com.vaadin.flow.component.combobox.dataview;

import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.IdentifierProvider;

/**
 * Implementation of generic data view for ComboBox.
 *
 * @param <T>
 *            the item type
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
}
