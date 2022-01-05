/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
