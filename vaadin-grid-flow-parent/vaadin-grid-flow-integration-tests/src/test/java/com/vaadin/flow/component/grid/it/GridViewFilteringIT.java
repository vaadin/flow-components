/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

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
