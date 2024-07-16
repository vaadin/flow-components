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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/toggle-visibility")
public class ToggleVisibilityIT extends AbstractComponentIT {

    @Test
    public void toggleVisibility_secondGridIsVisible() {
        open();

        waitForElementPresent(By.id("toggle-visibility-grid1"));
        checkLogsForErrors();

        GridElement grid1 = $(GridElement.class).id("toggle-visibility-grid1");
        Assert.assertEquals("Grid1 Item 0", grid1.getCell(0, 0).getText());

        WebElement toggle = findElement(By.id("toggle-visibility-button"));
        toggle.click();

        waitForElementPresent(By.id("toggle-visibility-grid2"));
        checkLogsForErrors();

        GridElement grid2 = $(GridElement.class).id("toggle-visibility-grid2");
        Assert.assertEquals("Grid2 Item 0", grid2.getCell(0, 0).getText());

        toggle.click();
        waitForElementPresent(By.id("toggle-visibility-grid1"));
        checkLogsForErrors();
    }

}
