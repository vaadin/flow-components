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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/detach-reattach-page")
public class DetachReattachIt extends AbstractComponentIT {

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

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));

        WebElement detailsElement = grid
                .findElement(By.tagName("flow-component-renderer"));

        Assert.assertTrue("Item details are visible on cell click by default.",
                detailsElement.isDisplayed());

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));
        Assert.assertFalse("Item details are hidden on subsequent cell click.",
                detailsElement.isDisplayed());

        // Do not show details on click
        $("button").id("toggle-details-visible-click-button").click();

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));
        Assert.assertFalse(
                "Item details are hidden with setDetailsVisibleOnClick(false).",
                detailsElement.isDisplayed());

        // Detach and re-attach
        $("button").id("detach-button").click();

        $("button").id("attach-button").click();

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));
        Assert.assertFalse(
                "Item details are still hidden after detach and re-attach.",
                detailsElement.isDisplayed());

    }

    private WebElement getRow(GridElement grid, int row) {
        return grid.$("*").id("items").findElements(By.cssSelector("tr"))
                .get(row);
    }
}
