/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.listbox;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import com.vaadin.flow.data.selection.SingleSelect;

/**
 * Server-side component for the {@code vaadin-list-box} element.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the type of the items contained by this component
 * @see MultiSelectListBox
 */
public class ListBox<T> extends ListBoxBase<ListBox<T>, T, T>
        implements SingleSelect<ListBox<T>, T> {

    /**
     * Creates a new list box component.
     */
    public ListBox() {
        super("selected", Integer.class, null, ListBox::presentationToModel,
                ListBox::modelToPresentation);
    }

    private static <T> T presentationToModel(ListBox<T> listBox,
            Integer selectedIndex) {
        if (selectedIndex == null || selectedIndex.intValue() == -1) {
            return null;
        }

        return listBox.getItemComponents().get(selectedIndex.intValue())
                .getItem();
    }

    private static <T> Integer modelToPresentation(ListBox<T> listBox,
            T selectedItem) {
        if (selectedItem == null) {
            return -1;
        }

        List<VaadinItem<T>> itemComponents = listBox.getItemComponents();
        return IntStream.range(0, itemComponents.size())
                .filter(idx -> listBox.getItemId(selectedItem).equals(
                        listBox.getItemId(itemComponents.get(idx).getItem())))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Could not find given value from the item set"));
    }

    /**
     * Compares two value instances to each other to determine whether they are
     * equal. Equality is used to determine whether to update internal state and
     * fire an event when {@link #setValue(Object)} or
     * {@link #setModelValue(Object, boolean)} is called. Subclasses can
     * override this method to define an alternative comparison method instead
     * of {@link Objects#equals(Object)}.
     *
     * @param value1
     *            the first instance
     * @param value2
     *            the second instance
     * @return <code>true</code> if the instances are equal; otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean valueEquals(T value1, T value2) {
        if (value1 == null && value2 == null)
            return true;
        if (value1 == null || value2 == null)
            return false;
        return getItemId(value1).equals(getItemId(value2));
    }
}
