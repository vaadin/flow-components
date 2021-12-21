package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/editor-vertical-scrolling")
public class EditorVerticalScrollingIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("editor-grid"));
        grid = $(GridElement.class).id("editor-grid");

        waitForElementPresent(By.id("edit-1"));
    }

    @Test
    public void editRow_scrollingIsDisabled_closeEditor_scrollingIsRestored() {
        String overflow = grid.$("*").id("table").getCssValue("overflow-y");
        Assert.assertEquals("auto", overflow);

        TestBenchElement editButton = grid.findElement(By.id("edit-1"));
        editButton.click();

        waitForElementPresent(By.id("cancel-1"));

        overflow = grid.$("*").id("table").getCssValue("overflow-y");
        Assert.assertEquals("hidden", overflow);

        TestBenchElement cancelButton = grid.findElement(By.id("cancel-1"));
        cancelButton.click();

        waitForElementPresent(By.id("edit-1"));

        overflow = grid.$("*").id("table").getCssValue("overflow-y");
        Assert.assertEquals("auto", overflow);
    }

}
