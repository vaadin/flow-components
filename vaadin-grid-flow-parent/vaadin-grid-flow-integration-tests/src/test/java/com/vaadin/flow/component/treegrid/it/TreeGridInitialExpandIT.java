package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-initial-expand")
public class TreeGridInitialExpandIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void initial_expand_of_items() {
        Assert.assertEquals("parent1", getTreeGrid().getCell(0, 0).getText());
        Assert.assertEquals("parent1-child1",
                getTreeGrid().getCell(1, 0).getText());
        Assert.assertEquals("parent1-child2",
                getTreeGrid().getCell(2, 0).getText());
        Assert.assertEquals("parent2", getTreeGrid().getCell(3, 0).getText());
        Assert.assertEquals("parent2-child2",
                getTreeGrid().getCell(4, 0).getText());
    }
}
