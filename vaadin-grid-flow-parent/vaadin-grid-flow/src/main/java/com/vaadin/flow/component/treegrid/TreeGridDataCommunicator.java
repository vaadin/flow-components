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

    public TreeGridDataCommunicator(CompositeDataGenerator<T> dataGenerator,
            HierarchicalArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode,
                uniqueKeyProviderSupplier);
    }

    @Override
    public void reset() {
        rootCache = null;
        getKeyMapper().removeAll();
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
        getKeyMapper().refresh(item);
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
    public void confirmUpdate(int updateId) {
        // NO-OP
    }

    @Override
    public void confirmUpdate(int updateId, String parentKey) {
        // NO-OP
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

    protected void requestFlush(HierarchicalUpdate update) {
        // NO-OP
    }

    protected void requestFlush(HierarchicalCommunicationController<T> update) {
        // NO-OP
    }

    @Override
    protected void flush() {
        int start = requestedRange.getStart();
        int end = requestedRange.getEnd();

        if (rootCache == null) {
            rootCache = createRootCache(getHierarchyMapper().getRootSize());
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

    private RootCache<T> createRootCache(int size) {
        return new RootCache<>(size, getDataProvider()::getId) {
            @Override
            protected void removeItemContext(T item) {
                super.removeItemContext(item);

                getKeyMapper().remove(item);
                getDataGenerator().destroyData(item);
            }
        };
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
        private final RootCache<T> rootCache;
        private final Cache<T> parentCache;
        private final int parentIndex;
        private final int size;
        private final Map<Object, T> itemIdToItem = new HashMap<>();
        private final SortedMap<Integer, Object> indexToItemId = new TreeMap<>();
        private final SortedMap<Integer, Cache<T>> indexToCache = new TreeMap<>();

        protected Cache(RootCache<T> rootCache, Cache<T> parentCache, int parentIndex, int size) {
            this.rootCache = rootCache != null ? rootCache : (RootCache<T>) this;
            this.parentCache = parentCache;
            this.parentIndex = parentIndex;
            this.size = size;
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

    private static class RootCache<T> extends Cache<T> {
        private final ValueProvider<T, Object> itemIdProvider;
        private final Map<Object, ItemContext<T>> itemIdToContext = new HashMap<>();

        public static record ItemContext<T>(Object id, Cache<T> cache,
                int index) {
        }

        public static record FlatIndexContext<T>(Cache<T> cache, int index) {
        }

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

        protected void addItemContext(T item, Cache<T> cache, int index) {
            Object itemId = getItemId(item);
            itemIdToContext.put(itemId,
                    new ItemContext<>(itemId, cache, index));
        }

        protected void removeItemContext(T item) {
            Object itemId = getItemId(item);
            itemIdToContext.remove(itemId);
        }

        private Object getItemId(T item) {
            return itemIdProvider.apply(item);
        }
    }
}
