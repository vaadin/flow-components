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
package com.vaadin.flow.component.grid;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.internal.Range;
import com.vaadin.tests.MockUIRule;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class GridScrollToIndexTest {
    @Rule
    public MockUIRule ui = new MockUIRule();

    private Grid<String> grid;

    @Before
    public void setUp() {
        grid = new Grid<>();
        grid.setPageSize(50);
    }

    @Test
    public void scrollToStart_preloadOnePage() {
        grid.scrollToIndex(0);
        Assert.assertEquals("0-50", getViewportRange(grid));
    }

    @Test
    public void scrollToEnd_preloadOnePage() {
        grid.scrollToIndex(950);
        Assert.assertEquals("950-1000", getViewportRange(grid));
    }

    @Test
    public void scrollToStartOfPage_preloadOnePage() {
        grid.scrollToIndex(500);
        Assert.assertEquals("500-550", getViewportRange(grid));
    }

    @Test
    public void scrollToSecondIndexOfPage_preloadOnePage() {
        grid.scrollToIndex(501);
        Assert.assertEquals("500-550", getViewportRange(grid));
    }

    @Test
    public void scrollToSecondLastIndexOfPage_preloadTwoPages() {
        grid.scrollToIndex(499);
        Assert.assertEquals("450-550", getViewportRange(grid));
    }

    @Test
    public void smallPageSize_scrollToIndex_preloadMultiplePages() {
        grid.setPageSize(5);
        grid.scrollToIndex(499);
        Assert.assertEquals("495-540", getViewportRange(grid));
    }

    @Test
    public void scrollToIndex_afterAttach_schedulesJsExecution() {
        ui.add(grid);
        grid.scrollToIndex(5);
        grid.scrollToIndex(5);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex", 5);
    }

    @Test
    public void scrollToIndex_beforeAttach_thenAttach_schedulesJsExecution() {
        grid.scrollToIndex(5);
        grid.scrollToIndex(5);
        ui.add(grid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex", 5);
    }

    @Test
    public void scrollToStart_afterAttach_schedulesJsExecution() {
        ui.add(grid);
        grid.scrollToStart();
        grid.scrollToStart();
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex", 0);
    }

    @Test
    public void scrollToStart_beforeAttach_thenAttach_schedulesJsExecution() {
        grid.scrollToStart();
        grid.scrollToStart();
        ui.add(grid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex", 0);
    }

    @Test
    public void scrollToEnd_afterAttach_schedulesJsExecution() {
        ui.add(grid);
        grid.scrollToEnd();
        grid.scrollToEnd();
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex(this._flatSize)");
    }

    @Test
    public void scrollToEnd_beforeAttach_thenAttach_schedulesJsExecution() {
        grid.scrollToEnd();
        grid.scrollToEnd();
        ui.add(grid);
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex(this._flatSize)");
    }

    @Test
    public void onlyLastScrollInvocationExecuted() {
        grid.setItems("Item 0", "Item 1");
        ui.add(grid);

        grid.scrollToItem("Item 0");
        grid.scrollToIndex(1);
        grid.scrollToStart();
        grid.scrollToEnd();
        ui.fakeClientCommunication();
        assertSingleJavaScriptScrollInvocation("scrollToIndex(this._flatSize)");
    }

    private void assertSingleJavaScriptScrollInvocation(
            String expectedExpression, Object... expectedParams) {
        var invocations = getJavaScriptScrollInvocations();
        Assert.assertEquals(1, invocations.size());

        var invocation = invocations.get(0);
        Assert.assertTrue(
                invocation.getExpression().contains(expectedExpression));

        var params = invocation.getParameters();
        for (int i = 1; i < expectedParams.length; i++) {
            Assert.assertEquals(expectedParams[i], params.get(i));
        }
    }

    private List<JavaScriptInvocation> getJavaScriptScrollInvocations() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("scroll"))
                .toList();
    }

    private String getViewportRange(Grid<String> grid) {
        try {
            var communicator = grid.getDataCommunicator();
            var viewportRangeField = communicator.getClass().getSuperclass()
                    .getDeclaredField("viewportRange");
            viewportRangeField.setAccessible(true);
            Range viewportRange = (Range) viewportRangeField.get(communicator);
            return viewportRange.getStart() + "-" + viewportRange.getEnd();

        } catch (Exception e) {
            return "";
        }
    }

}
