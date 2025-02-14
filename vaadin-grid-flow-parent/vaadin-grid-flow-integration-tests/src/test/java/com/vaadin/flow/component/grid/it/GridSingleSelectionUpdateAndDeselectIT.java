package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-single-selection-update-and-deselect")
public class GridSingleSelectionUpdateAndDeselectIT
        extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void shouldHaveCorrectValueAfterUpdateAndDeselect() {
        // Click the update button
        clickElementWithJs("update-name");
        GridElement grid = $(GridElement.class).first();
        String name = grid.getCell(0, 1).getText();

        // Expect the name to be updated
        Assert.assertEquals("Bar", name);
    }

}
@TestPath("vaadin-grid/grid-single-selection-update-and-deselect")
public class GridSingleSelectionUpdateAndDeselectIT
        extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void shouldHaveCorrectValueAfterUpdateAndDeselect() {
        // Click the update button
        clickElementWithJs("update-name");
        GridElement grid = $(GridElement.class).first();
        String name = grid.getCell(0, 1).getText();

        // Expect the name to be updated
        Assert.assertEquals("Bar", name);
    }

}
