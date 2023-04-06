
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

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
