/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

/**
 * Tests for dynamically adding new columns with different renderers after the
 * Grid has already been attached and rendered.
 */
@TestPath("vaadin-grid/adding-columns")
public class AddingColumnsIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void gridRendered_addColumnWithValueProvider_cellsRendered() {
        clickElementWithJs("add-value-provider-column");
        assertCellContents("foo", "bar");
    }

    @Test
    public void gridRendered_addColumnWithTemplateRenderer_cellsRendered() {
        clickElementWithJs("add-template-column");
        assertCellContents("<div>20</div>", "<div>30</div>");
    }

    @Test
    public void gridRendered_addColumnWithComponentRenderer_cellsRendered() {
        clickElementWithJs("add-component-column");
        // Using contains-operation to avoid depending on the
        // <flow-component-renderer> wrapper which is an implementation detail.
        assertCellContentsContain("<label>foo</label>", "<label>bar</label>");
    }

    @Test
    public void gridRendered_addColumnWithNumberRenderer_cellsRendered() {
        clickElementWithJs("add-number-column");
        assertCellContents("20", "30");
    }

    @Test
    public void gridRendered_addColumnWithLocalDateRenderer_cellsRendered() {
        clickElementWithJs("add-local-date-column");
        assertCellContents("January 20, 1990", "January 30, 1990");
    }

    @Test
    public void gridRendered_addColumnWithLocalDateTimeRenderer_cellsRendered() {
        clickElementWithJs("add-local-date-time-column");
        assertCellContents("January 1, 1980 1:20 AM",
                "January 1, 1980 1:30 AM");
    }

    @Test
    public void gridRendered_addColumnWithNativeButtonRenderer_cellsRendered() {
        clickElementWithJs("add-button-column");
        assertCellContents("<button>click</button>", "<button>click</button>");
    }

    private void assertCellContents(String expectedFirstRow,
            String expectedSecondRow) {
        Assert.assertEquals(expectedFirstRow,
                grid.getCell(0, 0).getInnerHTML());
        Assert.assertEquals(expectedSecondRow,
                grid.getCell(1, 0).getInnerHTML());
    }

    private void assertCellContentsContain(String expectedFirstRow,
            String expectedSecondRow) {
        Assert.assertThat(grid.getCell(0, 0).getInnerHTML(),
                CoreMatchers.containsString(expectedFirstRow));
        Assert.assertThat(grid.getCell(1, 0).getInnerHTML(),
                CoreMatchers.containsString(expectedSecondRow));
    }

}
