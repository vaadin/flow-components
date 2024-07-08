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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/treegrid-detach-attach")
public class TreeGridDetachAttachIT extends AbstractComponentIT {

    private TreeGridElement grid;
    private TestBenchElement toggleAttachedButton;
    private TestBenchElement useAutoWidthColumnButton;

    @Before
    public void before() {
        open();
        grid = $(TreeGridElement.class).first();
        toggleAttachedButton = $("button").id("toggle-attached");
        useAutoWidthColumnButton = $("button").id("use-auto-width-column");
    }

    @Test
    public void scrollDown_detach_attach_firstItemsRendered() {
        grid.scrollToRow(150);

        toggleAttachedButton.click();
        toggleAttachedButton.click();

        grid = $(TreeGridElement.class).first();
        Assert.assertEquals("0 | 0", grid.getCell(0, 0).getText());
    }

    @Test
    public void useAutoWidthColumn_detach_attach_shouldHaveProperColumnWidth() {
        grid.expandWithClick(0);
        useAutoWidthColumnButton.click();

        Integer columnOffsetWidth = grid.getCell(0, 0)
                .getPropertyInteger("offsetWidth");

        toggleAttachedButton.click();
        toggleAttachedButton.click();

        grid = $(TreeGridElement.class).first();
        Assert.assertEquals(columnOffsetWidth,
                grid.getCell(0, 0).getPropertyInteger("offsetWidth"));
    }
}
