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
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.internal.Range;
import com.vaadin.tests.MockUI;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class GridScrollToItemTest {

    private MockUI ui = new MockUI();
    private Grid<String> grid;

    @Before
    public void setUp() {
        grid = new Grid<>();
        grid.setPageSize(50);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void listDataProvider_scrollToItem_loadsCorrectRange() {
        List<String> items = IntStream.range(0, 1000).mapToObj(String::valueOf)
                .toList();

        grid.setItems(items);

        grid.scrollToItem(items.get(500));
        Assert.assertEquals("500-550", getViewportRange(grid));
    }

    @Test
    public void lazyDataProvider_noItemIndexProvider_scrollToItem_throwsUnsupportedOperationException() {
        List<String> items = IntStream.range(0, 1000).mapToObj(String::valueOf)
                .toList();

        grid.setItems(
                q -> items.stream().skip(q.getOffset()).limit(q.getLimit()));
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> grid.scrollToItem(items.get(500)));
    }

    @Test
    public void lazyDataProvider_withItemIndexProvider_scrollToItem_loadsCorrectRange() {
        List<String> items = IntStream.range(0, 1000).mapToObj(String::valueOf)
                .toList();

        grid.setItems(
                q -> items.stream().skip(q.getOffset()).limit(q.getLimit()))
                .setItemIndexProvider((item, query) -> items.indexOf(item));

        grid.scrollToItem(items.get(500));
        Assert.assertEquals("500-550", getViewportRange(grid));
    }

    @Test
    public void scrollToNonExistingItem_noSuchElementExceptionThrown() {
        List<String> items = IntStream.range(0, 10).mapToObj(String::valueOf)
                .toList();

        grid.setItems(items);

        Assert.assertThrows(NoSuchElementException.class,
                () -> grid.scrollToItem("Not present"));
    }

    @Test
    public void scrollToItem_afterAttach_schedulesJsExecution() {
        grid.setItems("Item 0", "Item 1");
        ui.add(grid);
        grid.scrollToItem("Item 0");
        grid.scrollToItem("Item 0");
        fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation("Item 0", 0);
    }

    @Test
    public void scrollToItem_beforeAttach_thenAttach_schedulesJsExecution() {
        grid.setItems("Item 0", "Item 1");
        grid.scrollToItem("Item 0");
        grid.scrollToItem("Item 0");
        ui.add(grid);
        fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation("Item 0", 0);
    }

    @Test
    public void scrollToIndex_scrollToItem_onlyScrollToItemExecuted() {
        grid.setItems("Item 0", "Item 1");
        ui.add(grid);

        grid.scrollToIndex(1);
        grid.scrollToItem("Item 0");
        fakeClientCommunication();
        assertSingleJavaScriptScrollToItemInvocation("Item 0", 0);
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private void assertSingleJavaScriptScrollToItemInvocation(
            String expectedItem, int expectedIndex) {
        var expectedItemKey = grid.getDataCommunicator().getKeyMapper()
                .key(expectedItem);

        var invocations = getJavaScriptScrollInvocations();
        Assert.assertEquals(1, invocations.size());

        var invocation = invocations.get(0);
        Assert.assertTrue(invocation.getExpression().contains("scrollToItem"));
        Assert.assertEquals(expectedItemKey, invocation.getParameters().get(1));
        Assert.assertEquals(expectedIndex, invocation.getParameters().get(2));
    }

    private List<JavaScriptInvocation> getJavaScriptScrollInvocations() {
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
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
