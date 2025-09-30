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
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HierarchicalTestBean;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

public class TreeGridScrollTest {

    private TreeGrid<CustomTestBean> treeGrid;
    private TreeData<CustomTestBean> treeData;

    @Before
    public void init() {
        treeGrid = Mockito.spy(new TreeGrid<>());
        treeGrid.addHierarchyColumn(CustomTestBean::getIndex).setSortable(true);
        treeGrid.setPageSize(50);
        treeData = getTreeData();
    }

    @Test
    public void setCustomProviderWithoutMethodImpl_scrollToItem_unsupportedOperationExceptionThrown() {
        treeGrid.setDataProvider(new LazyDataProviderWithoutMethodImpl(100, 3));
        Assert.assertThrows(UnsupportedOperationException.class, () -> treeGrid
                .scrollToItem(new CustomTestBean("", 0, 0, null)));
    }

    @Test
    public void flattenedProvider_scrollToRootItem_scrollsToCorrectIndex() {
        setFlattenedProvider();
        treeGrid.scrollToItem(treeData.getRootItems().get(30));
        assertScrolledIndex(30);
    }

    @Test
    public void nestedProvider_scrollToRootItem_scrollsToCorrectIndex() {
        setNestedProvider();
        treeGrid.scrollToItem(treeData.getRootItems().get(30));
        assertScrolledIndexes(30);
    }

    @Test
    public void lazyProvider_scrollToRootItem_scrollsToCorrectIndex() {
        setLazyProvider();
        treeGrid.scrollToItem(treeData.getRootItems().get(30));
        assertScrolledIndexes(30);
    }

    @Test
    public void flattenedProvider_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        setFlattenedProvider();
        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndex(11);
    }

    @Test
    public void nestedProvider_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        setNestedProvider();
        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndexes(10, 0);
    }

    @Test
    public void lazyProvider_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        setLazyProvider();
        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndexes(10, 0);
    }

    @Test
    public void flattenedProvider_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        setFlattenedProvider();
        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndex(10);
    }

    @Test
    public void nestedProvider_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        setNestedProvider();
        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndexes(10);
    }

    @Test
    public void lazyProvider_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        setLazyProvider();
        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndexes(10);
    }

    @Test
    public void flattenedProvider_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        setFlattenedProvider();
        sortDescending();
        treeGrid.scrollToItem(getLastRootItem());
        assertScrolledIndex(0);
    }

    @Test
    public void nestedProvider_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        setNestedProvider();
        sortDescending();
        treeGrid.scrollToItem(getLastRootItem());
        assertScrolledIndexes(0);
    }

    @Test
    public void flattenedProvider_scrollToItem_nullItem_nullPointerExceptionThrown() {
        setFlattenedProvider();
        Assert.assertThrows(NullPointerException.class,
                () -> treeGrid.scrollToItem(null));
    }

    @Test
    public void nestedProvider_scrollToItem_nullItem_nullPointerExceptionThrown() {
        setNestedProvider();
        Assert.assertThrows(NullPointerException.class,
                () -> treeGrid.scrollToItem(null));
    }

    @Test
    public void lazyProvider_scrollToItem_nullItem_nullPointerExceptionThrown() {
        setLazyProvider();
        Assert.assertThrows(NullPointerException.class,
                () -> treeGrid.scrollToItem(null));
    }

    private void assertScrolledIndexes(int... scrolledIndexes) {
        Mockito.verify(treeGrid, Mockito.times(1))
                .scrollToIndex(scrolledIndexes);
    }

    private void assertScrolledIndex(int scrolledIndex) {
        Mockito.verify(treeGrid, Mockito.times(1)).scrollToIndex(scrolledIndex);
    }

    private CustomTestBean getLastRootItem() {
        var rootItems = treeData.getRootItems();
        return rootItems.get(rootItems.size() - 1);
    }

    private void sortDescending() {
        var column = treeGrid.getColumns().get(0);
        treeGrid.sort(
                List.of(new GridSortOrder<>(column, SortDirection.DESCENDING)));
    }

    private void setFlattenedProvider() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
    }

    private void setNestedProvider() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData));
    }

    private void setLazyProvider() {
        treeGrid.setDataProvider(new LazyDataProvider(100, 3));
    }

    private static TreeData<CustomTestBean> getTreeData() {
        var depth = 2;
        var rootItemCount = 50;
        var childItemCountPerRootItem = 10;
        var treeData = new TreeData<CustomTestBean>();
        var rootItems = IntStream.range(0, rootItemCount)
                .mapToObj(i -> new CustomTestBean(null, 0, i, null));
        treeData.addItems(rootItems, parent -> {
            if (parent.getDepth() >= depth - 1) {
                return Stream.empty();
            }
            return IntStream.range(0, childItemCountPerRootItem)
                    .mapToObj(i -> new CustomTestBean(parent.getId(),
                            parent.getDepth() + 1, i, parent));
        });
        return treeData;
    }

    private static class CustomTestBean extends HierarchicalTestBean {
        private final CustomTestBean parent;

        public CustomTestBean(String id, int depth, int index,
                CustomTestBean parent) {
            super(id, depth, index);
            this.parent = parent;
        }

        public CustomTestBean getParent() {
            return parent;
        }
    }

    private static class LazyDataProvider
            extends LazyDataProviderWithoutMethodImpl {

        public LazyDataProvider(int nodesPerLevel, int depth) {
            super(nodesPerLevel, depth);
        }

        @Override
        public int getItemIndex(CustomTestBean item,
                HierarchicalQuery<CustomTestBean, Void> query) {
            return item.getIndex();
        }

        @Override
        public CustomTestBean getParent(CustomTestBean item) {
            return item.getParent();
        }
    }

    private static class LazyDataProviderWithoutMethodImpl extends
            AbstractBackEndHierarchicalDataProvider<CustomTestBean, Void> {

        private final int nodesPerLevel;
        private final int depth;

        public LazyDataProviderWithoutMethodImpl(int nodesPerLevel, int depth) {
            this.nodesPerLevel = nodesPerLevel;
            this.depth = depth;
        }

        @Override
        public int getChildCount(
                HierarchicalQuery<CustomTestBean, Void> query) {
            var count = query.getParentOptional().flatMap(parent -> Optional
                    .of(internalHasChildren(parent) ? nodesPerLevel : 0));

            return count.orElse(nodesPerLevel);
        }

        @Override
        public boolean hasChildren(CustomTestBean item) {
            return internalHasChildren(item);
        }

        private boolean internalHasChildren(CustomTestBean node) {
            return node.getDepth() < depth;
        }

        @Override
        protected Stream<CustomTestBean> fetchChildrenFromBackEnd(
                HierarchicalQuery<CustomTestBean, Void> query) {
            var queryDepth = query.getParentOptional().isPresent()
                    ? query.getParent().getDepth() + 1
                    : 0;
            var parentKey = query.getParentOptional()
                    .flatMap(parent -> Optional.of(parent.getId()));
            var list = new ArrayList<CustomTestBean>();
            var limit = Math.min(query.getLimit(), nodesPerLevel);
            for (var i = 0; i < limit; i++) {
                list.add(new CustomTestBean(parentKey.orElse(null), queryDepth,
                        i + query.getOffset(), query.getParent()));
            }
            return list.stream();
        }
    }
}
