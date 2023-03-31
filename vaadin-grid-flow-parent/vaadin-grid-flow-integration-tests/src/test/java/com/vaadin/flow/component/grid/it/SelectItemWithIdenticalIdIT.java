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

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/select-item-with-identical-id")
public class SelectItemWithIdenticalIdIT extends AbstractComponentIT {

    private CheckboxElement useMultiSelectCheckbox;

    private WebElement addGridButton;

    @Before
    public void init() {
        open();
        addGridButton = findElement(By.id("add-grid-button"));
        useMultiSelectCheckbox = $(CheckboxElement.class).first();
    }

    @Test
    public void selectUsingItemWithSameIdHash_addGrid_itemIsNotUpdated() {
        addGridButton.click();
        GridElement grid = $(GridElement.class).waitForFirst();

        Assert.assertEquals("1", grid.getCell(0, 0).getText());
    }

    @Test
    public void addGridWithSelection_updateSelectionUsingItemWithSameIdHash_itemIsNotUpdated() {
        addGridButton.click();
        GridElement grid = $(GridElement.class).waitForFirst();
        findElement(By.id("update-selection-button")).click();

        Assert.assertEquals("2", grid.getCell(1, 0).getText());
    }

    @Test
    public void setMultiSelect_selectUsingItemWithSameIdHash_addGrid_itemIsNotUpdated() {
        useMultiSelectCheckbox.setChecked(true);
        addGridButton.click();
        GridElement grid = $(GridElement.class).waitForFirst();

        Assert.assertEquals("1", grid.getCell(0, 1).getText());
    }

    @Test
    public void setMultiSelect_addGridWithSelection_updateSelectionUsingItemWithSameIdHash_itemIsNotUpdated() {
        useMultiSelectCheckbox.setChecked(true);
        addGridButton.click();
        GridElement grid = $(GridElement.class).waitForFirst();
        findElement(By.id("update-selection-button")).click();

        Assert.assertEquals("2", grid.getCell(1, 1).getText());
    }
}
