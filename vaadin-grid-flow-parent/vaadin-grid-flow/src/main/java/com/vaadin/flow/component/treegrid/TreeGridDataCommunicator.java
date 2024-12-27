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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater.HierarchicalUpdate;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalCommunicationController;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchyMapper;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
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
    private RootCache<T> rootCache;
    private Range requestedRange;
    private int nextUpdateId = 0;
    private int lastGeneratedKey = 0;

    public TreeGridDataCommunicator(CompositeDataGenerator<T> dataGenerator,
            HierarchicalArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode,
                uniqueKeyProviderSupplier);
    }

    protected void requestFlush(HierarchicalUpdate update) {
        // NO-OP
    }

    protected void requestFlush(HierarchicalCommunicationController<T> update) {
        // NO-OP
    }

    @Override
    public void reset() {
        rootCache = null;
        getDataGenerator().destroyAllData();
        requestFlush();
    }

    @Override
    protected void handleDataRefreshEvent(
            DataChangeEvent.DataRefreshEvent<T> event) {
        refresh(event.getItem());
    }

    @Override
    public void refresh(T item) {
        Objects.requireNonNull(item,
                "DataCommunicator can not refresh null object");
        getDataGenerator().refreshData(item);

        var itemContext = rootCache.getItemContext(item);
        if (itemContext != null) {
            itemContext.cache.refreshItem(item);
        }

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
        List<T> expandedItems = items.stream()
                .filter(getHierarchyMapper()::expand).toList();
        requestFlush();
        return expandedItems;
    }

    @Override
    public void collapse(T item) {
        collapse(Arrays.asList(item));
    }

    @Override
    public Collection<T> collapse(Collection<T> items) {
        List<T> collapsedItems = items.stream()
                .filter(getHierarchyMapper()::collapse).toList();

        List<Object> collapsedItemIds = collapsedItems.stream()
                .map(getDataProvider()::getId).toList();
        rootCache.removeDescendantCacheIf((cache) -> {
            Object parentItemId = getDataProvider()
                    .getId(cache.getParentItem());
            return collapsedItemIds.contains(parentItemId);
        });

        requestFlush();
        return collapsedItems;
    }

    @Override
    protected void flush() {
        int start = requestedRange.getStart();
        int end = requestedRange.getEnd();

        if (rootCache == null) {
            rootCache = new RootCache<>(getHierarchyMapper().getRootSize(),
                    getDataProvider()::getId, this::generateItemKey);
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

    private String generateItemKey(T item) {
        var uniqueKeyProvider = getUniqueKeyProvider();
        if (uniqueKeyProvider != null) {
            return uniqueKeyProvider.apply(item);
        }

        return String.valueOf(lastGeneratedKey++);
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
        private final Cache<T> parentCache;
        private final int parentIndex;
        private final int size;
        private Map<Object, T> itemIdToItem = new HashMap<>();
        private SortedMap<Integer, Object> indexToItemId = new TreeMap<>();
        private SortedMap<Integer, Cache<T>> indexToCache = new TreeMap<>();

        protected Cache(Cache<T> parentCache, int parentIndex, int size) {
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

        public int getDepth() {
            if (parentCache == null) {
                return 0;
            }
            return parentCache.getDepth() + 1;
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
            Object itemId = getRootCache().getItemId(item);
            itemIdToItem.replace(itemId, item);
        }

        public void clear() {
            RootCache<T> rootCache = getRootCache();

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

        public void setItems(int startIndex, List<T> items) {
            RootCache<T> rootCache = getRootCache();

            for (int i = 0; i < items.size(); i++) {
                var item = items.get(i);
                var itemId = rootCache.getItemId(item);
                var index = startIndex + i;

                indexToItemId.put(index, itemId);
                itemIdToItem.put(itemId, item);

                rootCache.createItemContext(item, this, index);
            }
        }

        public boolean hasCache(int index) {
            return indexToCache.containsKey(index);
        }

        public Cache<T> createCache(int index, int size) {
            Cache<T> cache = new Cache<>(this, index, size);
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

        private RootCache<T> getRootCache() {
            if (parentCache != null) {
                return parentCache.getRootCache();
            }
            return (RootCache<T>) this;
        }
    }

    private static class RootCache<T> extends Cache<T> {
        private final ValueProvider<T, Object> itemIdProvider;
        private final SerializableFunction<T, String> itemKeyGenerator;
        private Map<Object, ItemContext<T>> itemIdToContext = new HashMap<>();
        private Map<String, ItemContext<T>> itemKeyToContext = new HashMap<>();

        // @formatter:off
        public static record ItemContext<T>(
                Object id, String key, Cache<T> cache, int index) {}

        public static record FlatIndexContext<T>(
                Cache<T> cache, int index) {}
        // @formatter:off

        public RootCache(int size, ValueProvider<T, Object> itemIdProvider, SerializableFunction<T, String> itemKeyGenerator) {
            super(null, -1, size);
            this.itemIdProvider = itemIdProvider;
            this.itemKeyGenerator = itemKeyGenerator;
        }

        public ItemContext<T> getItemContext(T item) {
            Object itemId = getItemId(item);
            return itemIdToContext.get(itemId);
        }

        public FlatIndexContext<T> getFlatIndexContext(int flatIndex) {
            return getFlatIndexContext(this, flatIndex);
        }
        private FlatIndexContext<T> getFlatIndexContext(Cache<T> cache,
                int flatIndex) {
            int index = flatIndex;

            for (Entry<Integer, Cache<T>> entry : cache.indexToCache.entrySet()) {
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

        private void createItemContext(T item, Cache<T> cache, int index) {
            Object itemId = getItemId(item);
            String itemKey = generateItemKey(item);

            ItemContext<T> itemContext = new ItemContext<>(itemId, itemKey, cache, index);
            itemIdToContext.put(itemId, itemContext);
            itemKeyToContext.put(itemKey, itemContext);
        }

        private void removeItemContext(T item) {
            Object itemId = getItemId(item);
            ItemContext<T> itemContext = itemIdToContext.remove(itemId);
            itemKeyToContext.remove(itemContext.key);
        }

        private Object getItemId(T item) {
            return itemIdProvider.apply(item);
        }

        private String generateItemKey(T item) {
            return itemKeyGenerator.apply(item);
        }
    }
}
