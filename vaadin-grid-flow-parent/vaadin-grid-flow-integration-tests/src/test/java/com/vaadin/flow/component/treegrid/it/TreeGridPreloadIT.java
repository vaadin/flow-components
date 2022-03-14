/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-preload")
public class TreeGridPreloadIT extends AbstractTreeGridIT {

    private final int EAGER_FETCH_VIEWPORT_SIZE_ESTIMATE = 40;
    private TextFieldElement requestCount;
    private TextFieldElement dataProviderFetchCount;
    private ButtonElement requestCountReset;
    private TextAreaElement receivedParents;

    private void open(List<Integer> expandedRootIndexes,
            SortDirection sortDirection, Integer nodesPerLevel, Integer depth,
            Integer pageSize) {
        String url = getRootURL() + getTestPath() + "/?foo=bar";

        if (expandedRootIndexes.size() > 0) {
            String expandedRootIndexesString = expandedRootIndexes.stream()
                    .map(Object::toString).reduce((a, b) -> a + "," + b)
                    .orElse("");
            url += "&expandedRootIndexes=" + expandedRootIndexesString;
        }

        if (sortDirection != null) {
            url += "&sortDirection=" + sortDirection.name().toLowerCase();
        }

        if (nodesPerLevel != null) {
            url += "&nodesPerLevel=" + nodesPerLevel;
        }

        if (depth != null) {
            url += "&depth=" + depth;
        }

        if (pageSize != null) {
            url += "&pageSize=" + pageSize;
        }

        getDriver().get(url);
        waitForDevServer();

        setupTreeGrid();
        requestCount = $(TextFieldElement.class).id("request-count");
        dataProviderFetchCount = $(TextFieldElement.class).id("fetch-count");
        requestCountReset = $(ButtonElement.class).id("request-count-reset");
        receivedParents = $(TextAreaElement.class).id("received-parents");
    }

    private boolean parentItemsReceived(String parentId) {
        return Arrays.stream(receivedParents.getValue().split("\n"))
                .anyMatch(parentId::equals);
    }

    @Test
    public void firstExpanded_shouldHaveItemRecursivelyExpanded() {
        open(Arrays.asList(0), null, null, null, null);
        verifyRow(0, "/0/0");
        verifyRow(4, "/0/0/1/0/2/0/3/0/4/0");
    }

    @Test
    public void firstExpanded_shouldPreLoadDataForExpandedChildren() {
        open(Arrays.asList(0), null, null, null, null);
        Assert.assertEquals("1", requestCount.getValue());
    }

    @Test
    public void firstExpanded_shouldOptimizeDataProviderFetches() {
        open(Arrays.asList(0), null, null, null, null);
        Assert.assertEquals("55", dataProviderFetchCount.getValue());
    }

    @Test
    public void firstExpanded_shouldNotHaveDataForExpandedRootItemsOutsideViewportEstimate() {
        open(Arrays.asList(0), null, null, null, null);
        Assert.assertFalse(parentItemsReceived("/0/1"));
    }

    @Test
    public void firstExpanded_shouldNotHaveDataForExpandedChildrenOutsideViewportEstimate() {
        open(Arrays.asList(0), null, null, null, null);
        Assert.assertFalse(parentItemsReceived("/0/0/1/1"));
    }

    @Test
    public void firstExpanded_scrollByViewportEstimate_shouldHaveItemRecursivelyExpanded() {
        open(Arrays.asList(0), null, null, null, null);
        getTreeGrid().scrollToRow(EAGER_FETCH_VIEWPORT_SIZE_ESTIMATE);
        verifyRow(EAGER_FETCH_VIEWPORT_SIZE_ESTIMATE + 4,
                "/0/0/1/1/2/0/3/0/4/0");
    }

    @Test
    public void firstExpanded_scrollByViewportEstimate_shouldPreLoadDataForExpandedChildren() {
        open(Arrays.asList(0), null, null, null, null);
        requestCountReset.click();

        getTreeGrid().scrollToRow(EAGER_FETCH_VIEWPORT_SIZE_ESTIMATE);
        Assert.assertEquals("1", requestCount.getValue());
    }

    @Test
    public void firstExpanded_reExpand_shouldPreLoadDataForExpandedChildren() {
        open(Arrays.asList(0), null, null, null, null);
        requestCountReset.click();

        getTreeGrid().collapseWithClick(0);
        getTreeGrid().expandWithClick(0);
        // Expanding a recursively expanded parent doesn't yet trigger a preload
        // so a second request is made from client.
        Assert.assertEquals("2", requestCount.getValue());
    }

