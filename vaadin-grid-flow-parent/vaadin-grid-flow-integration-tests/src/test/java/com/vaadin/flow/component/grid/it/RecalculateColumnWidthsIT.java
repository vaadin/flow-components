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
 *
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.List;

@TestPath("vaadin-grid/recalculate-column-widths")
public class RecalculateColumnWidthsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("grid"));
    }

    @Test
    public void columnsRecalculateAfterDataChange() {
        GridElement grid = $(GridElement.class).id("grid");
        TestBenchElement changeDataButton = $(TestBenchElement.class)
                .id("change-data-button");

        GridTHTDElement cell = grid.getCell(1, 1);

        Integer scrollWidthBefore = cell.getPropertyInteger("scrollWidth");

        changeDataButton.click();

        Integer scrollWidthAfter = cell.getPropertyInteger("scrollWidth");
        Integer offsetWidthAfter = cell.getPropertyInteger("offsetWidth");

        Assert.assertTrue("Scroll width should have increased",
                scrollWidthAfter > scrollWidthBefore);
        Assert.assertTrue("Cell content should not be cut off with ellipsis",
                offsetWidthAfter <= scrollWidthAfter);
    }

    @Test
    public void columnsRecalculateAfterVisibilityChange() {
        GridElement grid = $(GridElement.class).id("grid");
        TestBenchElement setCol2InvisibleButton = $(TestBenchElement.class)
                .id("set-column-2-invisible-button");
        TestBenchElement setCol2VisibleButton = $(TestBenchElement.class)
                .id("set-column-2-visible-button");

        List<GridColumnElement> visibleColumnsBefore = grid.getVisibleColumns();
        Assert.assertEquals(3, visibleColumnsBefore.size());

        int column2SizeBefore = grid.getCell(0, visibleColumnsBefore.get(1))
                .getSize().getWidth();

        setCol2InvisibleButton.click();
        setCol2VisibleButton.click();

        List<GridColumnElement> visibleColumnsAfter = grid.getVisibleColumns();
        Assert.assertEquals(3, visibleColumnsAfter.size());
        Assert.assertEquals(column2SizeBefore, grid
                .getCell(0, visibleColumnsAfter.get(1)).getSize().getWidth());
    }
}
