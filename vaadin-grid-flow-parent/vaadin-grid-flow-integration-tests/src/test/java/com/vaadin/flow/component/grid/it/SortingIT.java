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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/sorting")
public class SortingIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id("sorting-grid");
    }

    @Test
    public void setInitialSortOrder_dataSorted() {
        Assert.assertEquals("A", grid.getCell(0, 0).getText());
        Assert.assertEquals("B", grid.getCell(1, 0).getText());
    }

    @Test
    public void setInitialSortOrder_sortIndicatorsUpdated() {
        assertAscendingSorter("Name");
    }

    @Test
    public void setInitialSortOrder_changeOrderFromServer_dataSorted() {
        findElement(By.id("sort-by-age")).click();
        Assert.assertEquals("B", grid.getCell(0, 0).getText());
        Assert.assertEquals("A", grid.getCell(1, 0).getText());
    }

    @Test
    @Ignore("Fix reverted for https://github.com/vaadin/vaadin-flow-components/issues/427")
    public void setInitialSortOrderGridHidden_showGrid_dataPresentAndSorted() {
        findElement(By.id("sort-hidden-by-age")).click();
        findElement(By.id("show-hidden-grid")).click();
        Assert.assertEquals("B",
                $(GridElement.class).id("hidden-grid").getCell(0, 0).getText());
        Assert.assertEquals("A",
                $(GridElement.class).id("hidden-grid").getCell(1, 0).getText());
    }

    @Test
    public void setInitialSortOrder_changeOrderFromServer_sortIndicatorsUpdated() {
        findElement(By.id("sort-by-age")).click();
        assertAscendingSorter("Age");
        findElement(By.id("reorder-button")).click();
        assertAscendingSorter("Age");
    }

    @Test
    public void setInitialSortOrder_sorterAriaLabels() {
        findElement(By.id("sort-by-age")).click();
        List<TestBenchElement> sorters = grid.$("vaadin-grid-sorter").all();
        // The first sorter uses a ComponentRenderer so an aria-label can't be
        // generated
        Assert.assertEquals(false, sorters.get(0).hasAttribute("aria-label"));
        Assert.assertEquals("Sort by Age",
                sorters.get(1).getAttribute("aria-label"));
    }

    @Test
    public void setInitialSortOrder_updateHeaderText_sortIndicatorsRemain() {
        findElement(By.id("sort-by-age")).click();
        assertAscendingSorter("Age");
        findElement(By.id("change-header-text")).click();
        assertAscendingSorter("Age (updated)");
    }

    @Test
    public void setInitialSortOrder_updateHeaderTextComponent_sortIndicatorsRemain() {
        findElement(By.id("sort-by-age")).click();
        assertAscendingSorter("Age");
        findElement(By.id("change-header-text-component")).click();
        assertAscendingSorter("Age (updated)");
    }

    @Test
    public void emptyGrid_sort_noClientErrors() {
        findElement(By.id("clear-items")).click();
        grid.findElements(By.tagName("vaadin-grid-sorter")).get(0).click();
        checkLogsForErrors();
    }

    @Test
    public void indicatorsSortStateNumbersAndDirectionsAndContentOfRow() {
        WebElement btnAttach = findElement(By.id("btn-attach"));
        WebElement btnRemove = findElement(By.id("btn-detach"));
        GridElement sortingGridElement = $(GridElement.class)
                .id("sorting-grid");
        findElement(By.id("sort-by-age")).click();
        sortingGridElement.findElements(By.tagName("vaadin-grid-sorter")).get(0)
                .click();

        String textAgeColumnBeforeReattch = sortingGridElement.getCell(0, 1)
                .getText();
        Assert.assertEquals("asc",
                sortingGridElement
                        .findElements(By.tagName("vaadin-grid-sorter")).get(0)
                        .getAttribute("direction"));
        String sortStateNumberNameColumn = sortingGridElement
                .findElements(By.tagName("vaadin-grid-sorter")).get(0)
                .getAttribute("_order");
        Assert.assertEquals("asc",
                sortingGridElement
                        .findElements(By.tagName("vaadin-grid-sorter")).get(1)
                        .getAttribute("direction"));
        String sortStateNumberAgeColumn = sortingGridElement
                .findElements(By.tagName("vaadin-grid-sorter")).get(1)
                .getAttribute("_order");
        // Detach
        btnRemove.click();
        // Reattach
        btnAttach.click();

        sortingGridElement = $(GridElement.class).id("sorting-grid");

        Assert.assertEquals("asc",
                sortingGridElement
                        .findElements(By.tagName("vaadin-grid-sorter")).get(0)
                        .getAttribute("direction"));

        Assert.assertEquals("asc",
                sortingGridElement
                        .findElements(By.tagName("vaadin-grid-sorter")).get(1)
                        .getAttribute("direction"));

        String sortStateNumberAgeColumnAfterDetach = sortingGridElement
                .findElements(By.tagName("vaadin-grid-sorter")).get(1)
                .getAttribute("_order");

        String sortStateNumberNameColumnAfterDetach = sortingGridElement
                .findElements(By.tagName("vaadin-grid-sorter")).get(0)
                .getAttribute("_order");
        String textAgeColumnAfterReattch = sortingGridElement.getCell(0, 1)
                .getText();
        Assert.assertEquals(textAgeColumnBeforeReattch,
                textAgeColumnAfterReattch);
        Assert.assertEquals(sortStateNumberAgeColumn,
                sortStateNumberAgeColumnAfterDetach);
        Assert.assertEquals(sortStateNumberNameColumn,
                sortStateNumberNameColumnAfterDetach);
    }

    private void assertAscendingSorter(String expectedColumnHeader) {
        List<TestBenchElement> sorters = grid.$("vaadin-grid-sorter")
                .hasAttribute("direction").all();
        Assert.assertEquals("Only one column should be sorted. "
                + "Expected a single instance of <vaadin-grid-sorter> with 'direction' attribute.",
                1, sorters.size());
        TestBenchElement sorter = sorters.get(0);
        Assert.assertEquals("Expected ascending sort order.", "asc",
                sorter.getAttribute("direction"));
        Assert.assertTrue(sorter.getText().startsWith(expectedColumnHeader));
    }

}
