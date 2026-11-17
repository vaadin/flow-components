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

import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider.HierarchyFormat;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsCall;
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
        assertSingleJavaScriptScrollCall("scrollToIndex", 5);
    }

    @Test
    void scrollToIndex_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToIndex(5);
        treeGrid.scrollToIndex(5);
        ui.add(treeGrid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollCall("scrollToIndex", 5);
    }

    @Test
    void scrollToIndexPath_afterAttach_schedulesJsExecution() {
        ui.add(treeGrid);
        treeGrid.scrollToIndex(1, 2);
        treeGrid.scrollToIndex(1, 2);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollCall("scrollToIndex", 1, 2);
    }

    @Test
    void scrollToIndexPath_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToIndex(1, 2);
        treeGrid.scrollToIndex(1, 2);
        ui.add(treeGrid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollCall("scrollToIndex", 1, 2);
    }

    @Test
    void scrollToEnd_afterAttach_schedulesJsExecution() {
        ui.add(treeGrid);
        treeGrid.scrollToEnd();
        treeGrid.scrollToEnd();
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollCall("scrollToIndex", -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1);
    }

    @Test
    void scrollToEnd_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToEnd();
        treeGrid.scrollToEnd();
        ui.add(treeGrid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollCall("scrollToIndex", -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1);
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
        assertSingleJavaScriptScrollCall("scrollToIndex", -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1);
    }

    /**
     * @param expectedCall
     *            the function name followed by the arguments of the function
     */
    private void assertSingleJavaScriptScrollCall(Object... expectedCall) {
        Assertions.assertEquals(List.of(List.of(expectedCall)),
                getJavaScriptScrollCalls());
    }

    /**
     * Scrolling is a call of a function named after it.
     *
     * @return the scroll calls, each as the function name followed by the
     *         arguments of the function
     */
    private List<List<Object>> getJavaScriptScrollCalls() {
        return ui
                .dumpJsCalls(treeGrid.getElement(),
                        Element.CallFunctionJs.class)
                .stream().map(JsCall::flattenArguments)
                .filter(call -> ((String) call.get(0)).contains("scroll"))
                .toList();
    }
}
