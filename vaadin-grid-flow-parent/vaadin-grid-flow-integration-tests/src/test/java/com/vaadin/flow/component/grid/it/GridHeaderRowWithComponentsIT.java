/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-header-row-with-components")
public class GridHeaderRowWithComponentsIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
    }

    @Test // for https://github.com/vaadin/vaadin-grid-flow/issues/172
    public void appendHeaderRowsWithComponents_headerCellsAreRenderedInCorrectOrder() {
        Assert.assertEquals(
                "There should be 4 header cells after appending 2 header rows for "
                        + "a Grid with two columns",
                4, getHeaderCellCount());

        Assert.assertTrue(
                "The first header cell should contain the component "
                        + "of the first appended header row",
                getInnerHtml(grid.getHeaderCellContent(0, 0))
                        .contains("<label>foo</label>"));
        Assert.assertTrue(
                "The second header cell should contain the component "
                        + "of the second appended header row",
                getInnerHtml(grid.getHeaderCellContent(1, 0))
                        .contains("<label>bar</label>"));
    }

    @Test
    public void prependHeader_setText_setComponent_componentOverridesText() {
        clickElementWithJs("set-both-text-and-component");
        var headerContent = getInnerHtml(grid.getHeaderCellContent(0, 0));
        Assert.assertFalse(
                "The header cell should not contain the text after "
                        + "overriding it with a component",
                headerContent.contains("this is text"));
        Assert.assertTrue(
                "The header cell should contain the component which was last set",
                headerContent.contains("<label>this is component</label>"));
    }

    @Test
    public void initiallyHiddenHeaderComponents_headerContentIsEmpty() {
        Assert.assertEquals("", getInnerHtml(grid.getHeaderCellContent(0, 1)));
        Assert.assertEquals("", getInnerHtml(grid.getHeaderCellContent(1, 1)));
    }

    @Test
    public void initiallyHiddenHeaderComponents_setVisible_headerContentIsUpdated() {
        clickElementWithJs("toggle-col-2-headers-visible");
        Assert.assertNotEquals("",
                getInnerHtml(grid.getHeaderCellContent(0, 1)));
        Assert.assertNotEquals("",
                getInnerHtml(grid.getHeaderCellContent(1, 1)));
    }

    private long getHeaderCellCount() {
        var thead = grid.$("*").id("header");
        var headerRows = thead.findElements(By.tagName("tr"));
        return headerRows.stream()
                .mapToLong(row -> row.findElements(By.tagName("th")).size())
                .sum();
    }

    private String getInnerHtml(TestBenchElement headerCell) {
        return headerCell.getDomProperty("innerHTML");
    }
}
