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
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-grid/detach-reattach-page")
public class DetachReattachIT extends AbstractComponentIT {

    @Test
    public void detachAndReattach_setDeselectAllowedPreserved() {
        open();
        GridElement grid = $(GridElement.class).first();

        grid.getRow(1).select();
        Assert.assertTrue("Row is selected.", grid.getRow(1).isSelected());

        // Disable de-selection
        $("button").id("disallow-deselect-button").click();

        grid.getRow(1).deselect();
        Assert.assertTrue("Row is still selected as deselection is disallowed.",
                grid.getRow(1).isSelected());

        // Detach and re-attach
        $("button").id("detach-button").click();

        $("button").id("attach-button").click();

        grid = $(GridElement.class).first();

        Assert.assertTrue(
                "Selected row is preserved after detach and re-attach.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue("Deselection is still disallowed after re-attach.",
                grid.getRow(1).isSelected());
    }

    @Test
    public void detachAndReattach_setDetailsVisibleOnClickPreserved() {
        open();

        GridElement grid = $(GridElement.class).first();

        // Add item details
        $("button").id("add-item-details-button").click();

        grid.getCell(1, 0).click();

        Assert.assertTrue("Item details are visible on cell click by default.",
                grid.findElement(By.tagName("span")).isDisplayed());

        grid.getCell(1, 0).click();

        Assert.assertEquals("Item details are hidden on subsequent cell click.",
                0, grid.findElements(By.tagName("span")).size());

        // Do not show details on click
        $("button").id("toggle-details-visible-click-button").click();

        grid.getCell(1, 0).click();
        Assert.assertEquals(
                "Item details are hidden with setDetailsVisibleOnClick(false).",
                0, grid.findElements(By.tagName("span")).size());

        // Detach and re-attach
        $("button").id("detach-button").click();

        $("button").id("attach-button").click();

        grid = $(GridElement.class).first();

        grid.getCell(1, 0).click();
        Assert.assertEquals(
                "Item details are still hidden after detach and re-attach.", 0,
                grid.findElements(By.tagName("span")).size());
    }

    @Test
    public void detachAndReattach_resetSorting_noErrorIsThrown() {
        open();
        GridElement grid = $(GridElement.class).first();

        grid.getHeaderCell(0).$("vaadin-grid-sorter").first().click();

        // Detach, reset sorting and re-attach
        $("button").id("detach-button").click();

        $("button").id("reset-sorting-button").click();

        $("button").id("attach-button").click();

        // Check that there are no new exceptions/errors thrown
        // after re-attaching the grid when sorting is reset
        checkLogsForErrors();
    }
}