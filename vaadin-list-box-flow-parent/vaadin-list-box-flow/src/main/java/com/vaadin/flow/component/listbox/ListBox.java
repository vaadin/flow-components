/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.listbox;

import java.util.List;
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
            return Integer.valueOf(-1);
        }

        List<VaadinItem<T>> itemComponents = listBox.getItemComponents();
        int itemIndex = IntStream.range(0, itemComponents.size()).filter(
                i -> selectedItem.equals(itemComponents.get(i).getItem()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Could not find given value from the item set"));
        return Integer.valueOf(itemIndex);
    }
}
