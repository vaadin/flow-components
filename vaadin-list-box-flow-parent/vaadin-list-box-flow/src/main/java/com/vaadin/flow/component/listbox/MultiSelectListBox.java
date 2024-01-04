/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.shared.DataChangeHandler;
import com.vaadin.flow.component.shared.SelectionOnDataChange;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataViewUtils;
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

    private DataChangeHandler<T> dataChangeHandler;

    /**
     * Creates a new list box component with multi-selection.
     */
    public MultiSelectListBox() {
        super("selectedValues", JsonArray.class, Collections.emptySet(),
                MultiSelectListBox::presentationToModel,
                MultiSelectListBox::modelToPresentation);
        getElement().setProperty("multiple", true);
        initDataChangeHandler();
    }

    private void initDataChangeHandler() {
        dataChangeHandler = new DataChangeHandler<>(
                SelectionOnDataChange.DISCARD) {

            @Override
            public void onPreserveAll(DataChangeEvent<T> dataChangeEvent) {
                rebuild();
            }

            @Override
            public void onPreserveExistent(DataChangeEvent<T> dataChangeEvent) {
                Set<Object> initialSelectedItemIds = getSelectedItems().stream()
                        .map(getDataProvider()::getId)
                        .collect(Collectors.toSet());
                @SuppressWarnings("unchecked")
                Stream<T> itemsStream = getDataProvider()
                        .fetch(DataViewUtils.getQuery(MultiSelectListBox.this));
                Set<T> existingItems = itemsStream
                        .filter(item -> initialSelectedItemIds
                                .contains(getDataProvider().getId(item)))
                        .limit(initialSelectedItemIds.size())
                        .collect(Collectors.toSet());
                setValue(existingItems);
                rebuild();
            }

            @Override
            public void onDiscard(DataChangeEvent<T> dataChangeEvent) {
                clear();
                rebuild();
            }
        };
    }

    private static <T> Set<T> presentationToModel(MultiSelectListBox<T> listBox,
            JsonArray presentation) {
        Set<T> modelValue = IntStream.range(0, presentation.length())
                .map(idx -> (int) presentation.getNumber(idx))
                .mapToObj(index -> listBox.getItems().get(index))
                .collect(Collectors.toSet());
        return Collections.unmodifiableSet(modelValue);
    }

    private static <T> JsonArray modelToPresentation(
            MultiSelectListBox<T> listBox, Set<T> model) {
        JsonArray array = Json.createArray();

        AtomicInteger idx = new AtomicInteger(0);
        listBox.getItems().forEach(item -> {
            int index = idx.getAndIncrement();
            Object itemId = listBox.getItemId(item);
            model.stream()
                    .filter(selectedItem -> itemId
                            .equals(listBox.getItemId(selectedItem)))
                    .findFirst()
                    .ifPresent(ignored -> array.set(array.length(), index));
        });

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

    /**
     * Returns an immutable set of the currently selected items. It is safe to
     * invoke other {@code SelectionModel} methods while iterating over the set.
     * <p>
     * There are no guarantees of the iteration order of the returned set of
     * items.
     *
     * @return the items in the current selection, not {@code null}
     */
    @Override
    public Set<T> getSelectedItems() {
        return getValue();
    }

    @Override
    public Registration addSelectionListener(
            MultiSelectionListener<MultiSelectListBox<T>, T> listener) {
        return addValueChangeListener(event -> {
            listener.selectionChange(new MultiSelectionEvent<>(this, this,
                    event.getOldValue(), event.isFromClient()));
        });
    }

    /**
     * Sets the selection strategy on data change. The default is
     * {@link SelectionOnDataChange#DISCARD}.
     *
     * @param selectionOnDataChange
     *            the selection strategy to switch to, not {@code null}
     *
     * @see SelectionOnDataChange
     */
    public void setSelectionOnDataChange(
            SelectionOnDataChange selectionOnDataChange) {
        dataChangeHandler.setSelectionOnDataChange(selectionOnDataChange);
    }

    /**
     * Gets the selection strategy on data change.
     *
     * @return the selection strategy
     *
     * @see #setSelectionOnDataChange(SelectionOnDataChange)
     */
    public SelectionOnDataChange getSelectionOnDataChange() {
        return dataChangeHandler.getSelectionOnDataChange();
    }

    @Override
    void handleDataChange(DataChangeEvent<T> dataChangeEvent) {
        if (dataChangeEvent instanceof DataChangeEvent.DataRefreshEvent) {
            super.handleDataChange(dataChangeEvent);
            return;
        }
        dataChangeHandler.handleDataChange(dataChangeEvent);
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
     *            the first set of instance
     * @param value2
     *            the second set of instance
     * @return <code>true</code> if sets are equal in size and also the items;
     *         otherwise <code>false</code>
     */
    @Override
    protected boolean valueEquals(Set<T> value1, Set<T> value2) {
        if (value1 == null && value2 == null)
            return true;
        if (value1 == null || value2 == null)
            return false;
        if (value1.size() != value2.size())
            return false;

        Set<Object> ids1 = value1.stream().map(super::getItemId)
                .collect(Collectors.toSet());
        Set<Object> ids2 = value2.stream().map(super::getItemId)
                .collect(Collectors.toSet());
        return ids1.equals(ids2);
    }
}
