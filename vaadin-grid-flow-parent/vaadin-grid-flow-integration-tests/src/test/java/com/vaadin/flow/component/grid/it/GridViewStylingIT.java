/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/styling")
public class GridViewStylingIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void stylingDemo_classNamesGenerated() {
        GridElement grid = $(GridElement.class).id("class-name-generator");
        scrollToElement(grid);

        GridStylingIT.assertCellClassNames(grid, 0, 0, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 0, 1, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 0, 2, "subscriber");

        GridStylingIT.assertCellClassNames(grid, 5, 0, "");
        GridStylingIT.assertCellClassNames(grid, 5, 1, "minor");
        GridStylingIT.assertCellClassNames(grid, 5, 2, "");

        GridStylingIT.assertCellClassNames(grid, 9, 0, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 9, 1, "subscriber minor");
        GridStylingIT.assertCellClassNames(grid, 9, 2, "subscriber");
    }
}
