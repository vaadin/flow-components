/*
 * Copyright 2000-2019 Vaadin Ltd.
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
