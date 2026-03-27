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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

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
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class TreeGridScrollToItemTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private TreeGrid<HierarchicalTestBean> treeGrid;
    private TreeData<HierarchicalTestBean> treeData;

    @BeforeEach
    void init() {
        treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(HierarchicalTestBean::getIndex)
                .setSortable(true);
        treeGrid.setPageSize(50);
        treeData = getTreeData();
        ui.add(treeGrid);
    }

    @Test
    void setProviderWithoutMethodImpl_scrollToItem_unsupportedOperationExceptionThrown() {
        treeGrid.setDataProvider(new DataProviderWithoutMethodImpl());
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> treeGrid
                        .scrollToItem(new HierarchicalTestBean("", 0, 0)));
    }

    @Test
    void treeDataProvider_flattenedHierarchyFormat_scrollToRootItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));

        var item = treeData.getRootItems().get(30);
        treeGrid.scrollToItem(item);

        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(item, new int[] { 30 });
    }

    @Test
    void treeDataProvider_nestedHierarchyFormat_scrollToRootItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        var item = treeData.getRootItems().get(30);
        treeGrid.scrollToItem(item);

        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(item, new int[] { 30 });
    }

    @Test
    void treeDataProvider_flattenedHierarchyFormat_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));

        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);

        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(firstChild,
                new int[] { 11 });
    }

    @Test
    void treeDataProvider_nestedHierarchyFormat_scrollToExpandedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        var rootItem = treeData.getRootItems().get(10);
        treeGrid.expand(rootItem);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);

        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(firstChild,
                new int[] { 10, 0 });
    }

    @Test
    void treeDataProvider_flattenedHierarchyFormat_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));

        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);

        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(firstChild,
                new int[] { 11 });
    }

    @Test
    void treeDataProvider_nestedHierarchyFormat_scrollToCollapsedChildItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        var rootItem = treeData.getRootItems().get(10);
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);

        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(firstChild,
                new int[] { 10, 0 });
    }

    @Test
    void treeDataProvider_flattenedHierarchyFormat_scrollToItemWithCollapsedParent_expandsParent() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        var rootItem = treeData.getRootItems().get(10);
        Assertions.assertFalse(treeGrid.isExpanded(rootItem));
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);
        Assertions.assertTrue(treeGrid.isExpanded(rootItem));
        Assertions.assertFalse(treeGrid.isExpanded(firstChild));
    }

    @Test
    void treeDataProvider_nestedHierarchyFormat_scrollToItemWithCollapsedParent_expandsParent() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        var rootItem = treeData.getRootItems().get(10);
        Assertions.assertFalse(treeGrid.isExpanded(rootItem));
        var firstChild = treeData.getChildren(rootItem).getFirst();
        treeGrid.scrollToItem(firstChild);
        Assertions.assertTrue(treeGrid.isExpanded(rootItem));
        Assertions.assertFalse(treeGrid.isExpanded(firstChild));
    }

    @Test
    void treeDataProvider_flattenedHierarchyFormat_scrollToMissingItem_doesNotScroll() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));

        Assertions.assertThrows(NoSuchElementException.class,
                this::scrollToMissingItem);

        ui.fakeClientCommunication();
        assertNoJavaScriptScrollToItemInvocation();
    }

    @Test
    void treeDataProvider_nestedHierarchyFormat_scrollToMissingItem_doesNotScroll() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));

        Assertions.assertThrows(NoSuchElementException.class,
                this::scrollToMissingItem);

        ui.fakeClientCommunication();
        assertNoJavaScriptScrollToItemInvocation();
    }

    @Test
    void treeDataProvider_flattenedHierarchyFormat_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        sortDescending();

        var item = treeData.getRootItems().getLast();
        treeGrid.scrollToItem(item);
        ui.fakeClientCommunication();

        assertSingleJavaScriptScrollToItemInvocation(item, new int[] { 0 });
    }

    @Test
    void treeDataProvider_nestedHierarchyFormat_reverseSort_scrollToItem_scrollsToCorrectIndex() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        sortDescending();

        var item = treeData.getRootItems().getLast();
        treeGrid.scrollToItem(item);

        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(item, new int[] { 0 });
    }

    @Test
    void treeDataProvider_flattenedHierarchyFormat_scrollToItem_nullItem_nullPointerExceptionThrown() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.FLATTENED));
        Assertions.assertThrows(NullPointerException.class,
                () -> treeGrid.scrollToItem(null));
    }

    @Test
    void treeDataProvider_nestedHierarchyFormat_scrollToItem_nullItem_nullPointerExceptionThrown() {
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData,
                HierarchicalDataProvider.HierarchyFormat.NESTED));
        Assertions.assertThrows(NullPointerException.class,
                () -> treeGrid.scrollToItem(null));
    }

    @Test
    void scrollToItem_afterAttach_schedulesJsExecution() {
        var item = treeData.getRootItems().get(10);
        treeGrid.setTreeData(treeData);

        treeGrid.scrollToItem(item);
        treeGrid.scrollToItem(item);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(item, new int[] { 10 });
    }

    @Test
    void scrollToItem_beforeAttach_thenAttach_schedulesJsExecution() {
        var item = treeData.getRootItems().get(10);
        treeGrid.setTreeData(treeData);

        ui.remove(treeGrid);
        treeGrid.scrollToItem(item);
        treeGrid.scrollToItem(item);
        ui.add(treeGrid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(item, new int[] { 10 });
    }

    @Test
    void scrollToIndex_scrollToItem_onlyScrollToItemExecuted() {
        var item = treeData.getRootItems().get(10);
        treeGrid.setTreeData(treeData);
        ui.add(treeGrid);

        treeGrid.scrollToIndex(5);
        treeGrid.scrollToItem(item);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation(item, new int[] { 10 });
    }

    private void scrollToMissingItem() {
        treeGrid.scrollToItem(new HierarchicalTestBean("NOT PRESENT", -2, -2));
    }

    private void assertNoJavaScriptScrollToItemInvocation() {
        List<JavaScriptInvocation> invocations = getJavaScriptScrollInvocations();
        Assertions.assertTrue(invocations.isEmpty());
    }

    private void assertSingleJavaScriptScrollToItemInvocation(
            HierarchicalTestBean expectedItem, int[] expectedPath) {
        var expectedItemKey = treeGrid.getDataCommunicator().getKeyMapper()
                .key(expectedItem);

        var invocations = getJavaScriptScrollInvocations();
        Assertions.assertEquals(1, invocations.size());

        var invocation = invocations.get(0);
        Assertions.assertTrue(
                invocation.getExpression().contains("scrollToItem"));
        Assertions.assertEquals(expectedItemKey,
                invocation.getParameters().get(0));
        Assertions.assertArrayEquals(expectedPath,
                (int[]) invocation.getParameters().get(1));
    }

    private List<JavaScriptInvocation> getJavaScriptScrollInvocations() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("scroll"))
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
