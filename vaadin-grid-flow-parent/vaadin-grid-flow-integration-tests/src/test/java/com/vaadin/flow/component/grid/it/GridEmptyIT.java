/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-empty")
public class GridEmptyIT extends AbstractComponentIT {

    @Test
    public void emptyGrid_clearCache_loadingStateCleared() {
        open();

        // Force data provider request by clearing the grid's cache
        $(ButtonElement.class).id("clear-cache-button").click();

        GridElement grid = $(GridElement.class).id("empty-grid");
        waitUntil(driver -> "false".equals(grid.getAttribute("loading")));
    }

}
