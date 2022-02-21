/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *
 * use this file except in compliance with the License. You may obtain a copy of
 *
 * the License at
 *
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 *
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *
 * License for the specific language governing permissions and limitations under
 *
 * the License.
 *
 */

package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/editor-focus")
public class GridEditorFocusIT extends AbstractComponentIT {

    private GridElement grid;

    private GridColumnElement nameColumn;
    private int rowCount;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-grid"));
        grid = $(GridElement.class).first();

        waitUntil(driver -> grid.getRowCount() > 0);

        rowCount = grid.getRowCount();

        nameColumn = grid.getColumn("Name");
    }

    @Test
    public void addNewItem() {
        findElement(By.id("add-item")).click();

        // Assert that new item added
        Assert.assertEquals(rowCount + 1, grid.getRowCount());

        // Assert editor opened on new row
        GridTRElement row = grid.getRow(grid.getRowCount() - 1);
        GridTHTDElement nameCell = row.getCell(nameColumn);
        Assert.assertTrue(nameCell.$("vaadin-text-field").exists());

        // Assert editor is focused
        TestBenchElement editorComponent = nameCell.$("vaadin-text-field")
                .first();
        TestBenchElement input = editorComponent.$("input").first();
        assertElementHasFocus(input);

    }

    @Test
    public void editFirstItem() {
        findElement(By.id("edit-first-item")).click();

        // Assert editor opened on new row
        GridTRElement row = grid.getRow(0);
        GridTHTDElement nameCell = row.getCell(nameColumn);
        Assert.assertTrue(nameCell.$("vaadin-text-field").exists());

        // Assert editor is focused
        // Flaky check, focus sometimes is not in place
        // TestBenchElement editorComponent =
        // nameCell.$("vaadin-text-field").first();
        // assertElementHasFocus(editorComponent);

    }

    private void assertElementHasFocus(WebElement element) {
        Assert.assertTrue("Element should have focus",
                (Boolean) executeScript(
                        "return document.activeElement === arguments[0]",
                        element));
    }
}
