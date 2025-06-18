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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/height-by-rows")
public class GridViewHeightByRowsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void heightByRows_allRowsAreFetched() {
        GridElement grid = $(GridElement.class).id("grid-height-by-rows");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() == 50);

        Assert.assertEquals("Grid should have heightByRows set to true", "true",
                grid.getAttribute("allRowsVisible"));
    }
}
