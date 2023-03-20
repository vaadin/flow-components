
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Tests for dynamically adding new columns with different renderers after the
 * Grid has already been attached and rendered.
 */
@TestPath("vaadin-grid/component-column-height")
public class ComponentColumnWithHeightIT extends AbstractComponentIT {

    private GridElement grid;
    private WebElement add;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
        add = $("button").first();
    }

    @Test
    public void shouldPositionItemsCorrectlyAfterUpdatingComponentRenderers() {
        add.click();
        // Expect the y position of the second row to equal the y position + the
        // height of the first row
        Assert.assertEquals(
                grid.getRow(0).getLocation().y
                        + grid.getRow(0).getSize().height,
                grid.getRow(1).getLocation().y);
    }
}
