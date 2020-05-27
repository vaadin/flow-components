package com.vaadin.flow.component.select.data;

import java.util.Optional;

import com.vaadin.flow.data.provider.DataView;

/**
 * An extended {@link DataView} interface for Select component
 *
 * @param <T>
 *         item type
 */
public interface SelectDataView<T> extends DataView<T> {
    /**
     * Gets the item at the given index in the sorted and filtered data set.
     *
     * @param index
     *         index to get item at
     * @return item at index
     * @throws IndexOutOfBoundsException
     *         requested index is outside of the available data set range.
     */
    T getItemOnIndex(int index);

    /**
     * Selects the item at the given index in the sorted and filtered data set.
     *
     * @param index
     *         index to select item at
     * @return item at index
     * @throws IndexOutOfBoundsException
     *         requested index is outside of the available data set range.
     */
    T selectItem(int index);

    /**
     * Selects the item after the currently selected item in the sorted and
     * filtered data set.
     *
     * @return the new selected item, or empty if the currently selected item is
     * the last item in the data set or no selection.
     */
    Optional<T> selectNextItem();

    /**
     * Selects the item before the currently selected item in the sorted and
     * filtered data set.
     *
     * @return the new selected item, or empty if the currently selected item is
     * the first item in the data set or no selection.
     */
    Optional<T> selectPreviousItem();
}
