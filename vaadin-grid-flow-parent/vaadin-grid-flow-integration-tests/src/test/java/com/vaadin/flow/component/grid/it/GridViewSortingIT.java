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
package com.vaadin.flow.component.grid.it;

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

    @Before
    public void init() {
        open();
    }

    @Test
    public void gridWithSorting() {
        GridElement grid = $(GridElement.class).id("grid-sortable-columns");
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
        clickElementWithJs(findElement(By.id("grid-multi-sort-toggle")));
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(1)).click();
        assertSortMessageEquals(
                QuerySortOrder.asc("age").thenAsc("firstName").build(), true);
    }

    @Test
    public void gridWithSorting_switchColumnSorting() {
        GridElement grid = $(GridElement.class).id("grid-sortable-columns");
        scrollToElement(grid);

        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(0)).click();

        Assert.assertEquals(
                "Current sort order: . Sort originates from the client: true.",
                findElement(By.id("grid-sortable-columns-message")).getText());
    }

    @Test
    public void gridWithSorting_invertAndResetSortings() {
        GridElement grid = $(GridElement.class).id("grid-sortable-columns");
        scrollToElement(grid);

        WebElement invertButton = findElement(
                By.id("grid-sortable-columns-invert-sortings"));
        WebElement resetButton = findElement(
                By.id("grid-sortable-columns-reset-sortings"));

        getCellContent(grid.getHeaderCell(0)).click();
        assertSortMessageEquals(QuerySortOrder.asc("firstName").build(), true);

        clickElementWithJs(invertButton);
        assertSortMessageEquals(QuerySortOrder.desc("firstName").build(),
                false);

        clickElementWithJs(invertButton);
        assertSortMessageEquals(QuerySortOrder.asc("firstName").build(), false);

        clickElementWithJs(resetButton);
        assertSortMessageEquals(Collections.emptyList(), false);

        // enable multi sort
        clickElementWithJs(findElement(By.id("grid-multi-sort-toggle")));
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(1)).click();
        assertSortMessageEquals(
                QuerySortOrder.asc("age").thenAsc("firstName").build(), true);
        clickElementWithJs(invertButton);
        assertSortMessageEquals(
                QuerySortOrder.desc("age").thenDesc("firstName").build(),
                false);

        clickElementWithJs(resetButton);
        assertSortMessageEquals(Collections.emptyList(), false);

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
                sortOrdersString, fromClient),
                findElement(By.id("grid-sortable-columns-message")).getText());
    }

    private WebElement getCellContent(GridTHTDElement cell) {
        return (WebElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0].firstElementChild;",
                cell);
    }

}
