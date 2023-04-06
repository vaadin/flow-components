package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-hash-collision")
public class TreeGridHashCollisionIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void treegrid_opens_correctly() {
        // Test that child has opened
        Assert.assertEquals("BB", getTreeGrid().getCell(1, 0).getText());
    }
}
