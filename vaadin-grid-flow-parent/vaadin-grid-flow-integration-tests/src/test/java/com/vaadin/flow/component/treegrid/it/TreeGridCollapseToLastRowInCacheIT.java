/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-scrolling")
public class TreeGridCollapseToLastRowInCacheIT extends AbstractComponentIT {

    @Test
    public void testCollapsingNode_removesLastRowFromGridCache_noInternalError() {
        open(TreeGridScrollingPage.NODES_PARAMETER + "=50");

        TreeGridElement grid = $(TreeGridElement.class).first();

        grid.expandWithClick(0);
        grid.expandWithClick(1);

        checkLogsForErrors();

        Assert.assertEquals("0 | 0", grid.getCell(0, 0).getText());
        Assert.assertEquals("1 | 0", grid.getCell(1, 0).getText());
        Assert.assertEquals("2 | 0", grid.getCell(2, 0).getText());

        grid.collapseWithClick(0);

        Assert.assertEquals("0 | 0", grid.getCell(0, 0).getText());
        Assert.assertEquals("0 | 1", grid.getCell(1, 0).getText());

        checkLogsForErrors();
    }
}
