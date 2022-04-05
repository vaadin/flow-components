/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.listbox;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import com.vaadin.flow.data.selection.SingleSelect;

/**
 * List Box allows the user to select one or more values from a scrollable list
 * of items. Although its functionally similar to Checkbox Group and Radio
 * Button Group, List Box is designed to be used as a lightweight scrollable
 * selection list rather than a form input field.
 * <p>
 * List Box also supports using dividers to group related items. Use them
 * sparingly to avoid creating unnecessary visual clutter. List Box supports
 * both single and multiple selection. The former allows the user to select only
 * one item while the latter enables multiple items to be selected.
 * <p>
 * Items can be rendered with rich content instead of plain text. This can be
 * useful to provide additional information in a more legible fashion than
 * appending it to the item text.
 * <p>
 * Best Practices:<br>
 * List Box is not designed to be used as an input field in forms, and lacks
 * features like label, helper, and validation errors. List Box is best suited
 * to be used as a lightweight, scrollable, single-column list for single or
 * multi-selection of items.
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
