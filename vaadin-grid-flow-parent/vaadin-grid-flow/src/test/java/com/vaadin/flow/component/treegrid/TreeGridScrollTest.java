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

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HierarchicalTestBean;
import com.vaadin.flow.component.grid.LazyHierarchicalDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

public class TreeGridScrollTest {

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
    public void setNonInMemoryProvider_scrollToNonExistingItem_unsupportedOperationExceptionThrown() {
        setNonInMemoryProvider();
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> treeGrid.scrollToItem(treeData.getRootItems().get(30)));
    }

    @Test
    public void flattenedTreeDataProvider_scrollToRootItem_scrollsToCorrectIndex() {
        setFlattenedTreeDataProvider();
        treeGrid.scrollToItem(treeData.getRootItems().get(30));
        assertScrolledIndex(30);
    }

    @Test
    public void nestedTreeDataProvider_scrollToRootItem_scrollsToCorrectIndex() {
        setNestedTreeDataProvider();
        treeGrid.scrollToItem(treeData.getRootItems().get(30));
        assertScrolledIndexes(30);
    }

    @Test
    public void flattenedTreeDataProvider_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        setFlattenedTreeDataProvider();
        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndex(11);
    }

    @Test
    public void nestedTreeDataProvider_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        setNestedTreeDataProvider();
        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndexes(10, 0);
    }

    @Test
    public void flattenedTreeDataProvider_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        setFlattenedTreeDataProvider();
        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndex(10);
    }

    @Test
    public void nestedTreeDataProvider_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        setNestedTreeDataProvider();
        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        treeGrid.scrollToItem(firstChild);
        assertScrolledIndexes(10);
    }

    @Test
    public void flattenedTreeDataProvider_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        setFlattenedTreeDataProvider();
        sortDescending();
        treeGrid.scrollToItem(getLastRootItem());
        assertScrolledIndex(0);
    }

    @Test
    public void nestedTreeDataProvider_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        setNestedTreeDataProvider();
        sortDescending();
        treeGrid.scrollToItem(getLastRootItem());
        assertScrolledIndexes(0);
    }

    @Test
    public void flattenedTreeDataProvider_scrollToNonExistingItem_illegalArgumentExceptionThrown() {
        setFlattenedTreeDataProvider();
        Assert.assertThrows(IllegalArgumentException.class, () -> treeGrid
                .scrollToItem(new HierarchicalTestBean("Not present", 0, 0)));
    }

    @Test
    public void scrollToItem_nullItem_nullPointerExceptionThrown() {
        setNestedTreeDataProvider();
        Assert.assertThrows(NullPointerException.class,
                () -> treeGrid.scrollToItem(null));
    }

    @Test
    public void nestedTreeDataProvider_scrollToNonExistingItem_illegalArgumentExceptionThrown() {
        setNestedTreeDataProvider();
        Assert.assertThrows(IllegalArgumentException.class, () -> treeGrid
                .scrollToItem(new HierarchicalTestBean("Not present", 0, 0)));
    }

    private void assertScrolledIndexes(int... scrolledIndexes) {
        Mockito.verify(treeGrid, Mockito.times(1))
                .scrollToIndex(scrolledIndexes);
    }

    private void assertScrolledIndex(int scrolledIndex) {
        Mockito.verify(treeGrid, Mockito.times(1)).scrollToIndex(scrolledIndex);
    }

    private HierarchicalTestBean getLastRootItem() {
        var rootItems = treeData.getRootItems();
        return rootItems.get(rootItems.size() - 1);
    }

    private void sortDescending() {
        var column = treeGrid.getColumns().get(0);
        treeGrid.sort(
                List.of(new GridSortOrder<>(column, SortDirection.DESCENDING)));
    }

    private void setFlattenedTreeDataProvider() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.FLATTENED));
    }

    private void setNestedTreeDataProvider() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData));
    }

    private void setLazyProvider() {
        // TODO implement
        treeGrid.setDataProvider(new LazyHierarchicalDataProvider(100, 3) {
            @Override
            public int getItemIndex(HierarchicalTestBean item,
                    HierarchicalQuery<HierarchicalTestBean, Object> query) {
                return item.getIndex();
            }

            @Override
            public HierarchicalTestBean getParent(HierarchicalTestBean item) {
                return item;
            }
        });
    }

    private void setNonInMemoryProvider() {
        treeGrid.setDataProvider(new LazyHierarchicalDataProvider(100, 3));
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
}
