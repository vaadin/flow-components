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
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/renderer-swap")
public class GridRendererSwapIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void swapRenderer_cellsRenderedWithNewRenderer() {
        Assert.assertEquals("$ 73.10", grid.getCell(0, 1).getText());

        swapRenderer();

        Assert.assertEquals("<span style=\"color: red\">73.1</span>",
                getCellHtml(0, 1));
        Assert.assertEquals("<span style=\"color: blue\">24.05</span>",
                getCellHtml(1, 1));
    }

    @Test
    public void swapRenderer_editorIsShown() {
        swapRenderer();
        editFirstItem();

        NumberFieldElement priceField = grid.getCell(0, 1)
                .$(NumberFieldElement.class).first();
        Assert.assertEquals("73.1", priceField.getValue());
    }

    @Test
    public void openAndCloseEditor_swapRenderer_editorIsShown() {
        editFirstItem();
        $("button").id("cancel-edit").click();

        swapRenderer();
        editFirstItem();

        NumberFieldElement priceField = grid.getCell(0, 1)
                .$(NumberFieldElement.class).first();
        Assert.assertEquals("73.1", priceField.getValue());
    }

    private void swapRenderer() {
        $("button").id("swap-renderer").click();
    }

    private void editFirstItem() {
        $("button").id("edit-first").click();
    }

    private String getCellHtml(int row, int column) {
        return TestHelper
                .stripComments(grid.getCell(row, column).getInnerHTML());
    }
}
