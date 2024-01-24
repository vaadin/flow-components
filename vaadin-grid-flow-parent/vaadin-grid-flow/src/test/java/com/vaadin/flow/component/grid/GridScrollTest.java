/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.vaadin.flow.internal.Range;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

public class GridScrollTest {

    private Grid<String> grid;

    @Before
    public void setUp() {
        grid = new Grid<>();
        grid.setPageSize(50);
    }

    @Test
    public void scrollToStart_preloadOnePage() {
        grid.scrollToIndex(0);
        Assert.assertEquals("0-50", getRequestedRange(grid));
    }

    @Test
    public void scrollToEnd_preloadOnePage() {
        grid.scrollToIndex(950);
        Assert.assertEquals("950-1000", getRequestedRange(grid));
    }

    @Test
    public void scrollToStartOfPage_preloadOnePage() {
        grid.scrollToIndex(500);
        Assert.assertEquals("500-550", getRequestedRange(grid));
    }

    @Test
    public void scrollToSecondIndexOfPage_preloadOnePage() {
        grid.scrollToIndex(501);
        Assert.assertEquals("500-550", getRequestedRange(grid));
    }

    @Test
    public void scrollToSecondLastIndexOfPage_preloadTwoPages() {
        grid.scrollToIndex(499);
        Assert.assertEquals("450-550", getRequestedRange(grid));
    }

    @Test
    public void smallPageSize_scrollToIndex_preloadMultiplePages() {
        grid.setPageSize(5);
        grid.scrollToIndex(499);
        Assert.assertEquals("495-540", getRequestedRange(grid));
    }

    @Test
    public void listDataProvider_scrollToItem_loadsCorrectRange() {
        List<String> items = IntStream.range(0, 1000).mapToObj(String::valueOf)
                .toList();

        grid.setItems(items);

        grid.scrollToItem(items.get(500));
        Assert.assertEquals("500-550", getRequestedRange(grid));
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
        Assert.assertEquals("500-550", getRequestedRange(grid));
    }

    @Test
    public void scrollToNonExistingItem_noSuchElementExceptionThrown() {
        List<String> items = IntStream.range(0, 10).mapToObj(String::valueOf)
                .toList();

        grid.setItems(items);

        Assert.assertThrows(NoSuchElementException.class,
                () -> grid.scrollToItem("Not present"));
    }

    private String getRequestedRange(Grid<String> grid) {
        try {
            var communicator = grid.getDataCommunicator();
            var requestedRangeField = communicator.getClass()
                    .getDeclaredField("requestedRange");
            requestedRangeField.setAccessible(true);
            Range requestedRange = (Range) requestedRangeField
                    .get(communicator);
            return requestedRange.getStart() + "-" + requestedRange.getEnd();

        } catch (Exception e) {
            return "";
        }
    }

}
