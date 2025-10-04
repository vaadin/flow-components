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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider.HierarchyFormat;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class ScrollToIndexTest {
    private TreeGrid<String> treeGrid;
    private TreeData<String> treeData;
    private UI ui;

    @Before
    public void init() {
        ui = new UI();
        UI.setCurrent(ui);
        var mockSession = Mockito.mock(VaadinSession.class);
        var mockService = Mockito.mock(VaadinService.class);
        var mockContext = Mockito.mock(VaadinContext.class);
        var mockConfiguration = Mockito.mock(DeploymentConfiguration.class);
        Mockito.when(mockSession.getService()).thenReturn(mockService);
        Mockito.when(mockSession.getConfiguration())
                .thenReturn(mockConfiguration);
        Mockito.when(mockService.getContext()).thenReturn(mockContext);
        ui.getInternals().setSession(mockSession);
        treeGrid = new TreeGrid<>();
        ui.add(treeGrid);
        treeData = getTreeData();
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
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
        var firstChild = treeData.getChildren(rootItem).getFirst();
        Assert.assertFalse(treeGrid.isExpanded(rootItem));
        Assert.assertFalse(treeGrid.isExpanded(firstChild));
        treeGrid.scrollToIndex(10, 0);
        Assert.assertTrue(treeGrid.isExpanded(rootItem));
        Assert.assertFalse(treeGrid.isExpanded(firstChild));
    }

    @Test
    public void flattenedHierarchyFormat_scrollToIndexNotPresent_notScrolled() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.FLATTENED));
        assertScrollToIndexCalled(false, 1000);
        assertScrollToIndexCalled(false, -1000);
    }

    @Test
    public void flattenedHierarchyFormat_scrollToIndex_scrolled() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.FLATTENED));
        assertScrollToIndexCalled(true, 10);
        assertScrollToIndexCalled(true, -10);
    }

    @Test
    public void nestedHierarchyFormat_scrollToPathNotPresent_notScrolled() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.NESTED));
        assertScrollToPathCalled(false, 1000);
        assertScrollToPathCalled(false, -1000);
        assertScrollToPathCalled(false, 10, 5, 5);
        assertScrollToPathCalled(false, -10, -5, -5);
    }

    @Test
    public void nestedHierarchyFormat_scrollToPath_scrolled() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.NESTED));
        assertScrollToPathCalled(true, 10, 5);
        assertScrollToPathCalled(true, -10, -5);
    }

    private void assertScrollToIndexCalled(boolean called, int index) {
        treeGrid.scrollToIndex(index);
        Assert.assertEquals(called, isScrollToIndexCalled());
    }

    private void assertScrollToPathCalled(boolean called, int... path) {
        treeGrid.scrollToIndex(path);
        Assert.assertEquals(called, isScrollToIndexCalled());
    }

    private boolean isScrollToIndexCalled() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
        var invocations = ui.getInternals().dumpPendingJavaScriptInvocations();
        return invocations.stream().anyMatch(invocation -> invocation
                .getInvocation().getExpression().contains("scrollToIndex"));
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
