package com.vaadin.flow.component.select.data;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * An implementation of {@link SelectDataView} for in-memory list data handling
 *
 * @param <T>
 *         item type
 */
public class SelectListDataView<T> extends AbstractListDataView<T>
        implements SelectDataView<T> {

    /**
     * Constructs a new SelectListDataView
     *
     * @param dataProviderSupplier
     *         supplier from which the DataProvider can be gotten
     * @param select
     *         select component that the dataView is bound to
     */
    public SelectListDataView(
            SerializableSupplier<DataProvider<T, ?>> dataProviderSupplier,
            Select<T> select) {
        super(dataProviderSupplier, select);
    }

    @Override
    public T getItemOnIndex(int index) {
        validateItemIndex(index);
        return getItems().skip(index).findFirst().orElse(null);
    }
}
