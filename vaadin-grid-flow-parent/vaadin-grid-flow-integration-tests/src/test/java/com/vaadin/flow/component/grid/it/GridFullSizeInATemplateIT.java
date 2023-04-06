
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
