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
package com.vaadin.flow.component.virtuallist;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

/**
 * Implementation of a SelectionModel.Multi.
 *
 * @param <T>
 *            the virtual list bean type
 * @author Vaadin Ltd.
 */
public class VirtualListMultiSelectionModel<T>
        implements SelectionModel.Multi<VirtualList<T>, T> {

    private final Map<Object, T> selected;
    private VirtualList<T> list;

    /**
     * Constructor for passing a reference of the virtual list to this
     * implementation.
     *
     * @param list
     *            reference to the virtual list for which this selection model
     *            is created
     */
    public VirtualListMultiSelectionModel(VirtualList<T> list) {
        this.list = list;
        selected = new LinkedHashMap<>();
    }

    @Override
    public Set<T> getSelectedItems() {
        /*
         * A new LinkedHashSet is created to avoid
         * ConcurrentModificationExceptions when changing the selection during
         * an iteration
         */
        return Collections
                .unmodifiableSet(new LinkedHashSet<>(selected.values()));
    }

    /**
     * Returns an unmodifiable view of the selected item ids.
     * <p>
     * Exposed to be overridden within subclasses.
     * <p>
     * The returned Set may be a direct view of the internal data structures of
     * this class. A defensive copy should be made by callers when iterating
     * over this Set and modifying the selection during iteration to avoid
     * ConcurrentModificationExceptions.
     *
     * @return An unmodifiable view of the selected item ids. Updates in the
     *         selection may or may not be directly reflected in the Set.
     */
    protected Set<Object> getSelectedItemIds() {
        return Collections.unmodifiableSet(this.selected.keySet());
    }

    @Override
    public Optional<T> getFirstSelectedItem() {
        return selected.values().stream().findFirst();
    }

    @Override
    public void select(T item) {
        if (isSelected(item)) {
            return;
        }
        Set<T> selected = new HashSet<>();
        if (item != null) {
            selected.add(item);
        }

        doUpdateSelection(selected, Collections.emptySet(), false);
    }

    @Override
    public void deselect(T item) {
        if (!isSelected(item)) {
            return;
        }
        Set<T> deselected = new HashSet<>();
        if (item != null) {
            deselected.add(item);
        }
        doUpdateSelection(Collections.emptySet(), deselected, false);
    }

    @Override
    public void selectAll() {
        updateSelection(
                (Set<T>) list.getDataCommunicator().getDataProvider()
                        .fetch(list.getDataCommunicator().buildQuery(0,
                                Integer.MAX_VALUE))
                        .collect(Collectors.toSet()),
                Collections.emptySet());
    }

    @Override
    public void deselectAll() {
        updateSelection(Collections.emptySet(), getSelectedItems());
    }

    @Override
    public void updateSelection(Set<T> addedItems, Set<T> removedItems) {
        Objects.requireNonNull(addedItems, "added items cannot be null");
        Objects.requireNonNull(removedItems, "removed items cannot be null");
        doUpdateSelection(addedItems, removedItems, false);
    }

    private Map<Object, T> mapItemsById(Set<T> items) {
        return items.stream().collect(LinkedHashMap::new,
                (map, item) -> map.put(this.getItemId(item), item),
                Map::putAll);
    }

    private void doUpdateSelection(Set<T> addedItems, Set<T> removedItems,
            boolean userOriginated) {
        Map<Object, T> addedItemsMap = mapItemsById(addedItems);
        Map<Object, T> removedItemsMap = mapItemsById(removedItems);
        addedItemsMap.keySet().stream().filter(removedItemsMap::containsKey)
                .collect(Collectors.toList()).forEach(key -> {
                    addedItemsMap.remove(key);
                    removedItemsMap.remove(key);
                });
        doUpdateSelection(addedItemsMap, removedItemsMap, userOriginated);
    }

    private void doUpdateSelection(Map<Object, T> addedItems,
            Map<Object, T> removedItems, boolean userOriginated) {
        if (selected.keySet().containsAll(addedItems.keySet()) && Collections
                .disjoint(selected.keySet(), removedItems.keySet())) {
            return;
        }

        Set<T> oldSelection = getSelectedItems();
        removedItems.keySet().forEach(selected::remove);
        selected.putAll(addedItems);

        ComponentUtil.fireEvent(list, new MultiSelectionEvent<>(list,
                asMultiSelect(), oldSelection, userOriginated));
    }

    @Override
    public boolean isSelected(T item) {
        return selected.containsKey(getItemId(item));
    }

    public void setSelectedItems(Set<T> items) {
        var oldValue = getSelectedItems();
        selected.clear();
        items.forEach(item -> selected.put(getItemId(item), item));

        // TODO: This should not be here
        ComponentUtil.fireEvent(list, new MultiSelectionEvent<>(list,
                asMultiSelect(), oldValue, true));

    }

    public MultiSelect<VirtualList<T>, T> asMultiSelect() {
        return new MultiSelect<VirtualList<T>, T>() {

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Registration addValueChangeListener(
                    ValueChangeListener<? super ComponentValueChangeEvent<VirtualList<T>, Set<T>>> listener) {
                Objects.requireNonNull(listener, "listener cannot be null");

                ComponentEventListener componentEventListener = event -> listener
                        .valueChanged(
                                (ComponentValueChangeEvent<VirtualList<T>, Set<T>>) event);

                return ComponentUtil.addListener(list,
                        MultiSelectionEvent.class, componentEventListener);
            }

            @Override
            public Registration addSelectionListener(
                    MultiSelectionListener<VirtualList<T>, T> listener) {
                return addMultiSelectionListener(listener);
            }

            @Override
            public void deselectAll() {
                VirtualListMultiSelectionModel.this.deselectAll();
            }

            @Override
            public void updateSelection(Set<T> addedItems,
                    Set<T> removedItems) {
                VirtualListMultiSelectionModel.this.updateSelection(addedItems,
                        removedItems);
            }

            @Override
            public Element getElement() {
                return list.getElement();
            }

            @Override
            public Set<T> getSelectedItems() {
                return VirtualListMultiSelectionModel.this.getSelectedItems();
            }
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Registration addSelectionListener(
            SelectionListener<VirtualList<T>, T> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        return ComponentUtil.addListener(list, MultiSelectionEvent.class,
                (ComponentEventListener) (event -> listener
                        .selectionChange((SelectionEvent) event)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addMultiSelectionListener(
            MultiSelectionListener<VirtualList<T>, T> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        return ComponentUtil.addListener(list, MultiSelectionEvent.class,
                (ComponentEventListener) (event -> listener
                        .selectionChange((MultiSelectionEvent) event)));
    }

    private Object getItemId(T item) {
        return list.getDataCommunicator().getDataProvider().getId(item);
    }
}
