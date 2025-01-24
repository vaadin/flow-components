package com.vaadin.flow.component.treegrid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vaadin.flow.function.SerializablePredicate;

class Cache<T> {
    private final RootCache<T> rootCache;
    private final Cache<T> parentCache;
    private final int parentIndex;
    private final int size;

    final Map<Object, T> itemIdToItem = new HashMap<>();
    final SortedMap<Integer, Object> indexToItemId = new TreeMap<>();
    final SortedMap<Integer, Cache<T>> indexToCache = new TreeMap<>();

    protected Cache(RootCache<T> rootCache, Cache<T> parentCache,
            int parentIndex, int size) {
        this.rootCache = rootCache != null ? rootCache
                : (RootCache<T>) this;
        this.parentCache = parentCache;
        this.parentIndex = parentIndex;
        this.size = size;
    }

    public T getParentItem() {
        return parentCache != null ? parentCache.getItem(parentIndex)
                : null;
    }

    public int getDepth() {
        return parentCache != null ? parentCache.getDepth() + 1 : 0;
    }

    public int getSize() {
        return size;
    }

    public int getFlatSize() {
        return size + indexToCache.values().stream()
                .mapToInt(Cache::getFlatSize).sum();
    }

    public boolean hasItem(int index) {
        return indexToItemId.containsKey(index);
    }

    public T getItem(int index) {
        Object itemId = indexToItemId.get(index);
        return itemIdToItem.get(itemId);
    }

    public void refreshItem(T item) {
        Object itemId = rootCache.getItemId(item);
        itemIdToItem.replace(itemId, item);
    }

    public void setItems(int startIndex, List<T> items) {
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            var itemId = rootCache.getItemId(item);
            var index = startIndex + i;

            indexToItemId.put(index, itemId);
            itemIdToItem.put(itemId, item);

            rootCache.addItemContext(item, this, index);
        }
    }

    public void clear() {
        indexToCache.values().forEach((cache) -> {
            cache.clear();
        });

        indexToItemId.values().forEach((itemId) -> {
            T item = itemIdToItem.get(itemId);
            rootCache.removeItemContext(item);
        });

        indexToCache.clear();
        indexToItemId.clear();
        itemIdToItem.clear();
    }

    public boolean hasCache(int index) {
        return indexToCache.containsKey(index);
    }

    public Cache<T> createCache(int index, int size) {
        Cache<T> cache = new Cache<>(rootCache, this, index, size);
        indexToCache.put(index, cache);
        return cache;
    }

    public void removeDescendantCacheIf(
            SerializablePredicate<Cache<T>> predicate) {
        indexToCache.values().removeIf(cache -> {
            if (predicate.test(cache)) {
                cache.clear();
                return true;
            }
            cache.removeDescendantCacheIf(predicate);
            return false;
        });
    }
}
