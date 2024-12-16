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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater.HierarchicalUpdate;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalCommunicationController;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchyMapper;
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
    private RootCache<T> rootCache;
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
            rootCache = new RootCache<>(getHierarchyMapper().getRootSize());
        }

        List<T> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            var context = rootCache.getFlatIndexContext(i);
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

        Update update = getArrayUpdater().startUpdate(flatSize);
        update.clear(0, start);
        update.clear(end, flatSize - end);
        update.set(start, result.stream().map(this::generateItemJson).toList());
        update.commit(nextUpdateId++);
    }

    private JsonValue generateItemJson(T item) {
        JsonObject json = Json.createObject();
        json.put("key", getKeyMapper().key(item));
        getDataGenerator().generateData(item, json);
        return json;
    }

    protected <F> HierarchyMapper<T, F> createHierarchyMapper(
            HierarchicalDataProvider<T, F> dataProvider) {
        return new HierarchyMapper<>(dataProvider) {
            @Override
            public int getDepth(T item) {
                var itemContext = rootCache.getItemContext(item);
                if (itemContext == null) {
                    return -1;
                }
                return itemContext.cache.getDepth();
            }

            @Override
            protected T getParentOfItem(T item) {
                var itemContext = rootCache.getItemContext(item);
                if (itemContext == null) {
                    return null;
                }
                return itemContext.cache.getParentItem();
            }

            @Override
            protected void registerChildren(T parent, List<T> childList) {
                // NO-OP
            }

            @Override
            protected void removeChildren(Object id) {
                // NO-OP
            }
        };
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

        public int getDepth() {
            return parentCache == null ? 0 : parentCache.getDepth() + 1;
        }

        public boolean hasItem(int index) {
            return items.containsKey(index);
        }

        public T getItem(int index) {
            return items.get(index);
        }

        public void setItems(int startIndex, List<T> itemsToSet) {
            RootCache<T> rootCache = getRootCache();

            for (int i = 0; i < itemsToSet.size(); i++) {
                T item = itemsToSet.get(i);
                items.put(startIndex + i, item);
                rootCache.itemToCache.put(item, this);
            }
        }

        public boolean hasCache(int index) {
            return caches.containsKey(index);
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

        private RootCache<T> getRootCache() {
            if (parentCache != null) {
                return parentCache.getRootCache();
            }
            return (RootCache<T>) this;
        }
    }

    private static class RootCache<T> extends Cache<T> {
        private Map<T, Cache<T>> itemToCache = new WeakHashMap<>();

        public static record ItemContext<T>(Cache<T> cache, int index) {}
        public static record FlatIndexContext<T>(Cache<T> cache, int index) {}

        public RootCache(int size) {
            super(null, -1, size);
        }

        public ItemContext<T> getItemContext(T item) {
            Cache<T> cache = itemToCache.get(item);
            if (cache == null) {
                return null;
            }
            return new ItemContext<>(cache, cache.items.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(item))
                    .map(Entry::getKey).findFirst().orElse(-1));
        }

        public FlatIndexContext<T> getFlatIndexContext(int flatIndex) {
            return getFlatIndexContext(this, flatIndex);
        }

        private FlatIndexContext<T> getFlatIndexContext(Cache<T> cache, int flatIndex) {
            int index = flatIndex;

            var subCaches = cache.caches.entrySet();
            for (Entry<Integer, Cache<T>> entry : subCaches) {
                var subCacheIndex = entry.getKey();
                var subCache = entry.getValue();

                if (index <= subCacheIndex) {
                    break;
                }

                if (index <= subCacheIndex + subCache.getFlatSize()) {
                    return getFlatIndexContext(subCache, index - subCacheIndex - 1);
                }

                index -= subCache.getFlatSize();
            }

            if (index >= cache.getSize()) {
                return null;
            }

            return new FlatIndexContext<>(cache, index);
        }
    }
}
