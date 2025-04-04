/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid;

import java.util.Arrays;
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
        this.rootCache = rootCache != null ? rootCache : (RootCache<T>) this;
        this.parentCache = parentCache;
        this.parentIndex = parentIndex;
        this.size = size;
    }

    public int getFlatIndex(int localIndex) {
        // const clampedIndex = Math.max(0, Math.min(this.size - 1, index));

        // return this.subCaches.reduce((prev, subCache) => {
        // const index = subCache.parentCacheIndex;
        // return clampedIndex > index ? prev + subCache.flatSize : prev;
        // }, clampedIndex);

        int clampedIndex = Math.max(0, Math.min(size - 1, localIndex));
        return indexToCache.entrySet().stream().reduce(clampedIndex,
                (prev, entry) -> {
                    var index = entry.getKey();
                    var subCache = entry.getValue();
                    return clampedIndex > index ? prev + subCache.getFlatSize()
                            : prev;
                }, Integer::sum);
    }

    public int getFlatIndexByPath(int flatIndex, int... indexes) {
        int levelIndex = indexes[0];

        int flatIndexOnLevel = getFlatIndex(levelIndex);
        var subCache = indexToCache.get(levelIndex);
        if (subCache != null && subCache.getFlatSize() > 0
                && indexes.length > 1) {
            return subCache.getFlatIndexByPath(flatIndex + flatIndexOnLevel + 1,
                    Arrays.copyOfRange(indexes, 1, indexes.length));
        }

        return flatIndex + flatIndexOnLevel;

        // if (levelIndex === Infinity) {
        // // Treat Infinity as the last index on the level
        // levelIndex = cache.size - 1;
        // }

        // const flatIndexOnLevel = cache.getFlatIndex(levelIndex);
        // const subCache = cache.getSubCache(levelIndex);
        // if (subCache && subCache.flatSize > 0 && subIndexes.length) {
        // return getFlatIndexByPath(subCache, subIndexes, flatIndex +
        // flatIndexOnLevel + 1);
        // }
        // return flatIndex + flatIndexOnLevel;
    }

    public T getParentItem() {
        return parentCache != null ? parentCache.getItem(parentIndex) : null;
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
