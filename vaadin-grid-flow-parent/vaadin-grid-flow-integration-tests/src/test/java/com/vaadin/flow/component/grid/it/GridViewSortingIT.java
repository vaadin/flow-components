/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/sorting")
public class GridViewSortingIT extends AbstractComponentIT {

    private GridElement grid;
    private CheckboxElement multiSortToggle;
    private CheckboxElement multiSortPriorityToggle;
    private TestBenchElement invertSortDirections;
    private TestBenchElement resetSortDirections;
    private TestBenchElement sortByAgeThenName;
    private TestBenchElement message;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id("grid-sortable-columns");
        multiSortToggle = $(CheckboxElement.class).id("grid-multi-sort-toggle");
        multiSortPriorityToggle = $(CheckboxElement.class)
                .id("grid-multi-sort-priority-toggle");
        invertSortDirections = $("button")
                .id("grid-sortable-columns-invert-sortings");
        resetSortDirections = $("button")
                .id("grid-sortable-columns-reset-sortings");
        sortByAgeThenName = $("button").id("grid-sortable-columns-sort-by-two");
        message = $("*").id("grid-sortable-columns-message");
    }

    @Test
    public void gridWithSorting() {
        scrollToElement(grid);

        getCellContent(grid.getHeaderCell(0)).click();
        assertSortMessageEquals(QuerySortOrder.asc("firstName").build(), true);
        getCellContent(grid.getHeaderCell(2)).click();
        assertSortMessageEquals(
                QuerySortOrder.asc("street").thenAsc("number").build(), true);
        getCellContent(grid.getHeaderCell(2)).click();
        assertSortMessageEquals(
                QuerySortOrder.desc("street").thenDesc("number").build(), true);
        getCellContent(grid.getHeaderCell(2)).click();
        assertSortMessageEquals(Collections.emptyList(), true);

        // enable multi sort
        multiSortToggle.click();
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(1)).click();
        assertSortMessageEquals(
                QuerySortOrder.asc("age").thenAsc("firstName").build(), true);
    }

    @Test
    public void gridWithSorting_switchColumnSorting() {
        scrollToElement(grid);

        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(0)).click();

        Assert.assertEquals(
                "Current sort order: . Sort originates from the client: true.",
                message.getText());
    }

    @Test
    public void gridWithSorting_invertAndResetSortings() {
        scrollToElement(grid);

        getCellContent(grid.getHeaderCell(0)).click();
        assertSortMessageEquals(QuerySortOrder.asc("firstName").build(), true);

        invertSortDirections.click();
        assertSortMessageEquals(QuerySortOrder.desc("firstName").build(),
                false);

        invertSortDirections.click();
        assertSortMessageEquals(QuerySortOrder.asc("firstName").build(), false);

        resetSortDirections.click();
        assertSortMessageEquals(Collections.emptyList(), false);

        // enable multi sort
        multiSortToggle.click();
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(1)).click();
        assertSortMessageEquals(
                QuerySortOrder.asc("age").thenAsc("firstName").build(), true);

        invertSortDirections.click();
        assertSortMessageEquals(
                QuerySortOrder.desc("age").thenDesc("firstName").build(),
                false);

        resetSortDirections.click();
        assertSortMessageEquals(Collections.emptyList(), false);
    }

    @Test
    public void gridWithSorting_toggleColumnVisibilityAndReset() {
        scrollToElement(grid);

        WebElement toggleFirstColumnButton = findElement(
                By.id("grid-sortable-columns-toggle-first"));

        getCellContent(grid.getHeaderCell(0)).click();
        assertSortMessageEquals(QuerySortOrder.asc("firstName").build(), true);

        clickElementWithJs(toggleFirstColumnButton);
        assertSortMessageEquals(Collections.emptyList(), false);

        clickElementWithJs(toggleFirstColumnButton);
        assertSortMessageEquals(Collections.emptyList(), false);

        WebElement sorter = grid.getHeaderCell(0).$("vaadin-grid-sorter")
                .first();
        Assert.assertNull(sorter.getAttribute("direction"));
    }

    @Test
    public void gridWithSorting_noMultiSort_secondSortColumnIgnored() {
        scrollToElement(grid);
        sortByAgeThenName.click();
        assertSortMessageEquals(QuerySortOrder.asc("age").build(), false);

        resetSortDirections.click();
        // enable multi sort
        multiSortToggle.click();
        sortByAgeThenName.click();
        assertSortMessageEquals(
                QuerySortOrder.asc("age").thenDesc("firstName").build(), false);
    }

    @Test
    public void gridWithSorting_multiSortPriorityAppend() {
        scrollToElement(grid);
        // enable multi sort
        multiSortToggle.click();
        // set multi-sort priority to append
        multiSortPriorityToggle.click();

        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(1)).click();

        assertSortMessageEquals(
                QuerySortOrder.asc("firstName").thenAsc("age").build(), true);

        getCellContent(grid.getHeaderCell(1)).click();

        assertSortMessageEquals(
                QuerySortOrder.asc("firstName").thenDesc("age").build(), true);
    }

    @Test
    public void multiSortPrepend_sortByAgeThenFirstName_sortIndicatorsOrderedByAgeThenFirstName() {
        scrollToElement(grid);
        // enable multi sort
        multiSortToggle.click();
        sortByAgeThenName.click();

        List<QuerySortOrder> expectedSortOrder = QuerySortOrder.asc("Age")
                .thenDesc("Name").build();
        assertSortIndicatorOrder(expectedSortOrder);
    }

    @Test
    public void multiSortAppend_sortByAgeThenFirstName_sortIndicatorsOrderedByAgeThenFirstName() {
        scrollToElement(grid);
        // enable multi sort
        multiSortToggle.click();
        // set multi-sort priority to append
        multiSortPriorityToggle.click();
        sortByAgeThenName.click();

        List<QuerySortOrder> expectedSortOrder = QuerySortOrder.asc("Age")
                .thenDesc("Name").build();
        assertSortIndicatorOrder(expectedSortOrder);
    }

    private void assertSortMessageEquals(List<QuerySortOrder> querySortOrders,
            boolean fromClient) {
        String sortOrdersString = querySortOrders.stream()
                .map(querySortOrder -> String.format(
                        "{sort property: %s, direction: %s}",
                        querySortOrder.getSorted(),
                        querySortOrder.getDirection()))
                .collect(Collectors.joining(", "));
        Assert.assertEquals(String.format(
                "Current sort order: %s. Sort originates from the client: %s.",
                sortOrdersString, fromClient), message.getText());
    }

    private WebElement getCellContent(GridTHTDElement cell) {
        return (WebElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0].firstElementChild;",
                cell);
    }

    private void assertSortIndicatorOrder(
            List<QuerySortOrder> querySortOrders) {
        List<TestBenchElement> sorters = grid.$("vaadin-grid-sorter")
                .hasAttribute("direction").all();

        querySortOrders.forEach((querySortOrder) -> {
            // Lookup sorter for column
            String columnName = querySortOrder.getSorted();
            TestBenchElement columnSorter = sorters.stream()
                    .filter(sorter -> sorter.getText().startsWith(columnName))
                    .findFirst().orElse(null);
            Assert.assertNotNull(
                    "Could not find sorter for column: " + columnName,
                    columnSorter);

            // Check sort direction attribute
            SortDirection direction = querySortOrder.getDirection();
            String directionValue = direction == SortDirection.ASCENDING ? "asc"
                    : "desc";
            Assert.assertEquals(directionValue,
                    columnSorter.getAttribute("direction"));

            // Check order part displays correct order value
            String orderValue = String
                    .valueOf(querySortOrders.indexOf(querySortOrder) + 1);
            TestBenchElement orderElement = columnSorter.$("*")
                    .attribute("part", "order").first();
            Assert.assertEquals(orderValue, orderElement.getText());
        });
    }
}
