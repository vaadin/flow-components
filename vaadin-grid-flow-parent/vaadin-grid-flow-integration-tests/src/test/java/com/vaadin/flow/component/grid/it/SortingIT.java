/*
 * Copyright 2000-2019 Vaadin Ltd.
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
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("sorting")
public class SortingIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
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
    public void setInitialSortOrder_changeOrderFromServer_sortIndicatorsUpdated() {
        findElement(By.id("sort-by-age")).click();
        assertAscendingSorter("Age");
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
        Assert.assertEquals(expectedColumnHeader, sorter.getText());
    }

}
