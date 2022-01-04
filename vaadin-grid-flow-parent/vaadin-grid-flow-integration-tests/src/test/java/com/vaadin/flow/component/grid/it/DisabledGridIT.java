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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/disabled-grid")
public class DisabledGridIT extends AbstractComponentIT {

    @Test
    public void gridIsDisabled_renderedButtonsAreDisabled() {
        open();

        WebElement message = findElement(By.id("message"));
        GridElement grid = $(GridElement.class).id("grid");
        GridTRElement row = grid.getRow(0);
        GridTHTDElement buttonCell = row
                .getCell(grid.getColumn("Button renderer"));
        WebElement button = buttonCell.getContext()
                .findElement(By.tagName("button"));

        Assert.assertTrue("The rendered button should be enabled",
                button.isEnabled());

        GridTHTDElement checkboxCell = row.getCell(grid.getColumn("Checkbox"));
        WebElement checkBox = checkboxCell.getContext()
                .findElement(By.tagName("vaadin-checkbox"));

        Assert.assertTrue("The rendered checkbox should be enabled",
                checkBox.isEnabled());

        WebElement toggleEnabled = findElement(By.id("toggleEnabled"));
        toggleEnabled.click();

        row = grid.getRow(0);
        buttonCell = row.getCell(grid.getColumn("Button renderer"));
        button = buttonCell.getContext().findElement(By.tagName("button"));
        Assert.assertFalse("The rendered button should be disabled",
                button.isEnabled());

        checkBox = checkboxCell.getContext()
                .findElement(By.tagName("vaadin-checkbox"));
        Assert.assertFalse("The rendered checkbox should be disabled",
                checkBox.isEnabled());

        button = buttonCell.getContext().findElement(By.tagName("button"));
        executeScript("arguments[0].disabled = false", button);
        button.click();

        assertEmptyMessage(message);

        executeScript("arguments[0].disabled = false", checkBox);
        checkBox.click();
        assertEmptyMessage(message);
    }

    @Test
    public void gridIsDisabled_componentsInHeaderAreDisabled() {
        open();
        WebElement message = findElement(By.id("message"));
        GridElement grid = $(GridElement.class).id("grid");
        TestBenchElement headerButton = grid
                .findElement(By.id("header-button"));

        Assert.assertTrue("Button in the header should be enabled",
                headerButton.isEnabled());

        disableGrid();

        executeScript("arguments[0].disabled = false", headerButton);
        headerButton.click();

        assertEmptyMessage(message);
    }

    @Test
    public void gridIsDisabled_componentsInHeaderHaveDisabledAttribute() {
        open();
        GridElement grid = $(GridElement.class).id("grid");
        TestBenchElement headerButton = grid
                .findElement(By.id("header-button"));

        Assert.assertTrue("Button in the header should be enabled",
                headerButton.isEnabled());

        disableGrid();

        Assert.assertFalse(
                "Button in the header should have 'disabled' attribute",
                headerButton.isEnabled());
    }

    @Test
    public void gridIsDisabled_noItemClickEvents() {
        open();
        WebElement message = findElement(By.id("message"));
        GridElement grid = $(GridElement.class).id("grid");

        disableGrid();

        GridTRElement row = grid.getRow(0);
        row.click(10, 10);

        assertEmptyMessage(message);

        row.doubleClick();

        assertEmptyMessage(message);
    }

    private void disableGrid() {
        findElement(By.id("toggleEnabled")).click();
    }

    private void assertEmptyMessage(WebElement message) {
        Assert.assertTrue("The message should be empty",
                message.getText().isEmpty());
    }

}
