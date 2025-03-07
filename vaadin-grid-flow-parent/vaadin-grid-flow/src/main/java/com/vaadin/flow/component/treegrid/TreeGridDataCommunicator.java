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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
            itemContext.cache().refreshItem(item);
        }

        requestFlush();
    }

    /** New API */
    public void refreshAllVisible() {
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
            var cache = context.cache();
            var index = context.index();

            if (!cache.hasItem(index)) {
                List<T> childItems = fetchDataProviderChildren(
                        cache.getParentItem(), Range.between(index, end))
                                .toList();
                cache.setItems(index, childItems);
            }

            T item = cache.getItem(index);
            if (!isFlatHierarchy() && isExpanded(item)
                    && !cache.hasCache(index)) {
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

    public boolean isFlatHierarchy() {
        return getDataProvider().isFlatHierarchy();
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
        if (isFlatHierarchy()) {
            return getDataProvider().getParentItem(item);
        }

        var itemContext = rootCache.getItemContext(item);
        if (itemContext == null) {
            return null;
        }
        return itemContext.cache().getParentItem();
    }

    /** @see HierarchicalDataCommunicator#getDepth(T) */
    public int getDepth(T item) {
        if (isFlatHierarchy()) {
            return getDataProvider().getDepth(item);
        }

        var itemContext = rootCache.getItemContext(item);
        if (itemContext == null) {
            return -1;
        }
        return itemContext.cache().getDepth();
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
            void removeItemContext(T item) {
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
                getInMemorySorting(), getFilter(), expandedItemIds, parent);

        return ((HierarchicalDataProvider<T, Object>) getDataProvider())
                .fetchChildren(query);
    }

    @SuppressWarnings("unchecked")
    private int getDataProviderChildCount(T parent) {
        HierarchicalQuery<T, Object> query = new HierarchicalQuery<>(
                getFilter(), expandedItemIds, parent);

        return ((HierarchicalDataProvider<T, Object>) getDataProvider())
                .getChildCount(query);
    }
}
