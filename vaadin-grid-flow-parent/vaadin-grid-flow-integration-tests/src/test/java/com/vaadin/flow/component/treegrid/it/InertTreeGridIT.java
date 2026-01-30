/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/inert-tree-grid")
public class InertTreeGridIT extends AbstractComponentIT {

    private TreeGridElement treeGrid;
    private WebElement expandFirstItemButton;
    private WebElement setAllRowsVisibleButton;

    @Before
    public void init() {
        open();
        treeGrid = $(TreeGridElement.class).first();
        expandFirstItemButton = $("button").id("expand-first");
        setAllRowsVisibleButton = $("button").id("set-all-rows-visible");
    }

    @Test
    public void setAllRowsVisible_lastParentRowHasData() {
        setAllRowsVisibleButton.click();

        var cell = treeGrid.getCell(99, 0);
        Assert.assertEquals("Parent 99", cell.getText());
    }

    @Test
    public void expandFirst_setAllRowsVisible_lastChildRowHasData() {
        expandFirstItemButton.click();
        setAllRowsVisibleButton.click();

        var cell = treeGrid.getCell(100, 0);
        Assert.assertEquals("Child 0/99", cell.getText());
    }

}
