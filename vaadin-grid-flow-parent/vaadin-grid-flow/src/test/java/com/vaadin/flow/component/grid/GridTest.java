/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Test;

public class GridTest {
    @Test
    public void setHeightByRows_allRowsAreVisible() {
        final Grid<String> grid = new Grid<>();

        Assert.assertEquals(null,
                grid.getElement().getProperty("heightByRows"));

        grid.setHeightByRows(true);
        Assert.assertEquals("true",
                grid.getElement().getProperty("heightByRows"));
    }

    @Test
    public void setAllRowsVisible_allRowsAreVisible() {
        final Grid<String> grid = new Grid<>();

        Assert.assertEquals(null,
                grid.getElement().getProperty("allRowsVisible"));

        grid.setAllRowsVisible(true);
        Assert.assertEquals("true",
                grid.getElement().getProperty("allRowsVisible"));
    }

    @Test
    public void setAllRowsVisibleProperty_isHeightByRowsAndIsAllRowsVisibleWork() {
        final Grid<String> grid = new Grid<>();
        grid.getElement().setProperty("allRowsVisible", true);

        Assert.assertTrue(grid.isHeightByRows());
        Assert.assertTrue(grid.isAllRowsVisible());
    }

    @Test
    public void setHeightByRowsProperty_isHeightByRowsAndIsAllRowsVisibleWork() {
        final Grid<String> grid = new Grid<>();
        grid.getElement().setProperty("allRowsVisible", true);

        Assert.assertTrue(grid.isHeightByRows());
        Assert.assertTrue(grid.isAllRowsVisible());
    }
}
