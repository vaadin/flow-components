/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-scroll-to-column")
public class GridScrollToColumnIT extends AbstractComponentIT {
    private GridElement grid;

    @Before
    public void setUp() {
        open();
        grid = $(GridElement.class).waitForFirst();
    }

    @Test
    public void scrollToColumnByIndex_columnIsVisible() {
        var headerCell = grid.getHeaderCell(0, 10);

        Assert.assertFalse(isCellVisible(headerCell));

        clickElementWithJs("scroll-by-index");

        Assert.assertTrue(isCellVisible(headerCell));
    }

    @Test
    public void scrollToColumnByReference_columnIsVisible() {
        var headerCell = grid.getHeaderCell(0, 10);

        Assert.assertFalse(isCellVisible(headerCell));

        clickElementWithJs("scroll-by-reference");

        Assert.assertTrue(isCellVisible(headerCell));
    }

    @Test
    public void scrollToColumnByIndexWithTestbench_columnIsVisible() {
        var headerCell = grid.getHeaderCell(0, 10);

        Assert.assertFalse(isCellVisible(headerCell));

        grid.scrollToColumn(10);

        Assert.assertTrue(isCellVisible(headerCell));
    }

    @Test
    public void scrollToColumnByReferenceWithTestbench_columnIsVisible() {
        var headerCell = grid.getHeaderCell(0, 10);

        Assert.assertFalse(isCellVisible(headerCell));

        var column = grid.getColumn("Column 10");
        grid.scrollToColumn(column);

        Assert.assertTrue(isCellVisible(headerCell));
    }

    private Boolean isCellVisible(GridTHTDElement cell) {
        return (Boolean) (cell.getCommandExecutor()).executeScript(
                """
                        const cell = arguments[0];
                        const bounds = cell.getBoundingClientRect();
                        const centerX = bounds.left + bounds.width / 2;
                        const centerY = bounds.top + bounds.height / 2;
                        const cellContent = document.elementFromPoint(centerX, centerY);
                        return cell.querySelector('slot').assignedElements().includes(cellContent);
                        """,
                cell);
    }
}
