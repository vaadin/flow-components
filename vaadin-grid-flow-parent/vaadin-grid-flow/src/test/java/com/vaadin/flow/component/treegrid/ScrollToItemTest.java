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

public class ScrollToItemTest {

    private TreeGrid<HierarchicalTestBean> treeGrid;
    private TreeData<HierarchicalTestBean> treeData;

    @Before
    public void init() {
        treeGrid = Mockito.spy(new TreeGrid<>());
        treeGrid.addHierarchyColumn(HierarchicalTestBean::getIndex)
                .setSortable(true);
        treeGrid.setPageSize(50);
        treeData = getTreeData();
    }

    @Test
    public void setProviderWithoutMethodImpl_scrollToItem_unsupportedOperationExceptionThrown() {
        treeGrid.setDataProvider(new DataProviderWithoutMethodImpl(100, 3));
        Assert.assertThrows(UnsupportedOperationException.class, () -> treeGrid
                .scrollToItem(new HierarchicalTestBean("", 0, 0)));
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToRootItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        treeGrid.scrollToItem(treeData.getRootItems().get(30));
        assertScrolledIndex(30);
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToRootItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        treeGrid.scrollToItem(treeData.getRootItems().get(30));
        assertScrolledIndex(30);
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndex(11);
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndexes(10, 0);
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndex(11);
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndexes(10, 0);
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToItemWithCollapsedParent_expandsParent() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        var rootItem = treeData.getRootItems().get(10);
        Assert.assertFalse(treeGrid.isExpanded(rootItem));
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);
        Assert.assertTrue(treeGrid.isExpanded(rootItem));
        Assert.assertFalse(treeGrid.isExpanded(firstChild));
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToItemWithCollapsedParent_expandsParent() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        var rootItem = treeData.getRootItems().get(10);
        Assert.assertFalse(treeGrid.isExpanded(rootItem));
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);
        Assert.assertTrue(treeGrid.isExpanded(rootItem));
        Assert.assertFalse(treeGrid.isExpanded(firstChild));
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToMissingItem_doesNotScroll() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        Assert.assertThrows(IllegalArgumentException.class,
                this::scrollToMissingItem);
        assertNotScrolled();
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToMissingItem_doesNotScroll() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        Assert.assertThrows(IllegalArgumentException.class,
                this::scrollToMissingItem);
        assertNotScrolled();
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        sortDescending();
        treeGrid.scrollToItem(treeData.getRootItems().getLast());
        assertScrolledIndex(0);
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        sortDescending();
        treeGrid.scrollToItem(treeData.getRootItems().getLast());
        assertScrolledIndex(0);
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToItem_nullItem_nullPointerExceptionThrown() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        Assert.assertThrows(NullPointerException.class,
                () -> treeGrid.scrollToItem(null));
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToItem_nullItem_nullPointerExceptionThrown() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
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

    private void scrollToMissingItem() {
        treeGrid.scrollToItem(new HierarchicalTestBean("NOT PRESENT", -2, -2));
    }

    private void assertNotScrolled() {
        Mockito.verify(treeGrid, Mockito.times(0)).scrollToIndex(Mockito.any());
    }

    private void sortDescending() {
        var column = treeGrid.getColumns().getFirst();
        treeGrid.sort(
                List.of(new GridSortOrder<>(column, SortDirection.DESCENDING)));
    }

    private static TreeData<HierarchicalTestBean> getTreeData() {
        var depth = 2;
        var rootItemCount = 50;
        var childItemCountPerRootItem = 10;
        var treeData = new TreeData<HierarchicalTestBean>();
        var rootItems = IntStream.range(0, rootItemCount)
                .mapToObj(i -> new HierarchicalTestBean(null, 0, i));
        treeData.addItems(rootItems, parent -> {
            if (parent.getDepth() >= depth - 1) {
                return Stream.empty();
            }
            return IntStream.range(0, childItemCountPerRootItem)
                    .mapToObj(i -> new HierarchicalTestBean(parent.getId(),
                            parent.getDepth() + 1, i));
        });
        return treeData;
    }

    private static class DataProviderWithoutMethodImpl extends
            AbstractBackEndHierarchicalDataProvider<HierarchicalTestBean, Void> {

        private final int nodesPerLevel;
        private final int depth;

        public DataProviderWithoutMethodImpl(int nodesPerLevel, int depth) {
            this.nodesPerLevel = nodesPerLevel;
            this.depth = depth;
        }

        @Override
        public int getChildCount(
                HierarchicalQuery<HierarchicalTestBean, Void> query) {
            return 0;
        }

        @Override
        public boolean hasChildren(HierarchicalTestBean item) {
            return internalHasChildren(item);
        }

        private boolean internalHasChildren(HierarchicalTestBean node) {
            return node.getDepth() < depth;
        }

        @Override
        protected Stream<HierarchicalTestBean> fetchChildrenFromBackEnd(
                HierarchicalQuery<HierarchicalTestBean, Void> query) {
            var queryDepth = query.getParentOptional().isPresent()
                    ? query.getParent().getDepth() + 1
                    : 0;
            var parentKey = query.getParentOptional()
                    .flatMap(parent -> Optional.of(parent.getId()));
            var list = new ArrayList<HierarchicalTestBean>();
            var limit = Math.min(query.getLimit(), nodesPerLevel);
            for (var i = 0; i < limit; i++) {
                list.add(new HierarchicalTestBean(parentKey.orElse(null),
                        queryDepth, i + query.getOffset()));
            }
            return list.stream();
        }
    }
}
