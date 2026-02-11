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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.server.VaadinSession;

public class ScrollToIndexTest {

    private UI ui;
    private TreeGrid<String> treeGrid;

    @Before
    public void setup() {
        ui = new UI();
        ui.getInternals().setSession(Mockito.mock(VaadinSession.class));
        treeGrid = new TreeGrid<>();
    }

    @Test
    public void scrollToIndex_afterAttach_schedulesJsExecution() {
        ui.add(treeGrid);

        treeGrid.scrollToIndex(5);

        assertPendingScrollToIndexInvocation();
    }

    @Test
    public void scrollToIndex_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToIndex(5);

        ui.add(treeGrid);

        assertPendingScrollToIndexInvocation();
    }

    @Test
    public void scrollToIndexPath_afterAttach_schedulesJsExecution() {
        ui.add(treeGrid);

        treeGrid.scrollToIndex(1, 2);

        assertPendingScrollToIndexPathInvocation();
    }

    @Test
    public void scrollToIndexPath_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToIndex(1, 2);

        ui.add(treeGrid);

        assertPendingScrollToIndexPathInvocation();
    }

    @Test
    public void scrollToEnd_afterAttach_schedulesJsExecution() {
        ui.add(treeGrid);

        treeGrid.scrollToEnd();

        assertPendingScrollToEndInvocation();
    }

    @Test
    public void scrollToEnd_beforeAttach_thenAttach_schedulesJsExecution() {
        treeGrid.scrollToEnd();

        ui.add(treeGrid);

        assertPendingScrollToEndInvocation();
    }

    private void assertPendingScrollToIndexInvocation() {
        List<PendingJavaScriptInvocation> pendingInvocations = getPendingJavaScriptInvocations();

        long scrollToIndexCount = pendingInvocations.stream().filter(inv -> inv
                .getInvocation().getExpression().contains("scrollToIndex"))
                .count();

        Assert.assertEquals(
                "Expected one scrollToIndex JS invocation to be scheduled", 1,
                scrollToIndexCount);
    }

    private void assertPendingScrollToIndexPathInvocation() {
        List<PendingJavaScriptInvocation> pendingInvocations = getPendingJavaScriptInvocations();

        long scrollToIndexCount = pendingInvocations.stream().filter(inv -> inv
                .getInvocation().getExpression().contains("scrollToIndex(1,2)"))
                .count();

        Assert.assertEquals(
                "Expected one scrollToIndex path JS invocation to be scheduled",
                1, scrollToIndexCount);
    }

    private void assertPendingScrollToEndInvocation() {
        List<PendingJavaScriptInvocation> pendingInvocations = getPendingJavaScriptInvocations();

        long scrollToEndCount = pendingInvocations.stream()
                .filter(inv -> inv.getInvocation().getExpression()
                        .contains("Array(10).fill(Infinity)"))
                .count();

        Assert.assertEquals(
                "Expected one scrollToEnd JS invocation to be scheduled", 1,
                scrollToEndCount);
    }

    private List<PendingJavaScriptInvocation> getPendingJavaScriptInvocations() {
        fakeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
