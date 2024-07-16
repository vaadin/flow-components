/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-multi-selection-column")
public class GridMultiSelectionColumnPageIT extends AbstractComponentIT {

    private static final String SELECT_ALL_CHECKBOX_ID = "selectAllCheckbox";

    @Test
    public void selectAllCheckbox_visibility() {
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
        Assert.assertNotNull(
                "selectAllCheckbox should have focus-target attribute",
                selectAllCheckbox.getAttribute("focus-target"));
    }

    @Test
    public void selectAllCheckbox_state() {
        open();
        WebElement grid = findElement(By.id("in-memory-grid"));
        WebElement selectAllCheckbox = grid
                .findElement(By.id(SELECT_ALL_CHECKBOX_ID));
        WebElement selectCheckbox = grid
                .findElements(By.tagName("vaadin-checkbox")).get(5);
        WebElement message = findElement(By.id("selected-item-count"));

        // Initial
        Assert.assertNull("Select all checkbox should not be checked initially",
                selectAllCheckbox.getAttribute("checked"));
        Assert.assertNull(
                "Select all checkbox should not be in indeterminate state initially",
                selectAllCheckbox.getAttribute("indeterminate"));

        // Select single
        selectCheckbox.click();
        Assert.assertEquals("Selected item count: 1", message.getText());
        Assert.assertNull(
                "Select all checkbox is checked even though not all items selected",
                selectAllCheckbox.getAttribute("checked"));
        Assert.assertEquals(
                "Select all checkbox is not in indeterminate state even though an item is selected",
                "true", selectAllCheckbox.getAttribute("indeterminate"));

        // Select all
        selectAllCheckbox.click();
        Assert.assertEquals(
                "Selected item count: "
                        + GridMultiSelectionColumnPage.ITEM_COUNT,
                message.getText());
        Assert.assertEquals(
                "Select all checkbox is not checked even though all items selected",
                "true", selectAllCheckbox.getAttribute("checked"));
        Assert.assertNull(
                "Select all checkbox is in indeterminate state even though all items are selected",
                selectAllCheckbox.getAttribute("indeterminate"));

        // Deselect single
        selectCheckbox.click();
        Assert.assertEquals(
                "Selected item count: "
                        + (GridMultiSelectionColumnPage.ITEM_COUNT - 1),
                message.getText());
        Assert.assertNull(
                "Select all checkbox is checked even though not all items selected",
                selectAllCheckbox.getAttribute("checked"));
        Assert.assertEquals(
                "Select all checkbox is not in indeterminate state even though not all items selected",
                "true", selectAllCheckbox.getAttribute("indeterminate"));

        // Deselect all, needs to toggle the checkbox twice, first to select all
        // again, then to deselect all
        selectAllCheckbox.click();
        selectAllCheckbox.click();
        Assert.assertEquals("Selected item count: 0", message.getText());
        Assert.assertNull(
                "Select all checkbox is checked even though no items selected",
                selectAllCheckbox.getAttribute("checked"));
        Assert.assertNull(
                "Select all checkbox is in indeterminate state even though no items are selected",
                selectAllCheckbox.getAttribute("indeterminate"));
    }

    @Test
    public void selectAllCheckbox_cellSpaceKey_toggleSelection() {
        open();
        GridElement grid = $(GridElement.class).id("in-memory-grid");
        WebElement selectAllCheckbox = grid
                .findElement(By.id(SELECT_ALL_CHECKBOX_ID));

        grid.getHeaderCell(0).focus();
        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();

        Assert.assertEquals(
                "Select all checkbox is not checked even though all items selected",
                "true", selectAllCheckbox.getAttribute("checked"));

        new Actions(getDriver()).sendKeys(Keys.SPACE).perform();
        Assert.assertNull(
                "Select all checkbox is checked even though no items selected",
                selectAllCheckbox.getAttribute("checked"));
    }

    @Test
    public void selectCheckboxMultiSelectionMode() {
        open();

        // Test switch selection mode from single to multi mode before adding
        // the grid to DOM
        WebElement gridSelectionMode = findElement(
                By.id("in-testing-multi-selection-mode-grid"));
        WebElement selectAllCheckbox_selectionMode = gridSelectionMode
                .findElement(By.id("selectAllCheckbox"));
        WebElement message_selectionMode = findElement(
                By.id("selected-item-count"));
        Assert.assertEquals(true,
                selectAllCheckbox_selectionMode.isDisplayed());
        selectAllCheckbox_selectionMode.click();
        Assert.assertEquals("true",
                selectAllCheckbox_selectionMode.getAttribute("checked"));
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
        Assert.assertEquals("true",
                selectCheckbox15_multiSelection.getAttribute("checked"));
        Assert.assertEquals("true",
                selectCheckbox6_multiSelection.getAttribute("checked"));
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

        // Test switch selection mode from Multi to single mode before adding
        // the grid to DOM
        // By checking the vaadin-grid-cell-content
        WebElement gridSelectionMode = findElement(
                By.id("in-testing-multi-selection-mode-grid-single"));
        Assert.assertTrue(gridSelectionMode
                .findElements(By.tagName("vaadin-checkbox")).isEmpty());
    }

    @Test
    public void gridWithSwappedDataProvider_selectAllIsNotVisible_swapingDataProvidersChangeItsState() {
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
        WebElement gridSelectionMode = grid
                .findElement(By.tagName("vaadin-grid-flow-selection-column"));
        String autoWidth = gridSelectionMode.getAttribute("autoWidth");
        Assert.assertTrue("autoWidth should be true",
                Boolean.parseBoolean(autoWidth));
    }

    @Test
    public void selectAllColumn_shouldBeSelected_whenAllRowsSelectedOnServerSide() {
        open();
        GridElement grid = $(GridElement.class).id(
                GridMultiSelectionColumnPage.MULTI_SELECT_GRID_ALL_SELECTED_GRID_ID);
        WebElement selectAllCheckbox = grid
                .findElement(By.id(SELECT_ALL_CHECKBOX_ID));
        Assert.assertEquals("true", selectAllCheckbox.getAttribute("checked"));
    }

    @Test
    public void selectAllColumn_shouldBeDeSelected_whenOneRowDeSelectedServerSide() {
        open();
        GridElement grid = $(GridElement.class).id(
                GridMultiSelectionColumnPage.MULTI_SELECT_GRID_ALL_SELECTED_GRID_ID);

        TestBenchElement deSelectRow = $(TestBenchElement.class)
                .id("deSelectRow0");
        deSelectRow.click();

        WebElement selectAllCheckbox = grid
                .findElement(By.id(SELECT_ALL_CHECKBOX_ID));
        Assert.assertEquals(null, selectAllCheckbox.getAttribute("checked"));
    }

    @Test
    public void selectAllColumn_shouldBeSelected_whenOneRowSelectedServerSide() {
        open();
        GridElement grid = $(GridElement.class).id(
                GridMultiSelectionColumnPage.MULTI_SELECT_GRID_ONE_NOT_SELECTED_GRID_ID);

        TestBenchElement selectRow0Button = $(TestBenchElement.class)
                .id("selectRow0");
        selectRow0Button.click();

        WebElement selectAllCheckbox = grid
                .findElement(By.id(SELECT_ALL_CHECKBOX_ID));
        Assert.assertEquals("true", selectAllCheckbox.getAttribute("checked"));
    }
}
