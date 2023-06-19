package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/empty")
public class GridEmptyIT extends AbstractComponentIT {
    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
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
