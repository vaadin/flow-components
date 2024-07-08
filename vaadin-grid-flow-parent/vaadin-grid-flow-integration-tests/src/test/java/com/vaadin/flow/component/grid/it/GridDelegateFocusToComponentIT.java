/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/delegate-focus-inside-component-renderer")
public class GridDelegateFocusToComponentIT extends AbstractComponentIT {

    @Test
    public void focusTextField() {
        open();

        // Move focus to the first cell of second row of grid and press `Enter`
        new Actions(getDriver())
                .sendKeys(Keys.TAB, Keys.TAB, Keys.ARROW_RIGHT, Keys.ENTER)
                .build().perform();

        GridElement grid = $(GridElement.class).id("grid");

        Assert.assertTrue(grid.getCell(0, 1).$("vaadin-text-field").exists());

        // Assert vaadin-text-field with id 'foo' is focused
        TestBenchElement focusableComponent = grid.getCell(0, 1)
                .$("vaadin-text-field").id("foo");
        assertElementHasFocus(focusableComponent);

    }

    @Test
    public void focusButton() {
        open();

        // Move focus to the first cell of third row of grid and press `Enter`
        new Actions(getDriver()).sendKeys(Keys.TAB, Keys.TAB, Keys.ARROW_RIGHT,
                Keys.ARROW_RIGHT, Keys.ENTER).build().perform();

        GridElement grid = $(GridElement.class).id("grid");

        Assert.assertTrue(grid.getCell(0, 2).$("vaadin-button").exists());

        // Assert vaadin-button is focused
        TestBenchElement button = grid.getCell(0, 2).$("vaadin-button").first();
        assertElementHasFocus(button);

        // Click on the button using `Enter`
        new Actions(getDriver()).sendKeys(Keys.ENTER).build().perform();

        WebElement info = $("div").id("info");
        Assert.assertEquals("foo", info.getText());

    }

    private void assertElementHasFocus(WebElement element) {
        Assert.assertTrue("Element should have focus",
                (Boolean) executeScript(
                        "return document.activeElement === arguments[0]",
                        element));
    }
}
