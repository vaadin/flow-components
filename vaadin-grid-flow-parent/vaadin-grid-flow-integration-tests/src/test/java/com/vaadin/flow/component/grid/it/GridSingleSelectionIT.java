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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.validation.constraints.AssertTrue;

@TestPath("vaadin-grid/grid-single-selection")
public class GridSingleSelectionIT extends AbstractComponentIT {

    @Test
    public void checkDeselectionAllowedByDefault() {
        open();

        // Ensure that de-selection is allowed by default
        GridElement grid = $(GridElement.class)
                .id(GridSingleSelectionPage.DESELECT_ALLOWED_GRID_ID);

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was not selected after selecting it.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue("Row 1 was still selected after de-selecting it.",
                !grid.getRow(1).isSelected());

        // Disable de-selection on the fly and test again
        $("button").id(GridSingleSelectionPage.DESELECT_ALLOWED_TOGGLE_ID)
                .click();

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was not selected after selecting it.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue(
                "Row 1 was deselected even though deselection is not allowed.",
                grid.getRow(1).isSelected());
    }

    @Test
    public void checkDeselectionDisallowedInitially() {
        open();

        // Ensure that de-selection is not possible when it has been disallowed
        // initially
        GridElement grid = $(GridElement.class)
                .id(GridSingleSelectionPage.DESELECT_DISALLOWED_GRID_ID);

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was not selected after selecting it.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue(
                "Row 1 was deselected even though deselection is not allowed.",
                grid.getRow(1).isSelected());

        // Enable de-selection on the fly and test again
        $("button").id(GridSingleSelectionPage.DESELECT_DISALLOWED_TOGGLE_ID)
                .click();

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was not selected after selecting it.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue("Row 1 was still selected after de-selecting it.",
                !grid.getRow(1).isSelected());
    }

    @Test
    public void selectItemAndSetItemsWithDeselectDisallowed() {
        open();

        // De-selection is not allowed(deselectAllowed is false) and then
        // setting items for grid
        GridElement grid = $(GridElement.class)
                .id(GridSingleSelectionPage.ITEMS_GRID);

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was selected after selecting it.",
                grid.getRow(1).isSelected());
        // Set Items again by clicking the button
        $("button").id(GridSingleSelectionPage.SET_ITEMS).click();
        $("button").id(GridSingleSelectionPage.SET_ITEMS).click();
    }

    @Test
    public void selectAnotherItemWithDeselectDisallowed() {
        open();

        // De-selection is not allowed(deselectAllowed is false) and then
        // setting items for grid
        GridElement grid = $(GridElement.class)
                .id(GridSingleSelectionPage.ITEMS_GRID);

        grid.getRow(0).select();

        Assert.assertTrue("Row 1 was selected after selecting it.",
                grid.getRow(0).isSelected());

        WebElement text1 = findElement(By.id("item1"));
        Assert.assertTrue("Row 1 is selected", text1.isDisplayed());

        grid.getRow(0).deselect();

        grid.getRow(1).select();

        Assert.assertTrue("Row 2 was selected after selecting it.",
                grid.getRow(1).isSelected());

        WebElement text2 = findElement(By.id("item2"));
        Assert.assertTrue("Row 2 is selected", text2.isDisplayed());
    }

    /**
     * Test that aria-multiselectable and aria-selected should NOT be present
     * when SelectionMode is set to NONE.
     */
    @Test
    public void testAriaSelectionModeNone() {
        open();
        GridElement grid = $(GridElement.class).id(GridSingleSelectionPage.DESELECT_ALLOWED_GRID_ID);
        TestBenchElement table = grid.$("table").first();
        // table should not have aria-multiselectable attribute
        Assert.assertFalse(table.hasAttribute("aria-multiselectable"));

        for (int i = 0; i < grid.getRowCount(); i++) {
            GridTRElement row = grid.getRow(i);
            Assert.assertFalse(row.hasAttribute("aria-selected"));
        }
    }

    /**
     * Test that aria-multiselectable=false & the selectable children should have aria-selected=true|false depending on their state
     */
    @Test
    public void testAriaSelectionModeSingle() {
        open();

        GridElement grid = $(GridElement.class).id(GridSingleSelectionPage.DESELECT_ALLOWED_GRID_ID);
        GridTRElement firstRow = grid.getRow(0);
        firstRow.select();
        TestBenchElement table = grid.$("table").first();
        // table should have aria-multiselectable
        Assert.assertTrue(table.hasAttribute("aria-multiselectable"));
        // aria-multiselectable should be set to false
        Assert.assertFalse(Boolean.parseBoolean(table.getAttribute("aria-multiselectable")));

        Assert.assertTrue(firstRow.hasAttribute("aria-selected"));
        Assert.assertTrue(Boolean.parseBoolean(firstRow.getAttribute("aria-selected")));
        Assert.assertFalse(Boolean.parseBoolean(grid.getRow(1).getAttribute("aria-selected")));
    }
}