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

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider.HierarchyFormat;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

public class ScrollToIndexTest {
    private TreeGrid<String> treeGrid;
    private TreeData<String> treeData;

    @Before
    public void init() {
        treeGrid = new TreeGrid<>();
        treeData = getTreeData();
    }

    @Test
    public void flattenedHierarchyFormat_scrollToMissingIndexPath_throwsIllegalArgumentException() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.FLATTENED));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> treeGrid.scrollToIndex(1000));
    }

    @Test
    public void nestedHierarchyFormat_scrollToMissingIndexPath_throwsIllegalArgumentException() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.NESTED));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> treeGrid.scrollToIndex(0, 20));
    }

    @Test
    public void flattenedHierarchyFormat_scrollToIndexPath_throwsUnsupportedOperationException() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.FLATTENED));
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> treeGrid.scrollToIndex(0, 0));
    }

    @Test
    public void nestedHierarchyFormat_scrollToIndexPathWithCollapsedParent_expandsParent() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.NESTED));
        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).iterator().next();
        Assert.assertFalse(treeGrid.isExpanded(rootItem));
        Assert.assertFalse(treeGrid.isExpanded(firstChild));
        treeGrid.scrollToIndex(10, 0);
        Assert.assertTrue(treeGrid.isExpanded(rootItem));
        Assert.assertFalse(treeGrid.isExpanded(firstChild));
    }

    private static TreeData<String> getTreeData() {
        var rootItemCount = 50;
        var childItemCountPerRootItem = 10;
        var treeData = new TreeData<String>();
        var rootItems = IntStream.range(0, rootItemCount)
                .mapToObj(i -> "Item " + i).toList();
        treeData.addRootItems(rootItems);
        rootItems.forEach(rootItem -> treeData.addItems(rootItem,
                IntStream.range(0, childItemCountPerRootItem)
                        .mapToObj(i -> rootItem + "-" + i)));
        return treeData;
    }
}
