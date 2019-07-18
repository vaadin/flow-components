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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;

/**
 * Server-side component for the {@code vaadin-list-box} element with
 * multi-selection.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the type of the items contained by this component
 * @see ListBox
 */
public class MultiSelectListBox<T>
        extends ListBoxBase<MultiSelectListBox<T>, T, Set<T>>
        implements MultiSelect<MultiSelectListBox<T>, T> {

    /**
     * Creates a new list box component with multi-selection.
     */
    public MultiSelectListBox() {
        super("selectedValues", JsonArray.class, Collections.emptySet(),
                MultiSelectListBox::presentationToModel,
                MultiSelectListBox::modelToPresentation);
        getElement().setProperty("multiple", true);
    }

    private static <T> Set<T> presentationToModel(MultiSelectListBox<T> listBox,
            JsonArray presentation) {
        return IntStream.range(0, presentation.length())
                .map(i -> (int) presentation.getNumber(i))
                .mapToObj(index -> listBox.getItems().get(index))
                .collect(Collectors.toSet());
    }

    private static <T> JsonArray modelToPresentation(
            MultiSelectListBox<T> listBox, Set<T> model) {
        JsonArray array = Json.createArray();
        model.stream().map(listBox.getItems()::indexOf)
                .forEach(index -> array.set(array.length(), index));
        return array;
    }

    /**
     * Sets the value of this component. If the new value is not equal to the
     * previous value, fires a value change event.
     * <p>
     * The component doesn't accept {@code null} values. The value of multi
     * select list box without any selected items is an empty set. You can use
     * the {@link #clear()} method to set the empty value.
     *
     * @param value
     *            the new value to set, not {@code null}
     * @throws NullPointerException
     *             if value is {@code null}
     */
    @Override
    public void setValue(Set<T> value) {
        Objects.requireNonNull(value,
                "Cannot set a null value to multi select list box. "
                        + "Use the clear-method to reset the component's value to an empty set.");
        super.setValue(value);
    }

    @Override
    public void updateSelection(Set<T> addedItems, Set<T> removedItems) {
        Set<T> value = new HashSet<>(getValue());
        value.addAll(addedItems);
        value.removeAll(removedItems);
        setValue(value);
    }

    @Override
    public Set<T> getSelectedItems() {
        return getValue();
    }

    @Override
    public Registration addSelectionListener(
            MultiSelectionListener<MultiSelectListBox<T>, T> listener) {
        return addValueChangeListener(event -> listener
                .selectionChange(new MultiSelectionEvent<>(this, this,
                        event.getOldValue(), event.isFromClient())));
    }
}
