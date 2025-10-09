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
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

public class ExpandCollapseTest {

    private TreeGrid<String> treeGrid;
    private AtomicReference<ExpandEvent<String, TreeGrid<String>>> expandEvent;
    private AtomicReference<CollapseEvent<String, TreeGrid<String>>> collapseEvent;

    @Before
    public void init() {
        treeGrid = new TreeGrid<>();
        var treeData = new TreeData<String>();
        treeData.addRootItems("Item 0", "Item 1");
        treeData.addItems("Item 0", "Item 0-0", "Item 0-1");
        treeData.addItems("Item 0-0", "Item 0-0-0", "Item 0-0-1");
        treeData.addItems("Item 0-0-0", "Item 0-0-0-0", "Item 0-0-0-1");
        var treeDataProvider = new TreeDataProvider<>(treeData);
        treeGrid.setDataProvider(treeDataProvider);
        expandEvent = new AtomicReference<>();
        treeGrid.addExpandListener(expandEvent::set);
        collapseEvent = new AtomicReference<>();
        treeGrid.addCollapseListener(collapseEvent::set);
    }

    @Test
    public void expand_itemExpandedCorrectly() {
        treeGrid.expand("Item 0");
        Assert.assertTrue(treeGrid.isExpanded("Item 0"));
        Assert.assertFalse(treeGrid.isExpanded("Item 0-0"));
        Assert.assertEquals(List.of("Item 0"), expandEvent.get().getItems());
    }

    @Test
    public void collapse_itemCollapsedCorrectly() {
        treeGrid.expand("Item 0");
        treeGrid.collapse("Item 0");
        Assert.assertFalse(treeGrid.isExpanded("Item 0"));
        Assert.assertEquals(List.of("Item 0"), collapseEvent.get().getItems());
    }

    @Test
    public void expandRecursivelyWithNonMaxDepth_itemsExpandedCorrectly() {
        treeGrid.expandRecursively(List.of("Item 0"), 1);
        Assert.assertTrue(treeGrid.isExpanded("Item 0"));
        Assert.assertTrue(treeGrid.isExpanded("Item 0-0"));
        Assert.assertFalse(treeGrid.isExpanded("Item 0-0-0"));
        Assert.assertEquals(List.of("Item 0", "Item 0-0"),
                expandEvent.get().getItems());
    }

    @Test
    public void expandRecursivelyWithMaxDepth_itemsExpandedCorrectly() {
        treeGrid.expandRecursively(List.of("Item 0"), 2);
        Assert.assertTrue(treeGrid.isExpanded("Item 0"));
        Assert.assertTrue(treeGrid.isExpanded("Item 0-0"));
        Assert.assertTrue(treeGrid.isExpanded("Item 0-0-0"));
        Assert.assertEquals(List.of("Item 0", "Item 0-0", "Item 0-0-0"),
                expandEvent.get().getItems());
    }

    @Test
    public void collapseRecursivelyWithNonMaxDepth_itemsCollapsedCorrectly() {
        treeGrid.expandRecursively(List.of("Item 0"), 2);
        treeGrid.collapseRecursively(List.of("Item 0"), 1);
        Assert.assertFalse(treeGrid.isExpanded("Item 0"));
        Assert.assertFalse(treeGrid.isExpanded("Item 0-0"));
        Assert.assertTrue(treeGrid.isExpanded("Item 0-0-0"));
        Assert.assertEquals(List.of("Item 0", "Item 0-0"),
                collapseEvent.get().getItems());
    }

    @Test
    public void collapseRecursivelyWithMaxDepth_itemsCollapsedCorrectly() {
        treeGrid.expandRecursively(List.of("Item 0"), 2);
        treeGrid.collapseRecursively(List.of("Item 0"), 2);
        Assert.assertFalse(treeGrid.isExpanded("Item 0"));
        Assert.assertFalse(treeGrid.isExpanded("Item 0-0"));
        Assert.assertFalse(treeGrid.isExpanded("Item 0-0-0"));
        Assert.assertEquals(List.of("Item 0", "Item 0-0", "Item 0-0-0"),
                collapseEvent.get().getItems());
    }
}
