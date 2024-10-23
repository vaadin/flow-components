/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/item-selectable-provider")
public class ItemSelectableProviderIT extends AbstractComponentIT {
    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
    }

    @Test
    public void singleSelect_clickRow_preventsSelection() {
        $("button").id("enable-single-selection").click();
        $("button").id("disable-selection-first-five").click();

        // Prevents selection of non-selectable item
        grid.select(0);
        assertSelectedItems(Set.of());

        // Allows selection of selectable item
        grid.select(5);
        assertSelectedItems(Set.of(5));
    }

    @Test
    public void singleSelect_clickRow_preventsDeselection() {
        $("button").id("enable-single-selection").click();
        grid.select(0);

        $("button").id("disable-selection-first-five").click();

        // Prevents deselection of non-selectable item
        grid.deselect(0);
        assertSelectedItems(Set.of(0));

        // Prevents deselection when clicking other non-selectable item
        grid.select(1);
        assertSelectedItems(Set.of(0));

        // Allows deselection of selectable item
        grid.select(5);
        grid.deselect(5);
        assertSelectedItems(Set.of());
    }

    @Test
    public void multiSelect_hidesCheckboxes() {
        $("button").id("enable-multi-selection").click();
        $("button").id("disable-selection-first-five").click();

        Assert.assertFalse(getItemCheckbox(0).isDisplayed());
        Assert.assertTrue(getItemCheckbox(5).isDisplayed());
    }

    @Test
    public void multiSelect_updateProvider_updatesCheckboxes() {
        $("button").id("enable-multi-selection").click();
        $("button").id("disable-selection-first-five").click();

        Assert.assertFalse(getItemCheckbox(0).isDisplayed());
        Assert.assertTrue(getItemCheckbox(5).isDisplayed());

        $("button").id("allow-selection-first-five").click();

        Assert.assertTrue(getItemCheckbox(0).isDisplayed());
        Assert.assertFalse(getItemCheckbox(5).isDisplayed());
    }

    @Test
    public void multiSelect_clickCheckbox_preventsSelection() {
        $("button").id("enable-multi-selection").click();
        $("button").id("disable-selection-first-five").click();

        // Prevents selection of non-selectable item
        clickItemCheckbox(0);
        assertSelectedItems(Set.of());

        // Allows selection of selectable item
        clickItemCheckbox(5);
        assertSelectedItems(Set.of(5));
    }

    @Test
    public void multiSelect_clickCheckbox_preventsDeselection() {
        $("button").id("enable-multi-selection").click();
        grid.select(0);
        grid.select(5);
        assertSelectedItems(Set.of(0, 5));

        $("button").id("disable-selection-first-five").click();

        // Prevents selection of non-selectable item
        clickItemCheckbox(0);
        assertSelectedItems(Set.of(0, 5));

        // Allows selection of selectable item
        clickItemCheckbox(5);
        assertSelectedItems(Set.of(0));
    }

    private TestBenchElement getItemCheckbox(int index) {
        return grid.getCell(index, 0).$("vaadin-checkbox").first();
    }

    private void clickItemCheckbox(int index) {
        var checkbox = getItemCheckbox(index);
        // Make checkboxes for non-selectable items interactable
        checkbox.setProperty("hidden", false);
        checkbox.setProperty("readonly", false);
        checkbox.click();
    }

    private Set<Integer> getServerSelectedItems() {
        var items = $("span").id("selected-items").getText();
        return items.isEmpty() ? Set.of()
                : Stream.of(items.split(",")).map(Integer::parseInt)
                        .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Set<Integer> getClientSelectedItems() {
        var itemNames = (List<String>) getCommandExecutor().executeScript(
                "return arguments[0].selectedItems.map(item => item.col0)",
                grid);
        return itemNames.stream().map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    private void assertSelectedItems(Set<Integer> items) {
        Assert.assertEquals(items, getServerSelectedItems());
        Assert.assertEquals(items, getClientSelectedItems());
    }
}
