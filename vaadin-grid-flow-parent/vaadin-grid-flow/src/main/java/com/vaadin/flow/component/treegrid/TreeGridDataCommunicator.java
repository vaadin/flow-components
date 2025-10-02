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
import java.util.LinkedList;
import java.util.List;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;

/**
 * This class is for internal use only.
 */
class TreeGridDataCommunicator<T> extends HierarchicalDataCommunicator<T> {
    private final Element element;

    public TreeGridDataCommunicator(Element element,
            CompositeDataGenerator<T> dataGenerator, ArrayUpdater arrayUpdater,
            SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
        super(dataGenerator, arrayUpdater, element.getNode(),
                uniqueKeyProviderSupplier);
        this.element = element;
    }

    @Override
    public void reset() {
        super.reset();
        if (element != null) {
            element.callJsFunction("$connector.reset");
        }
    }

    @Override
    public List<T> preloadFlatRangeForward(int start, int length) {
        return super.preloadFlatRangeForward(start, length);
    }

    @Override
    public List<T> preloadFlatRangeBackward(int start, int length) {
        return super.preloadFlatRangeBackward(start, length);
    }

    @Override
    public int resolveIndexPath(int... path) {
        return super.resolveIndexPath(path);
    }

    /**
     * Expands all ancestors of the item and returns the index path of it.
     * Returns empty list if item is not found.
     *
     * @param item
     *            the item to resolve
     * @return the index path of the item
     */
    public int[] resolveItem(T item) {
        var ancestors = getAncestors(item);
        var indexPath = getIndexPath(item, ancestors);
        if (!indexPath.isEmpty()) {
            expand(ancestors);
        }
        return indexPath;
    }

    /**
     * Gets the index path for the given item. Returns empty list if item is not
     * found.
     * <p>
     * Accepts the list of ancestors for optimization purposes where the list
     * already exists.
     * <p>
     * In order to be able to use this method, the data provider should
     * implement
     * {@link HierarchicalDataProvider#getItemIndex(T, HierarchicalQuery)}. Any
     * in-memory data provider implements it by default.
     *
     * @param item
     *            the item to get the index path for
     * @param ancestors
     *            the ordered list of the ancestors of the item
     * @return index path for the given item
     */
    private int[] getIndexPath(T item, List<T> ancestors) {
        var path = new ArrayList<Integer>();
        if (getDataProvider().getHierarchyFormat()
                .equals(HierarchicalDataProvider.HierarchyFormat.NESTED)) {
            path.addAll(getAncestorPath(ancestors));
        }
        var itemIndex = getItemIndex(item,
                path.isEmpty() ? null : ancestors.get(ancestors.size() - 1));
        if (itemIndex == -1) {
            throw new IllegalArgumentException("Item does not exist.");
        }
        path.add(itemIndex);
        return path.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Gets the ordered list of ancestors for the given item.
     * <p>
     * In order to be able to use this method, the data provider should
     * implement {@link HierarchicalDataProvider#getParent(T)}.
     * {@link TreeDataProvider} implements it by default.
     *
     * @param item
     *            the item to get the ancestors for
     * @return ordered list of ancestors of the given item
     */
    private List<T> getAncestors(T item) {
        var ancestors = new LinkedList<T>();
        while ((item = getDataProvider().getParent(item)) != null) {
            ancestors.addFirst(item);
        }
        return ancestors;
    }

    private List<Integer> getAncestorPath(List<T> ancestors) {
        var ancestorPath = new ArrayList<Integer>();
        for (var i = 0; i < ancestors.size(); i++) {
            var ancestorIndex = getItemIndex(ancestors.get(i),
                    i == 0 ? null : ancestors.get(i - 1));
            if (ancestorIndex == -1) {
                throw new IllegalArgumentException("Item does not exist.");
            }
            ancestorPath.add(ancestorIndex);
        }
        return ancestorPath;
    }

    private int getItemIndex(T item, T parent) {
        var query = buildQuery(parent, 0, Integer.MAX_VALUE);
        return ((HierarchicalDataProvider<T, Object>) getDataProvider())
                .getItemIndex(item, query);
    }
}
