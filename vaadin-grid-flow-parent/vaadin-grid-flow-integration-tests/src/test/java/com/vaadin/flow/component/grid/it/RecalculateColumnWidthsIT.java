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
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/recalculate-column-widths")
public class RecalculateColumnWidthsIT extends AbstractComponentIT {
    @Test
    public void columnsRecalculateAfterDataChange() {
        open();

        waitForElementPresent(By.id("grid"));

        GridElement grid = $(GridElement.class).id("grid");
        TestBenchElement button = $(TestBenchElement.class)
                .id("change-data-button");

        GridTHTDElement cell = grid.getCell(1, 1);

        Integer scrollWidthBefore = cell.getPropertyInteger("scrollWidth");

        button.click();

        Integer scrollWidthAfter = cell.getPropertyInteger("scrollWidth");
        Integer offsetWidthAfter = cell.getPropertyInteger("offsetWidth");

        Assert.assertTrue("Scroll width should have increased",
                scrollWidthAfter > scrollWidthBefore);
        Assert.assertTrue("Cell content should not be cut off with ellipsis",
                offsetWidthAfter <= scrollWidthAfter);
    }
}
