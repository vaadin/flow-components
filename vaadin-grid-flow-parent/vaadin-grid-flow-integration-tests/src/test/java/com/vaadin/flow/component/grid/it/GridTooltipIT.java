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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-grid/tooltip")
public class GridTooltipIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void tooltipGenerator_shouldNotProduceErrors() {
        checkLogsForErrors(
                msg -> !msg.contains("Cannot read properties of undefined"));
    }

    @Test
    @Ignore
    public void hoverOverTooltipColumnCell_showTooltip() {
        var grid = $(GridElement.class).first();
        flushScrolling(grid);
        showTooltip(grid.getCell("Jack"));
        Assert.assertEquals("First name of the person is Jack",
                getActiveTooltipText());
    }

    @Test
    @Ignore
    public void toggleGrid_hoverOverTooltipColumnCell_showTooltip() {
        // Remove the grid
        clickElementWithJs("toggle-grid-button");
        // Add the grid
        clickElementWithJs("toggle-grid-button");

        var grid = $(GridElement.class).first();
        flushScrolling(grid);
        showTooltip(grid.getCell("Jack"));
        Assert.assertEquals("First name of the person is Jack",
                getActiveTooltipText());
    }

    @Test
    @Ignore
    public void dynamicallyAddGenerator_hoverOverTooltipColumnCell_showTooltip() {
        var grid = $(GridElement.class).first();
        clickElementWithJs("set-age-tooltip-button");
        flushScrolling(grid);
        showTooltip(grid.getCell("33"));
        Assert.assertEquals("Age of the person is 33", getActiveTooltipText());
    }

    @Test
    public void addGeneratorForAllGrid() {
        var grid = $(GridElement.class).id("grid-with-tooltips");
        scrollToElement(grid);
        clickElementWithJs("set-grid-tooltip-button");
        for (int col = 0; col < 13; col++) {
            for (int row = 0; row < 2; row++) {
                String firstName = row == 0 ? "Jack" : "Jill";
                showTooltip(grid.getCell(row, col));
                // check all columns, except column 5 have grid's tooltip
                if (col != 5) {
                    Assert.assertEquals("Grid's tooltip! " + firstName,
                            getActiveTooltipText());
                } else {
                    // check that previously set column (5) tooltip was not
                    // replaced
                    Assert.assertEquals(
                            "First name of the person is " + firstName,
                            getActiveTooltipText());
                }
            }
        }
    }

    @Test
    public void columnTooltipOverridesGridTooltip() {
        var grid = $(GridElement.class).id("grid-with-tooltips");
        scrollToElement(grid);
        // set grid tooltip
        clickElementWithJs("set-grid-tooltip-button");
        // check column has grid's tooltip
        showTooltip(grid.getCell(1, 1));
        Assert.assertEquals("Grid's tooltip! Jill", getActiveTooltipText());
        // set column tooltip
        clickElementWithJs("set-age-tooltip-button");
        // check column now has column tooltip
        showTooltip(grid.getCell(1, 1));
        Assert.assertEquals("Age of the person is 33", getActiveTooltipText());
    }

    @Test
    public void newColumnHasGridTooltipGenerator() {
        var grid = $(GridElement.class).id("grid-with-tooltips");
        scrollToElement(grid);
        // set grid tooltip
        clickElementWithJs("set-grid-tooltip-button");
        // add new column
        clickElementWithJs("add-column-button");
        showTooltip(grid.getCell(0, 13));
        Assert.assertEquals("Grid's tooltip! Jack", getActiveTooltipText());
    }

    private void showTooltip(GridTHTDElement cell) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseenter', {bubbles:true}))",
                cell);
    }

    private String getActiveTooltipText() {
        return findElement(By.tagName("vaadin-tooltip-overlay")).getText();
    }

    /**
     * Forces the grid to remove the `scrolling` attribute on the grid scroller,
     * which would otherwise prevent a tooltip to open on mouseenter
     *
     * @param grid
     *            the grid to flush
     */
    private void flushScrolling(GridElement grid) {
        getCommandExecutor().executeScript(
                "const grid = arguments[0]; if (grid._debounceScrolling) { grid._debounceScrolling.flush() }",
                grid);
    }
}
