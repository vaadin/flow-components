package com.vaadin.flow.component.select.data;

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
}