    @Test
    public void firstExpanded_reExpand_shouldHaveItemRecursivelyExpanded() {
        open(Arrays.asList(0), null, null, null, null);
        getTreeGrid().collapseWithClick(0);
        getTreeGrid().expandWithClick(0);
        verifyRow(0, "/0/0");
        verifyRow(4, "/0/0/1/0/2/0/3/0/4/0");
    }

    @Test
    public void firstExpanded_reExpandChild_shouldPreLoadDataForExpandedChildren() {
        open(Arrays.asList(0), null, null, null, null);
        requestCountReset.click();

        getTreeGrid().collapseWithClick(2);
        getTreeGrid().expandWithClick(2);
        Assert.assertEquals("1", requestCount.getValue());
    }

    @Test
    public void firstExpanded_reExpandChild_shouldHaveItemRecursivelyExpanded() {
        open(Arrays.asList(0), null, null, null, null);
        getTreeGrid().collapseWithClick(1);
        getTreeGrid().expandWithClick(1);
        verifyRow(0, "/0/0");
        verifyRow(4, "/0/0/1/0/2/0/3/0/4/0");
    }

    @Test
    public void firstExpanded_smallPageSize_shouldHaveAllChildItemsVisible() {
        open(Arrays.asList(0), null, null, null, 2);
        verifyRow(6, "/0/0/1/0/2/0/3/0/4/2");
    }

    @Test
    public void secondExpanded_shouldNotHaveDataForNonExpandedRootItems() {
        open(Arrays.asList(1), null, null, null, null);
        Assert.assertTrue(parentItemsReceived("/0/1"));
        Assert.assertFalse(parentItemsReceived("/0/0"));
    }

    @Test
    public void multipleExpanded_shouldNotHaveDataForExpandedRootItemsOutsideViewportEstimate() {
        open(Arrays.asList(0, 2), null, null, null, null);
        Assert.assertTrue(parentItemsReceived("/0/0"));
        Assert.assertFalse(parentItemsReceived("/0/2"));
    }

    @Test
    public void firstExpanded_initiallySorted_shouldHaveItemRecursivelyExpanded() {
        open(Arrays.asList(0), SortDirection.DESCENDING, null, null, null);
        verifyRow(6, "/0/0/1/2/2/2/3/2/4/2");
    }

    @Test
    public void firstExpanded_initiallySorted_shouldPreLoadDataForExpandedChildren() {
        open(Arrays.asList(0), SortDirection.DESCENDING, null, null, null);
        Assert.assertEquals("1", requestCount.getValue());
    }

    @Test
    public void expandedOnSecondPage_scrollToIndex_shouldHaveItemExpanded() {
        open(Arrays.asList(70), null, 100, 1, null);
        verifyRow(71, "/0/70/1/0");
    }

    @Test
    public void expandedOnSecondPage_scrollToIndex_shouldPreLoadDataForExpandedChildren() {
        open(Arrays.asList(70), null, 100, 1, null);
        requestCountReset.click();

        getTreeGrid().scrollToRow(70);
        Assert.assertEquals("1", requestCount.getValue());
    }

    private void verifyRow(int rowActualIndex, String itemId) {
        Assert.assertEquals("Invalid id at index " + rowActualIndex, itemId,
                getTreeGrid().getCell(rowActualIndex, 0).getText());
    }

    @Test
    public void multipleExpanded_shouldExpandWhenScrolledTo() {
        open(Arrays.asList(0, 2), null, null, null, null);

        waitUntil(w -> {
            getTreeGrid().scrollToRow(Integer.MAX_VALUE);
            return "/0/2/1/2/2/2/3/2/4/2".equals(getTreeGrid()
                    .getCell(getTreeGrid().getLastVisibleRowIndex(), 0)
                    .getText());
        });
    }

    @Test
    public void multipleExpanded_dynamicallySorted_shouldHaveItemRecursivelyExpanded() {
        open(Arrays.asList(0, 2), SortDirection.ASCENDING, null, null, null);

        getTreeGrid().$("vaadin-grid-sorter").first().click();
        verifyRow(4, "/0/2/1/2/2/2/3/2/4/2");
    }

    @Test
    public void multipleExpanded_dynamicallySorted_shouldPreLoadDataForExpandedChildren() {
        open(Arrays.asList(0, 2), SortDirection.ASCENDING, null, null, null);
        requestCountReset.click();

        getTreeGrid().$("vaadin-grid-sorter").first().click();
        Assert.assertEquals("1", requestCount.getValue());
    }

}
