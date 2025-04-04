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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.flow.function.ValueProvider;

class RootCache<T> extends Cache<T> {
    private final ValueProvider<T, Object> itemIdProvider;
    private final Map<Object, ItemContext<T>> itemIdToContext = new HashMap<>();

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

        for (Entry<Integer, Cache<T>> entry : cache.indexToCache.entrySet()) {
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

    public ItemContext<T> getItemContext(T item) {
        Object itemId = getItemId(item);
        return itemIdToContext.get(itemId);
    }

    void addItemContext(T item, Cache<T> cache, int index) {
        Object itemId = getItemId(item);
        itemIdToContext.put(itemId, new ItemContext<>(itemId, cache, index));
    }

    void removeItemContext(T item) {
        Object itemId = getItemId(item);
        itemIdToContext.remove(itemId);
    }

    Object getItemId(T item) {
        return itemIdProvider.apply(item);
    }
}
