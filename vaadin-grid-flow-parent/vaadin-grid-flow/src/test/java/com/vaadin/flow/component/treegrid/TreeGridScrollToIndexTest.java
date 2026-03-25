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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider.HierarchyFormat;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class TreeGridScrollToIndexTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private TreeGrid<String> treeGrid;
    private TreeData<String> treeData = new TreeData<>();

    @BeforeEach
    void setup() {
        treeGrid = new TreeGrid<>();
    }

    @Test
    void nestedHierarchyFormat_scrollToIndexPath_doesNotThrow() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.NESTED));
        treeGrid.scrollToIndex(0, 0);
    }

    @Test
    void flattenedHierarchyFormat_scrollToIndexPath_throws() {
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, HierarchyFormat.FLATTENED));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> treeGrid.scrollToIndex(0, 0));
    }

    @Test
    void scrollToIndex_afterAttach_schedulesJsExecution() {
        ui.add(treeGrid);
        treeGrid.scrollToIndex(5);
        treeGrid.scrollToIndex(5);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex", 5);
    }

    @Test
    void scrollToIndex_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToIndex(5);
        treeGrid.scrollToIndex(5);
        ui.add(treeGrid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex", 5);
    }

    @Test
    void scrollToIndexPath_afterAttach_schedulesJsExecution() {
        ui.add(treeGrid);
        treeGrid.scrollToIndex(1, 2);
        treeGrid.scrollToIndex(1, 2);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex",
                new int[] { 1, 2 });
    }

    @Test
    void scrollToIndexPath_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToIndex(1, 2);
        treeGrid.scrollToIndex(1, 2);
        ui.add(treeGrid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex",
                new int[] { 1, 2 });
    }

    @Test
    void scrollToEnd_afterAttach_schedulesJsExecution() {
        ui.add(treeGrid);
        treeGrid.scrollToEnd();
        treeGrid.scrollToEnd();
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation(
                "scrollToIndex(...Array(10).fill(-1))");
    }

    @Test
    void scrollToEnd_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToEnd();
        treeGrid.scrollToEnd();
        ui.add(treeGrid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation(
                "scrollToIndex(...Array(10).fill(-1))");
    }

    @Test
    void onlyLastScrollInvocationExecuted() {
        treeData.addRootItems("Item 0");
        treeGrid.setTreeData(treeData);
        ui.add(treeGrid);

        treeGrid.scrollToItem("Item 0");
        treeGrid.scrollToIndex(5);
        treeGrid.scrollToIndex(5, 2);
        treeGrid.scrollToStart();
        treeGrid.scrollToEnd();
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation(
                "scrollToIndex(...Array(10).fill(-1))");
    }

    private void assertSingleJavaScriptScrollInvocation(
            String expectedExpression, Object... expectedParams) {
        var invocations = getJavaScriptScrollInvocations();
        Assertions.assertEquals(1, invocations.size());

        var invocation = invocations.get(0);
        Assertions.assertTrue(
                invocation.getExpression().contains(expectedExpression));

        var params = invocation.getParameters();
        for (int i = 1; i < expectedParams.length; i++) {
            Assertions.assertEquals(expectedParams[i], params.get(i));
        }
    }

    private List<JavaScriptInvocation> getJavaScriptScrollInvocations() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("scroll"))
                .toList();
    }
}
