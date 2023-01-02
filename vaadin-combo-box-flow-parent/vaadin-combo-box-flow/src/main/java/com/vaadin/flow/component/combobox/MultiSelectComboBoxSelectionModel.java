/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.function.SerializableFunction;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Selection model for the {@link MultiSelectComboBox}, which implements the
 * multi-select logic for the component. The model keeps track of the currently
 * selected items, and allows adding and removing items from the selection,
 * while ensuring that items can only be selected once (no duplicates). For
 * identifying items, the model uses an identity provider, which is supposed to
 * return a unique ID for each item. The identity provider implementation should
 * be based on the identity provider used by the Flow data classes, for example
 * {@link com.vaadin.flow.data.provider.DataProvider#getId(Object)}.
 *
 * @param <TItem>
 *            the type of the item selectable from the combo box
 */
class MultiSelectComboBoxSelectionModel<TItem> implements Serializable {
    private Map<Object, TItem> selection;
    private SerializableFunction<TItem, Object> identityProvider;

    MultiSelectComboBoxSelectionModel(
            SerializableFunction<TItem, Object> identityProvider) {
        this.selection = new LinkedHashMap<>();
        this.identityProvider = identityProvider;
    }

    /**
     * Gets the currently selected items as an unmodifiable set.
     */
    Set<TItem> getSelectedItems() {
        return Collections
                .unmodifiableSet(new LinkedHashSet<>(selection.values()));
    }

    /**
     * Sets the selected items, for example when the value of the combo box
     * changes. The selection will only be changed if the set of selected items
     * is different from the current selection, where the identity of each item
     * is checked with the identity provider. Returns {@code true} if the
     * selection was changed, {@code false} otherwise.
     *
     * @param items
     *            the new selection
     * @return {@code true} if the selection was changed, {@code false}
     *         otherwise
     */
    boolean setSelectedItems(Set<TItem> items) {
        Map<Object, TItem> newSelectionMap = mapItemsById(items);
        if (!newSelectionMap.keySet().equals(selection.keySet())) {
            selection = newSelectionMap;
            return true;
        }
        return false;
    }

    /**
     * Updates the identity provider used to identify items, and guarantees that
     * the selection is kept after the identity change.
     *
     * @param identityProvider
     *            the new identity provider
     */
    public void setIdentityProvider(
            SerializableFunction<TItem, Object> identityProvider) {
        this.identityProvider = identityProvider;
        // Refresh selection map with new item IDs
        selection = mapItemsById(new LinkedHashSet<>(selection.values()));
    }

    /**
     * Checks whether the item is currently selected or not, by comparing the ID
     * of the item that is provided by the identity provider
     *
     * @param item
     *            the item to check
     * @return {@code true} if the item is selected, {@code false} otherwise
     */
    boolean isSelected(TItem item) {
        return selection.containsKey(identityProvider.apply(item));
    }

    /**
     * Updates the selection, by adding and removing the specified items. If an
     * item is already selected, it is not added again. Likewise, if an item is
     * not selected, then nothing is removed. If there are no items to add, and
     * no items to remove, then the selection is not changed. Returns
     * {@code true} if the selection was changed, {@code false} otherwise.
     *
     * @param addedItems
     *            the items to add to the selection
     * @param removedItems
     *            the items to remove from the selection
     * @return {@code true} if the selection was changed, {@code false}
     *         otherwise
     */
    boolean updateSelection(Set<TItem> addedItems, Set<TItem> removedItems) {
        return doUpdateSelection(mapItemsById(addedItems),
                mapItemsById(removedItems));
    }

    private boolean doUpdateSelection(Map<Object, TItem> addedItems,
            Map<Object, TItem> removedItems) {
        // Skip if selection already contains all added items, and excludes all
        // removed items
        if (selection.keySet().containsAll(addedItems.keySet()) && Collections
                .disjoint(selection.keySet(), removedItems.keySet())) {
            return false;
        }
        // Remove items
        new LinkedHashMap<>(removedItems).forEach(selection::remove);
        // Add items
        selection.putAll(addedItems);

        return true;
    }

    private Map<Object, TItem> mapItemsById(Set<TItem> items) {
        return items.stream().collect(LinkedHashMap::new,
                (map, item) -> map.put(identityProvider.apply(item), item),
                Map::putAll);
    }
}
