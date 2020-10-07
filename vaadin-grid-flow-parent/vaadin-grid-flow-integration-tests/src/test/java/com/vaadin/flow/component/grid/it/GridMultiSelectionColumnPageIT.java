/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-multi-selection-column")
public class GridMultiSelectionColumnPageIT extends AbstractComponentIT {

    @Test
    public void selectAllCheckbox() {
        open();
        WebElement lazyGrid = findElement(By.id("lazy-grid"));
        Assert.assertEquals(
                "lazy grid selectAllCheckbox should be hidden by default",
                "true", lazyGrid.findElement(By.id("selectAllCheckbox"))
                        .getAttribute("hidden"));

        WebElement grid = findElement(By.id("in-memory-grid"));
        WebElement selectAllCheckbox = grid
                .findElement(By.id("selectAllCheckbox"));
        Assert.assertNull(
                "in-memory grid selectAllCheckbox should be visible by default",
                selectAllCheckbox.getAttribute("hidden"));

        selectAllCheckbox.click();
        WebElement message = findElement(By.id("selected-item-count"));
        Assert.assertEquals(
                "Selected item count: "
                        + GridMultiSelectionColumnPage.ITEM_COUNT,
                message.getText());
        Assert.assertEquals("true", selectAllCheckbox.getAttribute("checked"));

        WebElement selectCheckbox = grid
                .findElements(By.tagName("vaadin-checkbox")).get(5);
        Assert.assertEquals("true", selectCheckbox.getAttribute("checked"));
        selectCheckbox.click();
        Assert.assertNull("Item 5 selected even though it shouldn't be",
                selectCheckbox.getAttribute("checked"));
        Assert.assertNull("Select all check even though not all items selected",
                selectAllCheckbox.getAttribute("checked"));

        // On deselected item
        Assert.assertEquals(
                "Selected item count: "
                        + (GridMultiSelectionColumnPage.ITEM_COUNT - 1),
                message.getText());
    }

    @Test
    public void selectCheckboxMultiSelectionMode() {
        open();

        // Test switch selection mode from single to multi mode before adding the grid to DOM
        WebElement gridSelectionMode = findElement(By.id("in-testing-multi-selection-mode-grid"));
        WebElement selectAllCheckbox_selectionMode = gridSelectionMode
                .findElement(By.id("selectAllCheckbox"));
        WebElement message_selectionMode = findElement(By.id("selected-item-count"));
        Assert.assertEquals(true, selectAllCheckbox_selectionMode.isDisplayed());
        selectAllCheckbox_selectionMode.click();
        Assert.assertEquals("true", selectAllCheckbox_selectionMode.getAttribute("checked"));
        Assert.assertEquals(
                "Selected item count: "
                        + (GridMultiSelectionColumnPage.ITEM_COUNT),
                message_selectionMode.getText());
        selectAllCheckbox_selectionMode.click();
        WebElement selectCheckbox15_multiSelection = gridSelectionMode
                .findElements(By.tagName("vaadin-checkbox")).get(15);
        selectCheckbox15_multiSelection.click();
        WebElement selectCheckbox6_multiSelection = gridSelectionMode
                .findElements(By.tagName("vaadin-checkbox")).get(6);
        selectCheckbox6_multiSelection.click();
        Assert.assertEquals("true", selectCheckbox15_multiSelection.getAttribute("checked"));
        Assert.assertEquals("true", selectCheckbox6_multiSelection.getAttribute("checked"));
    }

    @Test
    public void noSelectOnRowItemClick() {
        open();
        WebElement grid = findElement(By.id("in-memory-grid"));
        // click the first row's cell that corresponds to the text column
        grid.findElements(By.tagName("vaadin-grid-cell-content")).stream()
                .filter(element -> "0".equals(element.getText())).findFirst()
                .get().click();
        Assert.assertEquals("No selection event should be fired", "",
                findElement(By.id("selected-item-count")).getText());

        // Test switch selection mode from Multi to single mode before adding the grid to DOM
        // By checking the vaadin-grid-cell-content
        WebElement gridSelectionMode = findElement(By.id("in-testing-multi-selection-mode-grid-single"));
        Assert.assertTrue(gridSelectionMode.findElements(By.tagName("vaadin-checkbox")).isEmpty());
    }

    @Test
    public void gridWithSwappedDataProvider_selectAllIsNotVisible_swappingDataProvidersChangeItsState() {
        open();

        WebElement grid = findElement(By.id("swapped-grid"));
        WebElement selectAllCheckbox = grid
                .findElement(By.id("selectAllCheckbox"));

        Assert.assertEquals("The selectAllCheckbox should be hidden by default",
                "true", selectAllCheckbox.getAttribute("hidden"));

        WebElement inMemory = findElement(By.id("set-in-memory-button"));
        inMemory.click();

        Assert.assertNull(
                "The selectAllCheckbox should be visible with in-memory DataProvider",
                selectAllCheckbox.getAttribute("hidden"));

        WebElement backend = findElement(By.id("set-backend-button"));
        backend.click();

        Assert.assertEquals(
                "The selectAllCheckbox should be hidden with backend DataProvider",
                "true", selectAllCheckbox.getAttribute("hidden"));
    }

    @Test
    public void gridWithSwappedDataProvider_selectAllIsForcedVisible_noSelectionEventOccurs() {
        open();

        WebElement grid = findElement(By.id("swapped-grid"));
        WebElement selectAllCheckbox = grid
                .findElement(By.id("selectAllCheckbox"));

        executeScript("arguments[0].selectAllHidden = false",
                selectAllCheckbox);
        selectAllCheckbox.click();

        WebElement message = findElement(By.id("selected-item-count"));
        Assert.assertEquals(
                "All selection shouldn't be possible when the selectAll is hidden",
                "", message.getText());
    }

    @Test
    public void setAutoWidthOfSelectionColumnIsTrue() {
        open();
        WebElement grid = findElement(By.id("set-auto-width-true"));

        // Test autoWidth of selection column is true
        WebElement gridSelectionMode = grid.findElement(By.tagName("vaadin-grid-flow-selection-column"));
        String autoWidth = gridSelectionMode.getAttribute("autoWidth");
        Assert.assertTrue("autoWidth should be true", Boolean.parseBoolean(autoWidth));
    }
}
