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
package com.vaadin.flow.component.treegrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater.HierarchicalUpdate;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalCommunicationController;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.Range;
import com.vaadin.flow.internal.StateNode;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

public class TreeGridDataCommunicator<T>
        extends HierarchicalDataCommunicator<T> {
    private int nextUpdateId = 0;
    private Cache<T> rootCache;
    private Range requestedRange;

    public TreeGridDataCommunicator(CompositeDataGenerator<T> dataGenerator,
            HierarchicalArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode,
                uniqueKeyProviderSupplier);
    }

    protected void requestFlush(HierarchicalUpdate update) {
    }

    protected void requestFlush(HierarchicalCommunicationController<T> update) {
    }

    @Override
    public void reset() {
        rootCache = null;
        getDataGenerator().destroyAllData();
        requestFlush();
    }

    @Override
    public void setRequestedRange(int start, int length) {
        requestedRange = Range.withLength(start, length);
        requestFlush();
    }

    @Override
    public void expand(T item) {
        expand(Arrays.asList(item));
    }

    @Override
    public Collection<T> expand(Collection<T> items) {
        List<T> expandedItems = new ArrayList<>();
        items.forEach(item -> {
            if (getHierarchyMapper().expand(item)) {
                expandedItems.add(item);
            }
        });
        requestFlush();
        return expandedItems;
    }

    @Override
    public void collapse(T item) {
        collapse(Arrays.asList(item));
    }

    @Override
    public Collection<T> collapse(Collection<T> items) {
        List<T> collapsedItems = new ArrayList<>();
        items.forEach(item -> {
            if (getHierarchyMapper().collapse(item)) {
                collapsedItems.add(item);
            }
        });
        rootCache.removeDescendantCacheIf(
                cache -> collapsedItems.contains(cache.getParentItem()));
        requestFlush();
        return collapsedItems;
    }

    @Override
    protected void flush() {
        int start = requestedRange.getStart();
        int end = requestedRange.getEnd();

        if (rootCache == null) {
            rootCache = new Cache<>(null, -1,
                    getHierarchyMapper().getRootSize());
        }

        List<T> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            FlatIndexContext<T> context = getFlatIndexContext(i);
            if (context == null) {
                end = i;
                break;
            }
            var cache = context.cache;
            var index = context.index;

            if (!cache.hasItem(index)) {
                List<T> childItems = getHierarchyMapper()
                        .fetchChildItems(cache.getParentItem(),
                                Range.between(index, end))
                        .toList();
                cache.setItems(index, childItems);
            }

            T item = cache.getItem(index);
            if (isExpanded(item) && !cache.hasCache(index)) {
                int childCount = getHierarchyMapper().countChildItems(item);
                cache.createCache(index, childCount);
            }

            result.add(item);
        }

        int flatSize = rootCache.getFlatSize();

        // Send the data to the client side
        Update update = getArrayUpdater().startUpdate(flatSize);
        update.clear(0, start);
        update.clear(end, flatSize - end);
        update.set(start, result.stream().map(this::generateItemJson).toList());
        update.commit(nextUpdateId++);

        // Restrict the requested range to the actual range of items
        // that were fetched in case it's requested again in the future.
        requestedRange = requestedRange.restrictTo(Range.between(start, end));
    }

    private FlatIndexContext<T> getFlatIndexContext(int flatIndex) {
        return getFlatIndexContext(flatIndex, rootCache);
    }

    private FlatIndexContext<T> getFlatIndexContext(int flatIndex,
            Cache<T> cache) {
        int index = flatIndex;

        for (int cacheIndex : cache.getCacheIndexes()) {
            Cache<T> childCache = cache.getCache(cacheIndex);

            if (index <= cacheIndex) {
                break;
            }

            if (index <= cacheIndex + childCache.getFlatSize()) {
                return getFlatIndexContext(index - cacheIndex - 1, childCache);
            }

            index -= childCache.getFlatSize();
        }

        if (index >= cache.getSize()) {
            return null;
        }

        return new FlatIndexContext<>(cache, index);
    }

    private JsonValue generateItemJson(T item) {
        JsonObject json = Json.createObject();
        json.put("key", getKeyMapper().key(item));
        getDataGenerator().generateData(item, json);
        return json;
    }

    private static record FlatIndexContext<T>(Cache<T> cache,
    int index)
    {
    }

    private static class Cache<T> {
        private Cache<T> parentCache;
        private int parentIndex;
        private int size;
        private SortedMap<Integer, T> items = new TreeMap<>();
        private SortedMap<Integer, Cache<T>> caches = new TreeMap<>();

        public Cache(Cache<T> parentCache, int parentIndex, int size) {
            this.parentCache = parentCache;
            this.parentIndex = parentIndex;
            this.size = size;
        }

        public T getParentItem() {
            if (parentCache == null) {
                return null;
            }
            return parentCache.getItem(parentIndex);
        }

        public int getSize() {
            return size;
        }

        public int getFlatSize() {
            return size + caches.values().stream().mapToInt(Cache::getFlatSize)
                    .sum();
        }

        public boolean hasItem(int index) {
            return items.containsKey(index);
        }

        public T getItem(int index) {
            return items.get(index);
        }

        public void setItems(int startIndex, List<T> itemsToSet) {
            for (int i = 0; i < itemsToSet.size(); i++) {
                items.put(startIndex + i, itemsToSet.get(i));
            }
        }

        public Set<Integer> getCacheIndexes() {
            return caches.keySet();
        }

        public boolean hasCache(int index) {
            return caches.containsKey(index);
        }

        public Cache<T> getCache(int index) {
            return caches.get(index);
        }

        public Cache<T> createCache(int index, int size) {
            Cache<T> cache = new Cache<>(this, index, size);
            caches.put(index, cache);
            return cache;
        }

        public void removeDescendantCacheIf(
                SerializablePredicate<Cache<T>> predicate) {
            caches.values().removeIf(cache -> {
                if (predicate.test(cache)) {
                    return true;
                }
                cache.removeDescendantCacheIf(predicate);
                return false;
            });
        }
    }
}
