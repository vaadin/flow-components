/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Test;

public class GridColumnRenderingTest {

    @Test
    public void defaultColumnRendering() {
        var grid = new Grid<String>();
        Assert.assertEquals(ColumnRendering.EAGER, grid.getColumnRendering());
    }

    @Test
    public void setColumnRendering_updatedPropertyValue() {
        var grid = new Grid<String>();
        grid.setColumnRendering(ColumnRendering.LAZY);
        Assert.assertEquals(ColumnRendering.LAZY, grid.getColumnRendering());
        Assert.assertEquals("lazy",
                grid.getElement().getProperty("columnRendering"));
    }

    @Test
    public void setColumnRenderingNull_defaultPropertyValue() {
        var grid = new Grid<String>();
        grid.setColumnRendering(null);
        Assert.assertEquals(ColumnRendering.EAGER, grid.getColumnRendering());
        Assert.assertEquals("eager",
                grid.getElement().getProperty("columnRendering"));
    }

    @Test
    public void setColumnRenderingFromLazyToNull_updatedPropertyValue() {
        var grid = new Grid<String>();
        grid.setColumnRendering(ColumnRendering.LAZY);
        grid.setColumnRendering(null);
        Assert.assertEquals(ColumnRendering.EAGER, grid.getColumnRendering());
        Assert.assertEquals("eager",
                grid.getElement().getProperty("columnRendering"));
    }

    @Test
    public void setColumnRenderingFromNullToLazy_updatedPropertyValue() {
        var grid = new Grid<String>();
        grid.setColumnRendering(null);
        grid.setColumnRendering(ColumnRendering.LAZY);
        Assert.assertEquals(ColumnRendering.LAZY, grid.getColumnRendering());
        Assert.assertEquals("lazy",
                grid.getElement().getProperty("columnRendering"));
    }
}
