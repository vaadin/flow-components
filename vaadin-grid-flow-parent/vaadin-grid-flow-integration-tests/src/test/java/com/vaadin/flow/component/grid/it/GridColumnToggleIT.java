/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/column-toggle")
public class GridColumnToggleIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
        waitUntil(driver -> grid.getRowCount() > 0);
    }

    @Test
    public void menuListsOnlyHideableColumns() {
        openMenu();
        List<String> labels = menuItems().stream()
                .map(TestBenchElement::getText).collect(Collectors.toList());
        // "Age" is not hideable (the default) and is excluded.
        Assert.assertEquals(List.of("First name", "Last name", "Email"),
                labels);
    }

    @Test
    public void toggleColumnOff_hidesColumnAndUpdatesServer() {
        Assert.assertEquals(4, grid.getVisibleColumns().size());

        openMenu();
        clickItem("First name");

        waitUntil(driver -> grid.getVisibleColumns().size() == 3);
        Assert.assertEquals("firstName visible=false fromClient=true",
                findElement(By.id("status")).getText());
    }

    @Test
    public void toggleColumnOffAndOn_menuStaysOpen_repopulatesData() {
        openMenu();

        clickItem("First name");
        waitUntil(driver -> grid.getVisibleColumns().size() == 3);

        // The menu is still open (keepOpen), so a second toggle works without
        // reopening it.
        clickItem("First name");
        waitUntil(driver -> grid.getVisibleColumns().size() == 4);

        // The re-shown column's data is present again.
        Assert.assertEquals("John", grid.getCell(0, 0).getText());
    }

    @Test
    public void setHideableFalseOnAllColumns_toggleHidesAutomatically() {
        Assert.assertTrue(columnToggle().isDisplayed());

        // All columns become non-hideable -> the toggle button hides itself.
        findElement(By.id("toggle-hideable")).click();
        waitUntil(driver -> !columnToggle().isDisplayed());

        // Columns become hideable again -> the toggle button reappears.
        findElement(By.id("toggle-hideable")).click();
        waitUntil(driver -> columnToggle().isDisplayed());
    }

    @Test
    public void setHideable_whileColumnHiddenViaToggle_syncsHiddenColumn() {
        // Hide "First name" through the toggle menu.
        openMenu();
        clickItem("First name");
        waitUntil(driver -> grid.getVisibleColumns().size() == 3);
        closeMenu();

        // The server flips hideable=false on every column while "First name"
        // is hidden. The change must also reach the hidden column: no hideable
        // columns remain, so the toggle button hides itself.
        findElement(By.id("toggle-hideable")).click();
        waitUntil(driver -> !columnToggle().isDisplayed());

        // Flip back: the toggle returns and offers the hidden column again.
        findElement(By.id("toggle-hideable")).click();
        waitUntil(driver -> columnToggle().isDisplayed());
        openMenu();
        List<String> labels = menuItems().stream()
                .map(TestBenchElement::getText).collect(Collectors.toList());
        Assert.assertEquals(List.of("First name", "Last name", "Email", "Age"),
                labels);
    }

    private void closeMenu() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        waitUntil(driver -> menuItems().stream()
                .noneMatch(TestBenchElement::isDisplayed));
    }

    /**
     * The column toggle context menu inside the grid's shadow root.
     */
    private TestBenchElement columnToggle() {
        return grid.$(TestBenchElement.class).id("columnToggle");
    }

    private void openMenu() {
        columnToggle().$("button").first().click();
        // The menu (and its items) render inside the toggle's context menu.
        waitUntil(driver -> !menuItems().isEmpty());
    }

    private List<TestBenchElement> menuItems() {
        return columnToggle().$("vaadin-context-menu-item").all();
    }

    private void clickItem(String label) {
        TestBenchElement item = menuItems().stream()
                .filter(element -> label.equals(element.getText())).findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No menu item with label " + label));
        item.click();
    }
}
