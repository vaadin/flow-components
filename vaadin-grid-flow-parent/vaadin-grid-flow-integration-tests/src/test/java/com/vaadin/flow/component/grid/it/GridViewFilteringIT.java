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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/filtering")
public class GridViewFilteringIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void gridWithFiltering() {
        GridElement grid = $(GridElement.class).id("grid-with-filters");
        scrollToElement(grid);

        IntStream.range(0, 4).forEach(i -> {
            GridTHTDElement headerCell = grid.getHeaderCell(i);
            assertRendereredHeaderCell(headerCell, "<vaadin-text-field", true,
                    false);
        });

        grid.findElement(By.tagName("vaadin-text-field")).sendKeys("6");
        waitUntil(driver -> grid.getCell(0, 0).getText().contains("Person 6"));
    }

    @Test
    public void scrollToEnd_filter_rowsUpdated() {
        GridElement grid = $(GridElement.class).id("grid-with-filters");

        // Scroll to the end of the grid
        grid.scrollToRow(grid.getRowCount() - 1);
        // Filter "Name" column with "100"
        grid.findElement(By.tagName("vaadin-text-field")).sendKeys("100");
        waitUntil(driver -> grid.getRowCount() == 1);

        // Expect the one remaining row's first cell to contain
        // text "Person 100"
        Assert.assertEquals("Person 100", grid.getCell(0, 0).getText());
    }

    private void assertRendereredHeaderCell(GridTHTDElement headerCell,
            String text, boolean componentRenderer, boolean withSorter) {

        String html = headerCell.getInnerHTML();
        if (withSorter) {
            Assert.assertTrue(html.contains("<vaadin-grid-sorter"));
        } else {
            Assert.assertFalse(html.contains("<vaadin-grid-sorter"));
        }
        if (componentRenderer) {
            Assert.assertTrue(html.contains("<flow-component-renderer"));
        }
        Assert.assertTrue(html.contains(text));
    }

}
