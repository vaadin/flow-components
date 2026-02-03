/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HierarchicalTestBean;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.server.VaadinSession;

public class ScrollToItemTest {

    private TreeGrid<HierarchicalTestBean> treeGrid;
    private TreeData<HierarchicalTestBean> treeData;
    private UI ui;

    @Before
    public void init() {
        treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(HierarchicalTestBean::getIndex)
                .setSortable(true);
        treeGrid.setPageSize(50);
        treeData = getTreeData();

        ui = Mockito.spy(new UI());
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        ui.add(treeGrid);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void setProviderWithoutMethodImpl_scrollToItem_unsupportedOperationExceptionThrown() {
        treeGrid.setDataProvider(new DataProviderWithoutMethodImpl());
        Assert.assertThrows(UnsupportedOperationException.class, () -> treeGrid
                .scrollToItem(new HierarchicalTestBean("", 0, 0)));
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToRootItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));

        var item = treeData.getRootItems().get(30);
        treeGrid.scrollToItem(item);

        fakeClientCommunication();
        assertScrollToItemInvocation(item, new int[] { 30 });
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToRootItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        var item = treeData.getRootItems().get(30);
        treeGrid.scrollToItem(item);

        fakeClientCommunication();
        assertScrollToItemInvocation(item, new int[] { 30 });
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));

        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);

        fakeClientCommunication();
        assertScrollToItemInvocation(firstChild, new int[] { 11 });
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);

        fakeClientCommunication();
        assertScrollToItemInvocation(firstChild, new int[] { 10, 0 });
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));

        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);

        fakeClientCommunication();
        assertScrollToItemInvocation(firstChild, new int[] { 11 });
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);

        fakeClientCommunication();
        assertScrollToItemInvocation(firstChild, new int[] { 10, 0 });
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

        Assert.assertThrows(NoSuchElementException.class,
                this::scrollToMissingItem);

        fakeClientCommunication();
        assertNoScrollToItemInvocation();
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_scrollToMissingItem_doesNotScroll() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        Assert.assertThrows(NoSuchElementException.class,
                this::scrollToMissingItem);

        fakeClientCommunication();
        assertNoScrollToItemInvocation();
    }

    @Test
    public void treeDataProvider_flattenedHierarchyFormat_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        sortDescending();

        var item = treeData.getRootItems().getLast();
        treeGrid.scrollToItem(item);
        fakeClientCommunication();

        assertScrollToItemInvocation(item, new int[] { 0 });
    }

    @Test
    public void treeDataProvider_nestedHierarchyFormat_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        sortDescending();

        var item = treeData.getRootItems().getLast();
        treeGrid.scrollToItem(item);

        fakeClientCommunication();
        assertScrollToItemInvocation(item, new int[] { 0 });
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

    private void scrollToMissingItem() {
        treeGrid.scrollToItem(new HierarchicalTestBean("NOT PRESENT", -2, -2));
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private void assertScrollToItemInvocation(HierarchicalTestBean item,
            int[] path) {
        var itemKey = treeGrid.getDataCommunicator().getKeyMapper().key(item);

        var invocations = getScrollToItemInvocations();
        Assert.assertEquals(1, invocations.size());
        Assert.assertEquals(itemKey, invocations.get(0).getParameters().get(0));
        Assert.assertArrayEquals(path,
                (int[]) invocations.get(0).getParameters().get(1));
    }

    private void assertNoScrollToItemInvocation() {
        var invocations = getScrollToItemInvocations();
        Assert.assertTrue(invocations.isEmpty());
    }

    private List<JavaScriptInvocation> getScrollToItemInvocations() {
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("$connector.scrollToItem"))
                .toList();
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

        @Override
        public int getChildCount(
                HierarchicalQuery<HierarchicalTestBean, Void> query) {
            return 0;
        }

        @Override
        public boolean hasChildren(HierarchicalTestBean item) {
            return false;
        }

        @Override
        protected Stream<HierarchicalTestBean> fetchChildrenFromBackEnd(
                HierarchicalQuery<HierarchicalTestBean, Void> query) {
            return Stream.empty();
        }
    }
}
