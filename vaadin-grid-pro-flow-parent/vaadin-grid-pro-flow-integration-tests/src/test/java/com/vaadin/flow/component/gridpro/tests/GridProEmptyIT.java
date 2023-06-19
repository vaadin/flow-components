package com.vaadin.flow.component.gridpro.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.gridpro.testbench.GridProElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-pro/empty")
public class GridProEmptyIT extends AbstractComponentIT {
    private GridProElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridProElement.class).first();
    }

    @Test
    public void getFirstAndLastVisibleRowIndex_doesNotThrow() {
        Assert.assertEquals(
                "Should return -1 when getting the first visible row index", -1,
                grid.getFirstVisibleRowIndex());
        Assert.assertEquals(
                "Should return -1 when getting the last visible row index", -1,
                grid.getLastVisibleRowIndex());
    }
}
