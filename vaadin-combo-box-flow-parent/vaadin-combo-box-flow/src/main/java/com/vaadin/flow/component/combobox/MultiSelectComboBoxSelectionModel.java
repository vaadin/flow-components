package com.vaadin.flow.component.combobox;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

class MultiSelectComboBoxSelectionModel<TItem> {
    private Map<Object, TItem> selection;
    private final SerializableFunction<TItem, Object> itemIdProvider;
    private final SerializableConsumer<Set<TItem>> selectionChangeHandler;

    MultiSelectComboBoxSelectionModel(
            SerializableFunction<TItem, Object> itemIdProvider,
            SerializableConsumer<Set<TItem>> selectionChangeHandler) {
        this.selection = new LinkedHashMap<>();
        this.itemIdProvider = itemIdProvider;
        this.selectionChangeHandler = selectionChangeHandler;
    }

    Set<TItem> getSelectedItems() {
        return Collections
                .unmodifiableSet(new LinkedHashSet<>(selection.values()));
    }

    void setSelectedItems(Set<TItem> items) {
        Map<Object, TItem> newSelectionMap = mapItemsById(items);
        if (!newSelectionMap.keySet().equals(selection.keySet())) {
            selection = mapItemsById(items);
            notifySelectionChange();
        }
    }

    boolean isSelected(TItem item) {
        return selection.containsKey(itemIdProvider.apply(item));
    }

    void updateSelection(Set<TItem> addedItems, Set<TItem> removedItems) {
        doUpdateSelection(mapItemsById(addedItems), mapItemsById(removedItems));
    }

    private void doUpdateSelection(Map<Object, TItem> addedItems,
            Map<Object, TItem> removedItems) {
        // Skip if selection already contains all added items, and excludes all
        // removed items
        if (selection.keySet().containsAll(addedItems.keySet()) && Collections
                .disjoint(selection.keySet(), removedItems.keySet())) {
            return;
        }
        // Remove items
        new LinkedHashMap<>(removedItems).forEach(selection::remove);
        // Add items
        selection.putAll(addedItems);
        // Notify change
        notifySelectionChange();
    }

    private void notifySelectionChange() {
        selectionChangeHandler.accept(Collections
                .unmodifiableSet(new LinkedHashSet<>(selection.values())));
    }

    private Map<Object, TItem> mapItemsById(Set<TItem> items) {
        return items.stream().collect(LinkedHashMap::new,
                (map, item) -> map.put(itemIdProvider.apply(item), item),
                Map::putAll);
    }
}
