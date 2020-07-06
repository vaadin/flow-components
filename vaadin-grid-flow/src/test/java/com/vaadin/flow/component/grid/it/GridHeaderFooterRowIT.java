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
    public void appendHeader_headerTemplatesAdded() {
        clickButton("append-header");
        assertColumnsHaveTemplates("header", true);
        assertColumnsHaveTemplates("footer", false);
    }

    @Test
    public void prependHeader_headerTemplatesAdded() {
        clickButton("prepend-header");
        assertColumnsHaveTemplates("header", true);
        assertColumnsHaveTemplates("footer", false);
    }

    @Test
    public void appendFooter_footerTemplatesAdded() {
        clickButton("append-footer");
        assertColumnsHaveTemplates("header", false);
        assertColumnsHaveTemplates("footer", true);
    }

    @Test
    public void prependFooter_footerTemplatesAdded() {
        clickButton("prepend-footer");
        assertColumnsHaveTemplates("header", false);
        assertColumnsHaveTemplates("footer", true);
    }

    @Test
    public void appendHeader_appendFooter_headerAndFooterTemplatesAdded() {
        clickButton("append-header");
        clickButton("append-footer");
        assertColumnsHaveTemplates("header", true);
        assertColumnsHaveTemplates("footer", true);
    }

    private void assertColumnsHaveTemplates(String className,
            boolean haveTemplates) {
        List<WebElement> columns = grid
                .findElements(By.className("vaadin-grid-column"));
        columns.forEach(col -> {
            List<WebElement> templates = col
                    .findElements(By.tagName("template"));
            if (haveTemplates) {
                Assert.assertTrue(
                        templates.stream().allMatch(template -> template
                                .getAttribute("class").contains(className)));
            } else {
                Assert.assertTrue(
                        templates.stream().noneMatch(template -> template
                                .getAttribute("class").contains(className)));
            }
        });
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

    @Test
    public void addHeader_makeSortable_sorterIsRendered() {
        clickButton("append-header");
        clickButton("set-sortable");
        assertHeaderHasGridSorter(0);
    }

    @Test
    public void makeSortable_addHeader_sorterIsRendered() {
        clickButton("set-sortable");
        clickButton("append-header");
        assertHeaderHasGridSorter(0);
    }

    @Test
    public void appendHeader_makeSortable_appendHeader_sorterIsRenderedOnTheUpperHeader() {
        clickButton("append-header");
        clickButton("set-sortable");
        clickButton("append-header");
        assertHeaderHasGridSorter(0);
    }

    @Test
    public void addHeaders_makeSortable_sorterIsRenderedOnTheFirstCreatedHeader() {
        clickButton("prepend-header");
        clickButton("prepend-header");
        clickButton("prepend-header");
        clickButton("set-sortable");
        assertHeaderHasGridSorter(2);

        init();
        clickButton("append-header");
        clickButton("append-header");
        clickButton("append-header");
        clickButton("set-sortable");
        assertHeaderHasGridSorter(0);

        init();
        clickButton("append-header");
        clickButton("prepend-header");
        clickButton("append-header");
        clickButton("set-sortable");
        assertHeaderHasGridSorter(1);
    }

    @Test
    public void makeSortable_changeHeaderContents_contentsChangeButSortersRemain() {
        clickButton("append-header");
        clickButton("append-header");
        clickButton("prepend-header");
        clickButton("set-sortable");
        clickButton("set-texts-for-headers");
        clickButton("set-components-for-headers");
        assertHeaderComponentsAreRendered();
        assertHeaderHasGridSorter(1);
        clickButton("set-texts-for-headers");
        assertHeaderTextsAreRendered();
        assertHeaderHasGridSorter(1);
    }

    @Test
    public void addHeadersAndFooters_removeColumn_columnGroupsRemoved() {
        clickButton("prepend-header");
        clickButton("append-footer");
        clickButton("prepend-header");
        clickButton("append-footer");

        clickButton("remove-column");

        List<WebElement> columns = grid
                .findElements(By.tagName("vaadin-grid-column"));
        List<WebElement> groups = grid
                .findElements(By.tagName("vaadin-grid-column-group"));

        Assert.assertEquals(
                "There should be no column or column-group elements after removing the only column",
                0, columns.size() + groups.size());
    }

    private void assertHeaderComponentsAreRendered() {
        List<String> headerContents = getHeaderContents();
        headerContents.forEach(content -> Assert.assertThat(
                "Label components should be rendered in the headers", content,
                CoreMatchers.containsString("<label>foo</label>")));
        headerContents.forEach(content -> Assert.assertThat(
                "Header components should have overridden the header texts",
                content, CoreMatchers.not(CoreMatchers.containsString("bar"))));
    }

    private void assertHeaderTextsAreRendered() {
        List<String> headerContents = getHeaderContents();
        headerContents.forEach(content -> Assert.assertThat(
                "The text that was set should be rendered in the headers",
                content, CoreMatchers.containsString("bar")));
        headerContents.forEach(content -> Assert.assertThat(
                "The text should override the previously set components",
                content,
                CoreMatchers.not(CoreMatchers.containsString("label"))));
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

    @Test
    public void columnSetHeader_alwaysTargetsTheFirstCreatedHeader() {
        clickButton("column-set-header");
        assertHeaderOrder(0);
        clickButton("append-header");
        assertHeaderOrder(0, 1);
        clickButton("column-set-header");
        assertHeaderOrder(2, 1);
        clickButton("prepend-header");
        assertHeaderOrder(3, 2, 1);
        clickButton("column-set-header");
        assertHeaderOrder(3, 4, 1);
    }

    /*
     * Creates step-by-step a hierarchical structure with three rows for both
     * header and footer, with in-most row having 4 cells, the middle one 2
     * cells and the out-most row 1 cell.
     */
    @Test
    public void joinCells() {
        grid = $(GridElement.class).id("grid2");
        clickButton("prepend-header-2");
        clickButton("prepend-header-2");
        clickButton("append-footer-2");
        clickButton("append-footer-2");
        assertHeaderOrder(4, 5, 6, 7, 0, 1, 2, 3);
        assertFooterOrder(8, 9, 10, 11, 12, 13, 14, 15);

        clickButton("join-headers-01");
        assertHeaderOrder(16, 6, 7, 0, 1, 2, 3);
        assertFooterOrder(8, 9, 10, 11, 12, 13, 14, 15);

        clickButton("join-headers-12");
        assertHeaderOrder(16, 17, 0, 1, 2, 3);
        assertFooterOrder(8, 9, 10, 11, 12, 13, 14, 15);

        clickButton("join-footers-23");
        assertFooterOrder(8, 9, 10, 11, 12, 13, 18);
        assertHeaderOrder(16, 17, 0, 1, 2, 3);

        clickButton("join-footers-01");
        assertFooterOrder(8, 9, 10, 11, 19, 18);
        assertHeaderOrder(16, 17, 0, 1, 2, 3);

        clickButton("prepend-header-2");
        assertHeaderOrder(20, 21, 16, 17, 0, 1, 2, 3);
        assertFooterOrder(8, 9, 10, 11, 19, 18);

        clickButton("append-footer-2");
        assertFooterOrder(8, 9, 10, 11, 19, 18, 22, 23);
        assertHeaderOrder(20, 21, 16, 17, 0, 1, 2, 3);

        clickButton("join-footers-01");
        assertFooterOrder(8, 9, 10, 11, 19, 18, 24);
        assertHeaderOrder(20, 21, 16, 17, 0, 1, 2, 3);

        clickButton("join-headers-01");
        assertHeaderOrder(25, 16, 17, 0, 1, 2, 3);
        assertFooterOrder(8, 9, 10, 11, 19, 18, 24);
    }

    private void assertHeaderHasGridSorter(int headerIndexFromTop) {
        List<WebElement> headerCells = getHeaderCells();
        WebElement cellWithSorter = headerCells.get(headerIndexFromTop);
        Assert.assertThat(cellWithSorter.getAttribute("innerHTML"),
                CoreMatchers.containsString("vaadin-grid-sorter"));

        Assert.assertTrue("Only one header should have the sorting indicators",
                headerCells.stream()
                        .filter(cell -> !cell.equals(cellWithSorter))
                        .noneMatch(cell -> cell.getAttribute("innerHTML")
                                .contains("vaadin-grid-sorter")));
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

        List<WebElement> headers = thead.findElements(By.tagName("tr")).stream()
                .filter(tr -> tr.getAttribute("hidden") == null)
                .flatMap(tr -> tr.findElements(By.tagName("th")).stream())
                .collect(Collectors.toList());

        List<String> cellNames = headers.stream().map(header -> header
                .findElement(By.tagName("slot")).getAttribute("name"))
                .collect(Collectors.toList());

        List<WebElement> headerCells = cellNames.stream()
                .map(name -> grid.findElement(By.cssSelector(
                        "vaadin-grid-cell-content[slot='" + name + "']")))
                .collect(Collectors.toList());

        return headerCells;
    }

    private List<String> getHeaderContents() {
        return getHeaderCells().stream()
                .map(cell -> cell.getAttribute("innerHTML"))
                .collect(Collectors.toList());
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
        WebElement tfoot = findInShadowRoot(grid, By.id("footer")).get(0);

        List<WebElement> footers = tfoot.findElements(By.tagName("tr")).stream()
                .filter(tr -> tr.getAttribute("hidden") == null)
                .flatMap(tr -> tr.findElements(By.tagName("td")).stream())
                .collect(Collectors.toList());

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
