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
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater.HierarchicalUpdate;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalCommunicationController;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
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
    private Cache<T> rootCache;

    private ArrayUpdater arrayUpdater;
    private CompositeDataGenerator<T> dataGenerator;
    private int nextUpdateId = 0;
    private Range requestedRange;

    public TreeGridDataCommunicator(CompositeDataGenerator<T> dataGenerator,
            HierarchicalArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode,
                uniqueKeyProviderSupplier);
        this.arrayUpdater = arrayUpdater;
        this.dataGenerator = dataGenerator;
    }

    protected void requestFlush(HierarchicalUpdate update) {
    }

    protected void requestFlush(HierarchicalCommunicationController<T> update) {
    }

    protected void flush() {
        int start = requestedRange.getStart();
        int end = requestedRange.getEnd();
        int length = requestedRange.length();

        List<T> result = new ArrayList<>();

        if (rootCache == null) {
            rootCache = new Cache<>(null, -1, countChildItems(null));
        }

        for (int i = start; i < end; i++) {
            FlatIndexContext<T> context = getFlatIndexContext(i);
            int index = context.index;
            Cache<T> cache = context.cache;

            if (!cache.hasItem(index)) {
                // TODO: Optimize length calculation
                cache.setItems(index, fetchChildItems(cache.getParentItem(),
                        Range.withLength(index, length)));
            }

            T item = cache.getItem(index);
            if (isExpanded(item) && !cache.hasCache(index)) {
                cache.createCache(index, countChildItems(item));
            }

            result.add(item);
        }

        Update update = arrayUpdater.startUpdate(rootCache.getFlatSize());
        update.set(start, result.stream().map(this::generateJson).toList());
        update.commit(nextUpdateId++);
    }

    public void setRequestedRange(int start, int length) {
        requestedRange = Range.withLength(start, length);
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

        return new FlatIndexContext<>(cache, index);
    }

    private JsonValue generateJson(T item) {
        JsonObject json = Json.createObject();
        json.put("key", getKeyMapper().key(item));
        dataGenerator.generateData(item, json);
        return json;
    }

    private List<T> fetchChildItems(T parentItem, Range range) {
        return getHierarchyMapper().fetchChildItems(parentItem, range).toList();
    }

    private int countChildItems(T parentItem) {
        return getHierarchyMapper().countChildItems(parentItem);
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
    }
}
