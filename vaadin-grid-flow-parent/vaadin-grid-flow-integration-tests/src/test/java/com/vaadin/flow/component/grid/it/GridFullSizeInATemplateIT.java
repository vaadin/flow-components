/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/grid-with-full-size-in-template")
public class GridFullSizeInATemplateIT extends GridSizeIT {

    @Test
    public void gridOccupies100PercentOfThePage() {
        open();
        TestBenchElement gridInATemplate = $("grid-in-a-template").first();
        WebElement grid = gridInATemplate.$("*").id("grid");
        assertGridOccupies100PercentOfThePage(grid);
    }

}
