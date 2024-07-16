/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-filtering")
public class GridFilteringIT extends AbstractComponentIT {

    @Test
    public void gridInNotLoadingState() {
        open();

        TestBenchElement filter = $("vaadin-text-field").id("filter");
        TestBenchElement input = filter.$("input").first();
        input.sendKeys("w");

        // Blur input to get value change
        executeScript("arguments[0].blur();", input);

        WebElement grid = findElement(By.id("data-grid"));
        // empty Grid content
        Object size = executeScript("return arguments[0].size", grid);
        Assert.assertEquals("0", size.toString());

        input.sendKeys(Keys.BACK_SPACE);

        // Blur input to get value change
        executeScript("arguments[0].blur();", input);

        waitUntil(driver -> executeScript("return arguments[0].size", grid)
                .toString().equals("3"));

        waitUntil(driver -> "false".equals(grid.getAttribute("loading")));
    }
}
