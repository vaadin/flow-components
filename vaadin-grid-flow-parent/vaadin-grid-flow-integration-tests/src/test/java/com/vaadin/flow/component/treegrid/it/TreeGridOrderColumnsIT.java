/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Tests reorder of columns
 */
@TestPath("vaadin-grid/" + TreeGridOrderColumnsPage.VIEW)
public class TreeGridOrderColumnsIT extends AbstractComponentIT {

    private TreeGridElement treeGrid;

    @Before
    public void init() {
        open();
        treeGrid = $(TreeGridElement.class).first();
    }

    @Test
    public void gridOrder_123() {
        findElement(By.id("button-123")).click();
        assertColumnHeaders(TreeGridOrderColumnsPage.COL1_NAME,
                TreeGridOrderColumnsPage.COL2_NAME,
                TreeGridOrderColumnsPage.COL3_NAME);
    }

    @Test
    public void gridOrder_321() {
        findElement(By.id("button-321")).click();
        assertColumnHeaders(TreeGridOrderColumnsPage.COL3_NAME,
                TreeGridOrderColumnsPage.COL2_NAME,
                TreeGridOrderColumnsPage.COL1_NAME);
    }

    @Test
    public void gridOrder_321_123() {
        findElement(By.id("button-321")).click();
        assertColumnHeaders(TreeGridOrderColumnsPage.COL3_NAME,
                TreeGridOrderColumnsPage.COL2_NAME,
                TreeGridOrderColumnsPage.COL1_NAME);
        findElement(By.id("button-123")).click();
        assertColumnHeaders(TreeGridOrderColumnsPage.COL1_NAME,
                TreeGridOrderColumnsPage.COL2_NAME,
                TreeGridOrderColumnsPage.COL3_NAME);
    }

    private void assertColumnHeaders(String... headers) {
        for (int i = 0; i < headers.length; i++) {
            // columnIndex 0 is multi select in the grid
            int columnIndex = i + 1;
            Assert.assertEquals("Unexpected header for column " + i, headers[i],
                    treeGrid.getHeaderCellContent(0, columnIndex).getText());
            Assert.assertEquals("Unexpected header for column " + i,
                    TreeGridOrderColumnsPage.HEADER2_PREFIX + headers[i],
                    treeGrid.getHeaderCellContent(1, columnIndex).getText());
            Assert.assertEquals("Unexpected header for column " + i,
                    TreeGridOrderColumnsPage.HEADER3_PREFIX + headers[i],
                    treeGrid.getHeaderCellContent(2, columnIndex).getText());
        }
    }
}
