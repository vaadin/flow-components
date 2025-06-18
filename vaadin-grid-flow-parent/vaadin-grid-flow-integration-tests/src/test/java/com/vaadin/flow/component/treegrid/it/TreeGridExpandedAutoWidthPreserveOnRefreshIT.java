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

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-expanded-auto-width-preserve-on-refresh")
public class TreeGridExpandedAutoWidthPreserveOnRefreshIT
        extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
    }

    @Test
    public void refresh_expectSameColumnWidth() {
        var grid = $(TreeGridElement.class).first();
        var columnOffsetWidth = grid.getCell(0, 0)
                .getPropertyInteger("offsetWidth");

        getDriver().navigate().refresh();

        grid = $(TreeGridElement.class).first();
        Assert.assertEquals(columnOffsetWidth,
                grid.getCell(0, 0).getPropertyInteger("offsetWidth"));
    }

}
