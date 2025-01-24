package com.vaadin.flow.component.treegrid;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.flow.function.ValueProvider;

class RootCache<T> extends Cache<T> {
    private final ValueProvider<T, Object> itemIdProvider;
    private final Map<Object, ItemContext<T>> itemIdToContext = new HashMap<>();

    public RootCache(int size, ValueProvider<T, Object> itemIdProvider) {
        super(null, null, -1, size);
        this.itemIdProvider = itemIdProvider;
    }

    public FlatIndexContext<T> getFlatIndexContext(int flatIndex) {
        return getFlatIndexContext(this, flatIndex);
    }

    private FlatIndexContext<T> getFlatIndexContext(Cache<T> cache,
            int flatIndex) {
        int index = flatIndex;

        for (Entry<Integer, Cache<T>> entry : cache.indexToCache
                .entrySet()) {
            var subCacheIndex = entry.getKey();
            var subCache = entry.getValue();

            if (index <= subCacheIndex) {
                break;
            }
            if (index <= subCacheIndex + subCache.getFlatSize()) {
                return getFlatIndexContext(subCache,
                        index - subCacheIndex - 1);
            }
            index -= subCache.getFlatSize();
        }

        if (index >= cache.getSize()) {
            return null;
        }

        return new FlatIndexContext<>(cache, index);
    }

    public ItemContext<T> getItemContext(T item) {
        Object itemId = getItemId(item);
        return itemIdToContext.get(itemId);
    }

    void addItemContext(T item, Cache<T> cache, int index) {
        Object itemId = getItemId(item);
        itemIdToContext.put(itemId,
                new ItemContext<>(itemId, cache, index));
    }

    void removeItemContext(T item) {
        Object itemId = getItemId(item);
        itemIdToContext.remove(itemId);
    }

    Object getItemId(T item) {
        return itemIdProvider.apply(item);
    }
}
