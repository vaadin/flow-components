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

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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

import elemental.json.JsonArray;

public class TreeGridDataCommunicator<T>
        extends HierarchicalDataCommunicator<T> {
    private SerializableRunnable flushListener;

    private Cache<T> rootCache = new Cache<>();

    public TreeGridDataCommunicator(CompositeDataGenerator<T> dataGenerator,
            HierarchicalArrayUpdater arrayUpdater,
            SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode,
            SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode,
                uniqueKeyProviderSupplier);
    }

    protected void requestFlush(boolean forced) {
        // flush();
    }

    protected void requestFlush(HierarchicalUpdate update) {
        // update.commit();
    }

    protected void requestFlush(HierarchicalCommunicationController<T> update) {
        // update.flush();
    }

    public void addFlushListener(SerializableRunnable listener) {
        this.flushListener = listener;
    }

    public void setRequestedFlatRange(int start, int length) {
        for (int i = start; i < start + length; i++) {
            FlatIndexContext<T> context = getFlatIndexContext(i);
            int index = context.index;
            Cache<T> cache = context.cache;

            if (!cache.hasItem(index)) {
                // TODO: Optimize length calculation
                cache.setItems(index, fetchChildItems(cache,
                        Range.withLength(index, length)));
            }

            T item = cache.getItem(index);
            if (isExpanded(item) && !cache.hasCache(index)) {
                cache.createCache(index, countChildItems(cache));
            }
        }
    }

    private FlatIndexContext<T> getFlatIndexContext(int flatIndex) {
        return getFlatIndexContext(flatIndex, rootCache);
    }

    private FlatIndexContext<T> getFlatIndexContext(int flatIndex,
            Cache<T> cache) {
        int index = flatIndex;

        for (Cache<T> childCache : cache.getCaches()) {
            int cacheIndex = childCache.getParentIndex();

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

    private List<T> fetchChildItems(Cache<T> cache, Range range) {
        return getHierarchyMapper()
                .fetchChildItems(cache.getParentItem(), range).toList();
    }

    private int countChildItems(Cache<T> cache) {
        return getHierarchyMapper().countChildItems(cache.getParentItem());
    }

    private static record FlatIndexContext<T>(Cache<T> cache,
    int index)
    {
    }

    private static class Cache<T> {
        private Cache<T> parentCache;
        private Integer parentIndex;
        private Integer size;
        private SortedMap<Integer, T> items = new TreeMap<>();
        private SortedMap<Integer, Cache<T>> caches = new TreeMap<>();

        public Cache() {
            this(null, 0, 0);
        }

        public Cache(Cache<T> parentCache, Integer parentIndex, Integer size) {
            this.parentCache = parentCache;
            this.parentIndex = parentIndex;
            this.size = size;
        }

        public Integer getParentIndex() {
            return parentIndex;
        }

        public T getParentItem() {
            return parentCache.getItem(parentIndex);
        }

        public Integer getSize() {
            return size;
        }

        public int getFlatSize() {
            if (size == null) {
                return 0;
            }

            return size + caches.values().stream().mapToInt(Cache::getFlatSize)
                    .sum();
        }

        public boolean hasItem(int index) {
            return items.containsKey(index);
        }

        public T getItem(int index) {
            return items.get(index);
        }

        public void setItems(int startIndex, List<T> items) {
            for (int i = 0; i < items.size(); i++) {
                this.items.put(startIndex + i, items.get(i));
            }
        }

        public List<Cache<T>> getCaches() {
            return caches.values().stream().toList();
        }

        public boolean hasCache(int index) {
            return caches.containsKey(index);
        }

        public Cache<T> createCache(int index, int size) {
            Cache<T> cache = new Cache<>(this, index, size);
            caches.put(index, cache);
            return cache;
        }
    }
}
