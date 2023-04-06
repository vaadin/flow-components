
package com.vaadin.flow.component.grid.it;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-on-flex-layout")
public class GridOnFlexLayoutIT extends GridSizeIT {

    @Test
    public void gridOccupies100PercentOfThePage() {
        open();
        WebElement grid = findElement(By.id("full-size-grid"));
        assertGridOccupies100PercentOfThePage(grid);
    }

}
