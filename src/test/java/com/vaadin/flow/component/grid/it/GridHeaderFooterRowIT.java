/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-header-footer-rows")
public class GridHeaderFooterRowIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id("grid");
    }

    @Test
    public void addHeadersAfterGridIsRendered_cellsAreRenderedInCorrectOrder() {
        clickButton("append-header");
        assertHeaderOrder(0);
        clickButton("append-header");
        assertHeaderOrder(0, 1);
        clickButton("prepend-header");
        assertHeaderOrder(2, 0, 1);
    }

    @Test
    public void addFootersAfterGridIsRendered_cellsAreRenderedInCorrectOrder() {
        clickButton("append-footer");
        assertFooterOrder(0);
        clickButton("append-footer");
        assertFooterOrder(0, 1);
        clickButton("prepend-footer");
        assertFooterOrder(2, 0, 1);
    }

    @Test
    public void appendHeaderAfterGridIsRendered_lastHeaderIsEmpty() {
        clickButton("append-header");
        clickButton("append-header-without-content");
        List<WebElement> headerCells = getHeaderCells();
        String lastHeaderContent = headerCells.get(headerCells.size() - 1)
                .getAttribute("innerHTML");
        Assert.assertTrue(
                "The appended header should be empty, but contained text: '"
                        + lastHeaderContent + "'",
                lastHeaderContent.isEmpty());
    }

    @Ignore // https://github.com/vaadin/vaadin-grid-flow/issues/167
    @Test
    public void makeSortableAfterGridIsRendered_sorterIsRendered() {
        clickButton("append-header");
        clickButton("set-sortable");
        assertBottomHeaderHasGridSorter();
    }

    @Test
    public void makeSortableAndAppendHeaderAfterGridIsRendered_sorterIsRendered() {
        clickButton("append-header");
        clickButton("set-sortable");
        clickButton("append-header");
        assertBottomHeaderHasGridSorter();
    }

    @Test
    public void addHeaderRow_setMultiselect_disableSelection() {
        clickButton("prepend-header");
        clickButton("set-multiselect");
        List<WebElement> headerCells = getHeaderCells();
        Assert.assertEquals(
                "There should be one header cell for multiselection checkbox "
                        + "and another for the header",
                2, headerCells.size());
        Assert.assertThat(
                "The first header cell should contain the multiselection checkbox",
                headerCells.get(0).getAttribute("innerHTML"),
                CoreMatchers.containsString("vaadin-checkbox"));
        Assert.assertEquals(
                "The second header cell should contain the set text", "0",
                headerCells.get(1).getText());

        clickButton("disable-selection");
        headerCells = getHeaderCells();

        Assert.assertEquals(
                "There should be only one header cell after removing selection column",
                1, headerCells.size());
        Assert.assertEquals(
                "The remaining header cell should be the one set with HeaderRow API",
                "0", headerCells.get(0).getText());
    }

    private void assertBottomHeaderHasGridSorter() {
        List<WebElement> headerCells = getHeaderCells();
        WebElement bottomCell = headerCells.get(headerCells.size() - 1);
        Assert.assertThat(bottomCell.getAttribute("innerHTML"),
                CoreMatchers.containsString("vaadin-grid-sorter"));
    }

    private void assertHeaderOrder(int... numbers) {
        List<WebElement> headerCells = getHeaderCells();
        Assert.assertEquals("Unexpected amount of header cells", numbers.length,
                headerCells.size());
        IntStream.range(0, numbers.length).forEach(i -> {
            Assert.assertEquals("Unexpected header cell content",
                    String.valueOf(numbers[i]), headerCells.get(i).getText());
        });
    }

    private List<WebElement> getHeaderCells() {
        WebElement thead = findInShadowRoot(grid, By.id("header")).get(0);
        List<WebElement> headers = thead.findElements(By.tagName("th"));

        List<String> cellNames = headers.stream().map(header -> header
                .findElement(By.tagName("slot")).getAttribute("name"))
                .collect(Collectors.toList());

        List<WebElement> headerCells = cellNames.stream()
                .map(name -> grid.findElement(By.cssSelector(
                        "vaadin-grid-cell-content[slot='" + name + "']")))
                .collect(Collectors.toList());

        return headerCells;
    }

    private void assertFooterOrder(int... numbers) {
        List<WebElement> footerCells = getFooterCells();
        Assert.assertEquals("Unexpected amount of footer cells", numbers.length,
                footerCells.size());
        IntStream.range(0, numbers.length).forEach(i -> {
            Assert.assertEquals("Unexpected footer cell content",
                    String.valueOf(numbers[i]) + "",
                    footerCells.get(i).getText());
        });
    }

    private List<WebElement> getFooterCells() {
        WebElement thead = findInShadowRoot(grid, By.id("footer")).get(0);
        List<WebElement> footers = thead.findElements(By.tagName("td"));

        List<String> cellNames = footers.stream().map(footer -> footer
                .findElement(By.tagName("slot")).getAttribute("name"))
                .collect(Collectors.toList());

        List<WebElement> footerCells = cellNames.stream()
                .map(name -> grid.findElement(By.cssSelector(
                        "vaadin-grid-cell-content[slot='" + name + "']")))
                .collect(Collectors.toList());

        return footerCells;
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();
    }

}
