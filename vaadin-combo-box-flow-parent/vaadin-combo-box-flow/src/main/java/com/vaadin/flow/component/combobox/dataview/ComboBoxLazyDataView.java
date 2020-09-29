package com.vaadin.flow.component.combobox.dataview;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.AbstractLazyDataView;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.Query;

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
     * <p>
     * Calling this method will clear any previously set count callback with the
     * {@link #setItemCountCallback(CallbackDataProvider.CountCallback)} method.
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
}
