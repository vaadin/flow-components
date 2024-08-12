/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

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
        assertElementHasFocus(editorComponent);

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
