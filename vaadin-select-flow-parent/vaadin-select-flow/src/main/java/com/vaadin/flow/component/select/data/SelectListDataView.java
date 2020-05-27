package com.vaadin.flow.component.select.data;

import java.util.Optional;
import java.util.function.Function;

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
    private Select<T> select;

    /**
     * Constructs a new SelectListDataView
     *
     * @param dataProviderSupplier
     *         supplier from which the DataProvider can be gotten
     * @param select
     *         select that the dataView is bound to
     */
    public SelectListDataView(
            SerializableSupplier<DataProvider<T, ?>> dataProviderSupplier,
            Select<T> select) {
        super(dataProviderSupplier, select);
        this.select = select;
    }

    @Override
    public T getItemOnIndex(int index) {
        validateItemIndex(index);
        return getAllItems().skip(index).findFirst().orElse(null);
    }

    @Override
    public T selectItem(int index) {
        T item = getItemOnIndex(index);
        select.setValue(item);

        return item;
    }

    @Override
    public Optional<T> selectNextItem() {
        return selectAnotherItem(this::getNextItem);
    }

    @Override
    public Optional<T> selectPreviousItem() {
        return selectAnotherItem(this::getPreviousItem);
    }

    private Optional<T> selectAnotherItem(Function<T, T> itemSupplier) {
        T selectedItem = select.getValue();
        if (selectedItem == null) {
            return Optional.empty();
        }
        T newItem = itemSupplier.apply(selectedItem);
        if (newItem == null) {
            return Optional.empty();
        }
        select.setValue(newItem);
        return Optional.of(newItem);
    }
}
