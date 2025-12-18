/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/treegrid-select-component-column-after-expand")
public class TreeGridSelectComponentColumnAfterExpandIT
        extends AbstractComponentIT {
    private TreeGridElement treeGrid;

    @Before
    public void init() {
        open();
        treeGrid = $(TreeGridElement.class).first();
    }

    /**
     * <a href="https://github.com/vaadin/vaadin-flow-components/issues/376">
     * See issue 376 in vaadin-flow-components </a>
     */
    @Test
    public void select_after_expand_should_not_remove_item_text() {
        assertExpectedValuesWhenExpanded();
        Assert.assertEquals(4, treeGrid.getRowCount());
        clickElementWithJs("collapse-button");
        Assert.assertEquals(1, treeGrid.getRowCount());
        clickElementWithJs("expand-button");
        clickElementWithJs("select-button");
        assertExpectedValuesWhenExpanded();
    }

    private void assertCellText(int rowIndex, int collIndex, String expected) {
        Assert.assertEquals(expected,
                treeGrid.getCellWaitForRow(rowIndex, collIndex).getText());
    }

    private void assertRowText(int rowIndex, String expected) {
        assertCellText(rowIndex, 0, expected);
        assertCellText(rowIndex, 1, expected);
    }

    private void assertExpectedValuesWhenExpanded() {
        assertRowText(0, "Root");
        assertRowText(1, "child");
        assertRowText(2, "sub-child");
        assertRowText(3, "child2");
    }
}
