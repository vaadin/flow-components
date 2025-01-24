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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
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

public class TreeGridDataCommunicator<T> extends DataCommunicator<T> {
    private final Set<Object> expandedItemIds = new HashSet<>();
    private final ArrayUpdater arrayUpdater;
    private final DataGenerator<T> dataGenerator;
    private final SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier;

    private RootCache<T> rootCache;
    private Range requestedRange;
    private int nextUpdateId = 0;

    public TreeGridDataCommunicator(CompositeDataGenerator<T> dataGenerator,
            ArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode);
        this.arrayUpdater = arrayUpdater;
        this.dataGenerator = dataGenerator;
        this.uniqueKeyProviderSupplier = uniqueKeyProviderSupplier;

        KeyMapper<T> keyMapper = createKeyMapper();
        setKeyMapper(keyMapper);

        setDataProvider(new TreeDataProvider<>(new TreeData<>()), null);
    }

    /** @see DataCommunicator#getDataProvider() */
    @Override
    public HierarchicalDataProvider<T, ?> getDataProvider() {
        return (HierarchicalDataProvider<T, ?>) super.getDataProvider();
    }

    /** @see DataCommunicator#getDataProviderSize() */
    @Override
    public int getDataProviderSize() {
        return getDataProviderChildCount(null);
    }

    /** @see DataCommunicator#reset() */
    @Override
    public void reset() {
        if (rootCache != null) {
            getKeyMapper().removeAll();
            dataGenerator.destroyAllData();
        }

        rootCache = null;
        requestFlush();
    }

    /** @see DataCommunicator#refresh(T) */
    @Override
    public void refresh(T item) {
        Objects.requireNonNull(item,
                "DataCommunicator can not refresh null object");
        getKeyMapper().refresh(item);
        dataGenerator.refreshData(item);

        var itemContext = rootCache.getItemContext(item);
        if (itemContext != null) {
            itemContext.cache.refreshItem(item);
        }

        requestFlush();
    }

    /** @see DataCommunicator#setRequestedRange(int, int) */
    @Override
    public void setRequestedRange(int start, int length) {
        requestedRange = Range.withLength(start, length);
        requestFlush();
    }

    /** @see DataCommunicator#confirmUpdate() */
    @Override
    public void confirmUpdate(int updateId) {
        // NO-OP
    }

    /** @see DataCommunicator#flush() */
    @Override
    protected void flush() {
        int start = requestedRange.getStart();
        int end = requestedRange.getEnd();

        if (rootCache == null) {
            rootCache = createRootCache(getDataProviderChildCount(null));
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
                List<T> childItems = fetchDataProviderChildren(
                        cache.getParentItem(), Range.between(index, end))
                        .toList();
                cache.setItems(index, childItems);
            }

            T item = cache.getItem(index);
            if (isExpanded(item) && !cache.hasCache(index)) {
                int childCount = getDataProviderChildCount(item);
                cache.createCache(index, childCount);
            }

            result.add(item);
        }

        int flatSize = rootCache.getFlatSize();

        Update update = arrayUpdater.startUpdate(flatSize);
        update.clear(0, start);
        update.clear(end, flatSize - end);
        update.set(start, result.stream().map(this::generateItemJson).toList());
        update.commit(nextUpdateId++);
    }

    /** @see HierarchicalDataCommunicator#hasChildren(T) */
    public boolean hasChildren(T item) {
        return getDataProvider().hasChildren(item);
    }

    /** @see HierarchicalDataCommunicator#isExpanded(T) */
    public boolean isExpanded(T item) {
        if (item == null) {
            // Root nodes are always visible.
            return true;
        }
        return expandedItemIds.contains(getDataProvider().getId(item));
    }

    /** @see HierarchicalDataCommunicator#expand(T item) */
    public void expand(T item) {
        expand(Arrays.asList(item));
    }

    /** @see HierarchicalDataCommunicator#expand(Collection) */
    public Collection<T> expand(Collection<T> items) {
        List<T> expandedItems = items.stream().filter(item -> {
            if (!hasChildren(item)) {
                return false;
            }

            return expandedItemIds.add(getDataProvider().getId(item));
        }).toList();

        requestFlush();
        return expandedItems;
    }

    /** @see HierarchicalDataCommunicator#collapse(T) */
    public void collapse(T item) {
        collapse(Arrays.asList(item));
    }

    /** @see HierarchicalDataCommunicator#collapse(Collection) */
    public Collection<T> collapse(Collection<T> items) {
        List<T> collapsedItems = items.stream().filter(
                item -> expandedItemIds.remove(getDataProvider().getId(item)))
                .toList();

        rootCache.removeDescendantCacheIf(
                (cache) -> !isExpanded(cache.getParentItem()));

        requestFlush();
        return collapsedItems;
    }

    /** @see HierarchicalDataCommunicator#getParentItem(T) */
    public T getParentItem(T item) {
        var itemContext = rootCache.getItemContext(item);
        if (itemContext == null) {
            return null;
        }
        return itemContext.cache.getParentItem();
    }

    /** @see HierarchicalDataCommunicator#getDepth(T) */
    public int getDepth(T item) {
        var itemContext = rootCache.getItemContext(item);
        if (itemContext == null) {
            return -1;
        }
        return itemContext.cache.getDepth();
    }

    private JsonValue generateItemJson(T item) {
        JsonObject json = Json.createObject();
        json.put("key", getKeyMapper().key(item));
        dataGenerator.generateData(item, json);
        return json;
    }

    private KeyMapper<T> createKeyMapper() {
        return new KeyMapper<T>() {
            private T object;

            @Override
            public String key(T o) {
                this.object = o;
                try {
                    return super.key(o);
                } finally {
                    this.object = null;
                }
            }

            @Override
            protected String createKey() {
                return Optional.ofNullable(uniqueKeyProviderSupplier.get())
                        .map(provider -> provider.apply(object))
                        .orElse(super.createKey());
            }
        };
    }

    private RootCache<T> createRootCache(int size) {
        return new RootCache<>(size, getDataProvider()::getId) {
            @Override
            protected void removeItemContext(T item) {
                super.removeItemContext(item);

                getKeyMapper().remove(item);
                dataGenerator.destroyData(item);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Stream<T> fetchDataProviderChildren(T parent, Range range) {
        HierarchicalQuery<T, Object> query = new HierarchicalQuery<>(
                range.getStart(), range.length(), getBackEndSorting(),
                getInMemorySorting(), getFilter(), parent);

        return ((HierarchicalDataProvider<T, Object>) getDataProvider())
                .fetchChildren(query);
    }

    @SuppressWarnings("unchecked")
    private int getDataProviderChildCount(T parent) {
        HierarchicalQuery<T, Object> query = new HierarchicalQuery<>(
                getFilter(), parent);

        return ((HierarchicalDataProvider<T, Object>) getDataProvider())
                .getChildCount(query);
    }

    private static class Cache<T> {
        private final RootCache<T> rootCache;
        private final Cache<T> parentCache;
        private final int parentIndex;
        private final int size;
        private final Map<Object, T> itemIdToItem = new HashMap<>();
        private final SortedMap<Integer, Object> indexToItemId = new TreeMap<>();
        private final SortedMap<Integer, Cache<T>> indexToCache = new TreeMap<>();

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
