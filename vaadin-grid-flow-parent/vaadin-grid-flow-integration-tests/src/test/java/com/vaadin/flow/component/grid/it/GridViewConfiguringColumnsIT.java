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
import org.openqa.selenium.WebElement;

import java.util.List;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/configuring-columns")
public class GridViewConfiguringColumnsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void gridColumnApiTests() {
        WebElement grid = findElement(By.id("column-api-example"));
        scrollToElement(grid);

        Assert.assertEquals("Two resize handlers should be present", 2L,
                getCommandExecutor().executeScript(
                        "return arguments[0].shadowRoot.querySelectorAll('[part~=\"resize-handle\"]').length;",
                        grid));

        Assert.assertEquals("First width is fixed", "75px",
                getCommandExecutor().executeScript(
                        "return arguments[0].shadowRoot.querySelectorAll('th')[1].style.width;",
                        grid));

        WebElement toggleIdColumnVisibility = findElement(
                By.id("toggle-id-column-visibility"));
        String firstCellHiddenScript = "return arguments[0].shadowRoot.querySelectorAll('tr')[1].querySelectorAll('td').length;";
        Assert.assertEquals(4L, getCommandExecutor()
                .executeScript(firstCellHiddenScript, grid));
        clickElementWithJs(toggleIdColumnVisibility);
        waitUntil(c -> 3L == (long) getCommandExecutor()
                .executeScript(firstCellHiddenScript, grid));
        clickElementWithJs(toggleIdColumnVisibility);
        waitUntil(c -> 4L == (long) getCommandExecutor()
                .executeScript(firstCellHiddenScript, grid));

        Assert.assertNotEquals("true",
                grid.getAttribute("columnReorderingAllowed"));

        WebElement toggleUserReordering = findElement(
                By.id("toggle-user-reordering"));
        clickElementWithJs(toggleUserReordering);
        Assert.assertEquals("true",
                grid.getAttribute("columnReorderingAllowed"));
        clickElementWithJs(toggleUserReordering);
        Assert.assertNotEquals("true",
                grid.getAttribute("columnReorderingAllowed"));

        String frozenStatusScript = "return arguments[0].frozen";
        assertFrozenColumn(grid, frozenStatusScript, "toggle-id-column-frozen",
                "vaadin-grid-column", 0);
        assertFrozenColumn(grid, frozenStatusScript,
                "toggle-selection-column-frozen",
                "vaadin-grid-flow-selection-column", 0);

        String frozenToEndStatusScript = "return arguments[0].frozenToEnd";
        assertFrozenColumn(grid, frozenToEndStatusScript,
                "toggle-age-column-frozen-to-end", "vaadin-grid-column", 2);

        WebElement alignments = findElement(By.id("toggle-text-align"));

        List<WebElement> radioGroups = alignments
                .findElements(By.tagName("vaadin-radio-button"));
        radioGroups.get(2).click();
        assertTextAlignment(grid, 2, ColumnTextAlign.END);

        radioGroups.get(1).click();
        assertTextAlignment(grid, 2, ColumnTextAlign.CENTER);

        radioGroups.get(0).click();
        assertTextAlignment(grid, 2, ColumnTextAlign.START);
    }

    @Test
    public void beanGrid_columnsForPropertiesAddedWithCorrectHeaders() {
        GridElement grid = $(GridElement.class).id("bean-grid");
        scrollToElement(grid);

        Assert.assertEquals("Unexpected amount of columns", 13,
                grid.findElements(By.tagName("vaadin-grid-column")).size());

        Assert.assertEquals("Address", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Age", grid.getHeaderCell(1).getText());
        Assert.assertEquals("Birth Date", grid.getHeaderCell(2).getText());
        Assert.assertEquals("Deceased", grid.getHeaderCell(3).getText());
        Assert.assertEquals("Email", grid.getHeaderCell(4).getText());
        Assert.assertEquals("First Name", grid.getHeaderCell(5).getText());
        Assert.assertEquals("Gender", grid.getHeaderCell(6).getText());
        Assert.assertEquals("Last Name", grid.getHeaderCell(7).getText());
        Assert.assertEquals("Rent", grid.getHeaderCell(8).getText());
        Assert.assertEquals("Salary", grid.getHeaderCell(9).getText());
        Assert.assertEquals("Salary Double", grid.getHeaderCell(10).getText());
        Assert.assertEquals("Subscriber", grid.getHeaderCell(11).getText());
        Assert.assertEquals("Postal Code", grid.getHeaderCell(12).getText());
    }

    @Test
    public void beanGrid_valuesAreConvertedToStrings() {
        WebElement grid = findElement(By.id("bean-grid"));
        scrollToElement(grid);

        findElement(By.id("show-address-information")).click();

        List<?> cellTexts = (List<?>) getCommandExecutor().executeScript(
                "var result = [];  var cells = arguments[0].querySelectorAll('vaadin-grid-cell-content');"
                        + "for (i=0; i<cells.length; i++) { result.push(cells[i].innerText); } return result;",
                grid);

        Assert.assertTrue(
                "Address should be displayed as a String starting with the street name",
                cellTexts.stream().anyMatch(
                        cell -> cell.toString().startsWith("Street")));
    }

    @Test
    public void beanGrid_setColumns_columnsChanged() {
        GridElement grid = $(GridElement.class).id("bean-grid");
        scrollToElement(grid);

        findElement(By.id("show-address-information")).click();

        Assert.assertEquals(
                "Grid should have three columnsa after calling "
                        + "setColumns() with three properties",
                3, grid.getAllColumns().size());
        Assert.assertEquals("Street", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Number", grid.getHeaderCell(1).getText());
        Assert.assertEquals("Postal Code", grid.getHeaderCell(2).getText());
        Assert.assertTrue(
                "The cells on the first column should display street names",
                grid.getCell(0, 0).getText().startsWith("Street"));

        findElement(By.id("show-basic-information")).click();

        Assert.assertEquals(
                "Grid should have three columnsa after calling "
                        + "setColumns() with three properties",
                3, grid.getAllColumns().size());
        Assert.assertEquals("First Name", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Age", grid.getHeaderCell(1).getText());
        Assert.assertEquals("Address", grid.getHeaderCell(2).getText());
        Assert.assertTrue(
                "The cells on the first column should display person names",
                grid.getCell(0, 0).getText().startsWith("Person"));
    }

    private void assertFrozenColumn(WebElement grid, String frozenStatusScript,
            String buttonId, String columnTag, Integer idx) {
        WebElement toggleIdColumnFrozen = findElement(By.id(buttonId));
        WebElement idColumn = grid.findElements(By.tagName(columnTag)).get(idx);
        Assert.assertEquals(false, getCommandExecutor()
                .executeScript(frozenStatusScript, idColumn));
        clickElementWithJs(toggleIdColumnFrozen);
        Assert.assertEquals(true, getCommandExecutor()
                .executeScript(frozenStatusScript, idColumn));
        clickElementWithJs(toggleIdColumnFrozen);
        Assert.assertEquals(false, getCommandExecutor()
                .executeScript(frozenStatusScript, idColumn));
    }

    private void assertTextAlignment(WebElement grid, int column,
            ColumnTextAlign align) {
        Assert.assertEquals(align.getPropertyValue(),
                getCommandExecutor().executeScript(
                        "return arguments[0].querySelectorAll('vaadin-grid-column')["
                                + column + "].textAlign;",
                        grid));
    }

}
