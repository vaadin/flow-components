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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.demo.GridView;
import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.demo.TabbedComponentDemoTest;
import com.vaadin.testbench.TestBenchElement;

/**
 * Integration tests for the {@link GridView}.
 */
public class GridViewIT extends TabbedComponentDemoTest {

    @Test
    public void dataIsShown() throws InterruptedException {
        openTabAndCheckForErrors("");
        GridElement grid = $(GridElement.class).id("basic");

        Assert.assertEquals("Name", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Person 1", grid.getCell(0, 0).getText());
        scroll(grid, 185);
        waitUntil(driver -> grid.getFirstVisibleRowIndex() >= 185);
        Assert.assertEquals("Person 186", grid.getCell(185, 0).getText());
    }

    @Test
    public void noHeaderIsShown() throws InterruptedException {
        openTabAndCheckForErrors("");
        GridElement grid = $(GridElement.class).id("noHeader");

        Assert.assertFalse(grid.getHeaderCell(0).isDisplayed());
        Assert.assertFalse(grid.getHeaderCell(1).isDisplayed());
    }

    @Test
    public void lazyDataIsShown() throws InterruptedException {
        openTabAndCheckForErrors("");
        GridElement grid = $(GridElement.class).id("lazy-loading");
        scrollToElement(grid);

        Assert.assertEquals("Name", grid.getHeaderCell(0).getText());
        scroll(grid, 1010);
        waitUntil(driver -> grid.getFirstVisibleRowIndex() >= 1010);
        Assert.assertEquals("Person 1011", grid.getCell(1010, 0).getText());
    }

    @Test
    public void gridAsSingleSelect() {
        openTabAndCheckForErrors("selection");
        GridElement grid = $(GridElement.class).id("single-selection");
        scrollToElement(grid);

        WebElement toggleButton = findElement(By.id("single-selection-toggle"));
        WebElement messageDiv = findElement(By.id("single-selection-message"));

        clickElementWithJs(toggleButton);
        Assert.assertEquals(
                getSelectionMessage(null, GridView.items.get(0), false),
                messageDiv.getText());
        Assert.assertTrue("Person 1 was not marked as selected",
                isRowSelected(grid, 0));
        clickElementWithJs(toggleButton);
        Assert.assertEquals(
                getSelectionMessage(GridView.items.get(0), null, false),
                messageDiv.getText());
        Assert.assertFalse("Person 1 was marked as selected",
                isRowSelected(grid, 0));

        // should be the cell in the first column's second row
        clickElementWithJs(getCell(grid, "Person 2"));
        Assert.assertTrue("Person 2 was not marked as selected",
                isRowSelected(grid, 1));
        Assert.assertEquals(
                getSelectionMessage(null, GridView.items.get(1), true),
                messageDiv.getText());
        clickElementWithJs(getCell(grid, "Person 2"));
        Assert.assertFalse("Person 2 was marked as selected",
                isRowSelected(grid, 1));

        clickElementWithJs(getCell(grid, "Person 2"));
        clickElementWithJs(toggleButton);
        Assert.assertTrue("Person 1 was not marked as selected",
                isRowSelected(grid, 0));
        Assert.assertFalse("Person 2 was marked as selected",
                isRowSelected(grid, 1));
        Assert.assertEquals(getSelectionMessage(GridView.items.get(1),
                GridView.items.get(0), false), messageDiv.getText());
        clickElementWithJs(toggleButton);
        Assert.assertFalse("Person 1 was marked as selected",
                isRowSelected(grid, 0));

        // scroll to bottom
        for (int i = 0; i < 10; i++) {
            scroll(grid, 100 + (100 * i));
        }
        waitUntilCellHasText(grid, "Person 499");
        // select item that is not in cache
        clickElementWithJs(toggleButton);
        Assert.assertEquals(
                getSelectionMessage(null, GridView.items.get(0), false),
                messageDiv.getText());
        // scroll back up
        scroll(grid, 100);
        WebElement table = findInShadowRoot(grid, By.id("table")).get(0);
        // Actually scroll up to have grid do a correct event.
        while (!getCells(grid).stream()
                .filter(cell -> "Person 1".equals(cell.getText())).findFirst()
                .isPresent()) {
            executeScript("arguments[0].scrollTop -= 100;", table);
        }
        // scroll the first row so it is visible.
        scroll(grid, 0);
        Assert.assertTrue("Person 1 was not marked as selected",
                isRowSelected(grid, 0));

        Assert.assertFalse(
                getLogEntries(Level.SEVERE).stream().findAny().isPresent());
    }

    @Test
    public void gridAsMultiSelect() {
        openTabAndCheckForErrors("selection");
        GridElement grid = $(GridElement.class).id("multi-selection");
        scrollToElement(grid);

        WebElement selectBtn = findElement(By.id("multi-selection-button"));
        WebElement messageDiv = findElement(By.id("multi-selection-message"));

        clickElementWithJs(selectBtn);
        Assert.assertEquals(
                getSelectionMessage(GridView.items.subList(0, 2),
                        GridView.items.subList(0, 5), false),
                messageDiv.getText());
        assertRowsSelected(grid, 0, 5);

        WebElement checkbox = getCellContent(grid.getCell(0, 0));
        checkbox.click();
        checkbox = getCellContent(grid.getCell(1, 0));
        checkbox.click();
        Assert.assertEquals(
                getSelectionMessage(GridView.items.subList(1, 5),
                        GridView.items.subList(2, 5), true),
                messageDiv.getText());
        assertRowsSelected(grid, 2, 5);

        checkbox = getCellContent(grid.getCell(5, 0));
        checkbox.click();
        Assert.assertTrue(isRowSelected(grid, 5));
        clickElementWithJs(selectBtn);
        assertRowsSelected(grid, 0, 6);
        Assert.assertFalse(isRowSelected(grid, 6));

        // test the select all button
        grid.findElement(By.id("selectAllCheckbox")).click();
        // deselect 1
        getCellContent(grid.getCell(0, 0)).click();
        Assert.assertEquals("Select all should have been deselected", "false",
                grid.findElement(By.id("selectAllCheckbox"))
                        .getAttribute("aria-checked"));

        getCellContent(grid.getCell(0, 0)).click();
        Assert.assertEquals("Select all should have been reselected", "true",
                grid.findElement(By.id("selectAllCheckbox"))
                        .getAttribute("aria-checked"));

    }

    @Test
    public void gridWithDisabledSelection() {
        openTabAndCheckForErrors("selection");
        GridElement grid = $(GridElement.class).id("none-selection");
        scrollToElement(grid);
        clickElementWithJs(grid
                .findElements(By.tagName("vaadin-grid-cell-content")).get(3));
        Assert.assertFalse(isRowSelected(grid, 1));
    }

    @Test
    public void gridWithColumnTemplate() {
        openTabAndCheckForErrors("using-templates");
        GridElement grid = $(GridElement.class).id("template-renderer");
        scrollToElement(grid);

        Assert.assertEquals("0", grid.getCell(0, 0).getText());
        Assert.assertEquals(
                "<div title=\"Person 1\">Person 1<br><small>23 years old</small></div>",
                grid.getCell(0, 1).getInnerHTML());
        Assert.assertEquals(
                "<div>Street S, number 49<br><small>10795</small></div>",
                grid.getCell(0, 2).getInnerHTML());
        Assert.assertEquals("<button>Update</button><button>Remove</button>",
                grid.getCell(0, 3).getInnerHTML());

        List<TestBenchElement> buttons = grid.getCell(0, 3).$("button").all();
        Assert.assertEquals(2, buttons.size());

        buttons.get(0).click();
        Assert.assertEquals(
                "<div title=\"Person 1 Updated\">Person 1 Updated<br><small>23 years old</small></div>",
                grid.getCell(0, 1).getInnerHTML());
        buttons.get(0).click();
        Assert.assertEquals(
                "<div title=\"Person 1 Updated Updated\">Person 1 Updated Updated<br><small>23 years old</small></div>",
                grid.getCell(0, 1).getInnerHTML());

        buttons.get(1).click();
        Assert.assertEquals(
                "<div title=\"Person 2\">Person 2<br><small>61 years old</small></div>",
                grid.getCell(0, 1).getInnerHTML());
    }

    @Test
    public void gridColumnApiTests() {
        openTabAndCheckForErrors("configuring-columns");
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
        String firstCellHiddenScript = "return arguments[0].shadowRoot.querySelectorAll('td')[1].hidden;";
        Assert.assertNotEquals(true, getCommandExecutor()
                .executeScript(firstCellHiddenScript, grid));
        clickElementWithJs(toggleIdColumnVisibility);
        Assert.assertEquals(true, getCommandExecutor()
                .executeScript(firstCellHiddenScript, grid));
        clickElementWithJs(toggleIdColumnVisibility);
        Assert.assertNotEquals(true, getCommandExecutor()
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
                "vaadin-grid-column");
        assertFrozenColumn(grid, frozenStatusScript,
                "toggle-selection-column-frozen",
                "vaadin-grid-flow-selection-column");

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
    public void gridDetailsRowTests() {
        openTabAndCheckForErrors("item-details");
        GridElement grid = $(GridElement.class).id("grid-with-details-row");
        scrollToElement(grid);

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));

        WebElement detailsElement = grid
                .findElement(By.className("custom-details"));

        List<WebElement> children = detailsElement
                .findElements(By.tagName("div"));
        Assert.assertEquals(2, children.size());

        Assert.assertEquals("div",
                children.get(0).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("Hi! My name is Person 1!",
                children.get(0).getText());

        Assert.assertEquals("div",
                children.get(1).getTagName().toLowerCase(Locale.ENGLISH));

        WebElement button = children.get(1).findElement(By.tagName("button"));

        Assert.assertEquals("Update Person", button.getText());

        clickElementWithJs(detailsElement.findElement(By.tagName("button")));

        Assert.assertTrue(hasCell(grid, "Person 1 Updated"));
    }

    @Test
    public void gridDetailsRowServerAPI() {
        openTabAndCheckForErrors("item-details");
        GridElement grid = $(GridElement.class).id("grid-with-details-row-2");
        scrollToElement(grid);

        assertAmountOfOpenDetails(grid, 0);

        getCellContent(grid.getCell(1, 2)).click();
        assertAmountOfOpenDetails(grid, 1);
        Assert.assertThat(
                grid.findElement(By.className("custom-details"))
                        .getAttribute("innerHTML"),
                CoreMatchers.containsString("Hi! My name is <b>Person 2!</b>"));

        getCellContent(grid.getCell(3, 2)).click();
        assertAmountOfOpenDetails(grid, 2);

        getCellContent(grid.getCell(1, 2)).click();
        getCellContent(grid.getCell(3, 2)).click();
        Assert.assertThat(
                "Details should be closed after clicking the button again",
                grid.findElement(By.className("custom-details"))
                        .getAttribute("innerHTML"),
                CoreMatchers.not(CoreMatchers
                        .containsString("Hi! My name is <b>Person 2!</b>")));
    }

    private void assertAmountOfOpenDetails(WebElement grid,
            int expectedAmount) {
        waitUntil(driver -> grid.findElements(By.className("custom-details"))
                .size() == expectedAmount);
        Assert.assertEquals(expectedAmount,
                grid.findElements(By.className("custom-details")).size());
    }

    @Test
    public void gridWithComponentRenderer_cellsAreRenderered() {
        openTabAndCheckForErrors("using-components");
        WebElement grid = findElement(By.id("component-renderer"));
        scrollToElement(grid);

        Assert.assertTrue(hasComponentRendereredCell(grid,
                "<div>Hi, I'm Person 1!</div>"));
        Assert.assertTrue(hasComponentRendereredCell(grid,
                "<div>Hi, I'm Person 2!</div>"));

        WebElement idField = findElement(By.id("component-renderer-id-field"));
        WebElement nameField = findElement(
                By.id("component-renderer-name-field"));
        WebElement updateButton = findElement(
                By.id("component-renderer-update-button"));

        idField.sendKeys("1");
        executeScript("arguments[0].blur();", idField);
        nameField.sendKeys("SomeOtherName");
        executeScript("arguments[0].blur();", nameField);
        clickElementWithJs(updateButton);

        waitUntil(driver -> hasComponentRendereredCell(grid,
                "<div>Hi, I'm SomeOtherName!</div>"), 3);

        idField.sendKeys(Keys.BACK_SPACE, "2");
        executeScript("arguments[0].blur();", idField);
        nameField.sendKeys("2");
        executeScript("arguments[0].blur();", nameField);
        clickElementWithJs(updateButton);

        waitUntil(driver -> hasComponentRendereredCell(grid,
                "<div>Hi, I'm SomeOtherName2!</div>"));
    }

    @Test
    public void gridWithComponentRenderer_detailsAreRenderered() {
        openTabAndCheckForErrors("using-components");
        WebElement grid = findElement(By.id("component-renderer"));
        scrollToElement(grid);

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));
        assertComponentRendereredDetails(grid, 0, "Person 1");

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));
        assertComponentRendereredDetails(grid, 1, "Person 2");

        WebElement idField = findElement(By.id("component-renderer-id-field"));
        WebElement nameField = findElement(
                By.id("component-renderer-name-field"));
        WebElement updateButton = findElement(
                By.id("component-renderer-update-button"));

        idField.sendKeys("1");
        executeScript("arguments[0].blur();", idField);
        nameField.sendKeys("SomeOtherName");
        executeScript("arguments[0].blur();", nameField);
        clickElementWithJs(updateButton);

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));
        assertComponentRendereredDetails(grid, 0, "SomeOtherName");

        idField.sendKeys(Keys.BACK_SPACE, "2");
        executeScript("arguments[0].blur();", idField);
        nameField.sendKeys("2");
        executeScript("arguments[0].blur();", nameField);
        clickElementWithJs(updateButton);

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));
        assertComponentRendereredDetails(grid, 1, "SomeOtherName2");
    }

    @Test
    public void gridWithSorting() {
        openTabAndCheckForErrors("sorting");
        GridElement grid = $(GridElement.class).id("grid-sortable-columns");
        scrollToElement(grid);

        getCellContent(grid.getHeaderCell(0)).click();
        assertSortMessageEquals(QuerySortOrder.asc("name").build(), true);
        getCellContent(grid.getHeaderCell(2)).click();
        assertSortMessageEquals(
                QuerySortOrder.asc("street").thenAsc("number").build(), true);
        getCellContent(grid.getHeaderCell(2)).click();
        assertSortMessageEquals(
                QuerySortOrder.desc("street").thenDesc("number").build(), true);
        getCellContent(grid.getHeaderCell(2)).click();
        assertSortMessageEquals(Collections.emptyList(), true);

        // enable multi sort
        clickElementWithJs(findElement(By.id("grid-multi-sort-toggle")));
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(1)).click();
        assertSortMessageEquals(
                QuerySortOrder.asc("age").thenAsc("name").build(), true);
    }

    @Test
    public void gridWithSorting_switchColumnSorting() {
        openTabAndCheckForErrors("sorting");
        GridElement grid = $(GridElement.class).id("grid-sortable-columns");
        scrollToElement(grid);

        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(0)).click();

        Assert.assertEquals(
                "Current sort order: . Sort originates from the client: true.",
                findElement(By.id("grid-sortable-columns-message")).getText());
    }

    @Test
    public void gridWithSorting_invertAndResetSortings() {
        openTabAndCheckForErrors("sorting");
        GridElement grid = $(GridElement.class).id("grid-sortable-columns");
        scrollToElement(grid);

        WebElement invertButton = findElement(
                By.id("grid-sortable-columns-invert-sortings"));
        WebElement resetButton = findElement(
                By.id("grid-sortable-columns-reset-sortings"));

        getCellContent(grid.getHeaderCell(0)).click();
        assertSortMessageEquals(QuerySortOrder.asc("name").build(), true);

        clickElementWithJs(invertButton);
        assertSortMessageEquals(QuerySortOrder.desc("name").build(), false);

        clickElementWithJs(invertButton);
        assertSortMessageEquals(QuerySortOrder.asc("name").build(), false);

        clickElementWithJs(resetButton);
        assertSortMessageEquals(Collections.emptyList(), false);

        // enable multi sort
        clickElementWithJs(findElement(By.id("grid-multi-sort-toggle")));
        getCellContent(grid.getHeaderCell(0)).click();
        getCellContent(grid.getHeaderCell(1)).click();
        assertSortMessageEquals(
                QuerySortOrder.asc("age").thenAsc("name").build(), true);
        clickElementWithJs(invertButton);
        assertSortMessageEquals(
                QuerySortOrder.desc("age").thenDesc("name").build(), false);

        clickElementWithJs(resetButton);
        assertSortMessageEquals(Collections.emptyList(), false);

    }

    private void assertSortMessageEquals(List<QuerySortOrder> querySortOrders,
            boolean fromClient) {
        String sortOrdersString = querySortOrders.stream()
                .map(querySortOrder -> String.format(
                        "{sort property: %s, direction: %s}",
                        querySortOrder.getSorted(),
                        querySortOrder.getDirection()))
                .collect(Collectors.joining(", "));
        Assert.assertEquals(String.format(
                "Current sort order: %s. Sort originates from the client: %s.",
                sortOrdersString, fromClient),
                findElement(By.id("grid-sortable-columns-message")).getText());
    }

    @Test
    public void gridWithHeaderAndFooterRows_headerAndFooterAreRenderered() {
        openTabAndCheckForErrors("header-and-footer-rows");

        GridElement grid = $(GridElement.class)
                .id("grid-with-header-and-footer-rows");
        scrollToElement(grid);

        assertRendereredHeaderCell(grid.getHeaderCell(0), "Name", false, true);
        assertRendereredHeaderCell(grid.getHeaderCell(1), "Age", false, true);
        assertRendereredHeaderCell(grid.getHeaderCell(2), "Street", false,
                false);
        assertRendereredHeaderCell(grid.getHeaderCell(3), "Postal Code", false,
                false);

        List<WebElement> columnGroups = grid
                .findElements(By.tagName("vaadin-grid-column-group"));

        Assert.assertThat(
                "The first column group should have 'Basic Information' header text",
                columnGroups.get(0).getAttribute("innerHTML"),
                CoreMatchers.containsString("Basic Information"));

        Assert.assertThat(
                "The second column group should have 'Address Information' header text",
                columnGroups.get(1).getAttribute("innerHTML"),
                CoreMatchers.containsString("Address Information"));

        List<WebElement> columns = grid
                .findElements(By.tagName("vaadin-grid-column"));

        Assert.assertThat("There should be a cell with the renderered footer",
                columns.get(0).getAttribute("innerHTML"),
                CoreMatchers.containsString("Total: 500 people"));
    }

    @Test
    public void gridWithHeaderWithComponentRenderer_headerAndFooterAreRenderered() {
        openTabAndCheckForErrors("header-and-footer-rows");

        GridElement grid = $(GridElement.class)
                .id("grid-header-with-components");
        scrollToElement(grid);

        GridTHTDElement headerCell = grid.getHeaderCell(0);
        assertRendereredHeaderCell(headerCell, "<label>Name</label>", true,
                true);

        headerCell = grid.getHeaderCell(1);
        assertRendereredHeaderCell(headerCell, "<label>Age</label>", true,
                true);

        headerCell = grid.getHeaderCell(2);
        assertRendereredHeaderCell(headerCell, "<label>Street</label>", true,
                false);

        headerCell = grid.getHeaderCell(3);
        assertRendereredHeaderCell(headerCell, "<label>Postal Code</label>",
                true, false);

        Assert.assertTrue(
                "There should be a cell with the renderered 'Basic Information' header",
                hasComponentRendereredHeaderCell(grid,
                        "<label>Basic Information</label>"));

        Assert.assertTrue("There should be a cell with the renderered footer",
                hasComponentRendereredHeaderCell(grid,
                        "<label>Total: 500 people</label>"));
    }

    @Test
    public void gridWithFiltering() {
        openTabAndCheckForErrors("filtering");

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
    public void beanGrid_columnsForPropertiesAddedWithCorrectHeaders() {
        openTabAndCheckForErrors("configuring-columns");
        GridElement grid = $(GridElement.class).id("bean-grid");
        scrollToElement(grid);

        Assert.assertEquals("Unexpected amount of columns", 6,
                grid.findElements(By.tagName("vaadin-grid-column")).size());

        Assert.assertEquals("Address", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Age", grid.getHeaderCell(1).getText());
        Assert.assertEquals("Email", grid.getHeaderCell(2).getText());
        Assert.assertEquals("Name", grid.getHeaderCell(3).getText());
        Assert.assertEquals("Subscriber", grid.getHeaderCell(4).getText());
        Assert.assertEquals("Postal Code", grid.getHeaderCell(5).getText());
    }

    @Test
    public void beanGrid_valuesAreConvertedToStrings() {
        openTabAndCheckForErrors("configuring-columns");
        WebElement grid = findElement(By.id("bean-grid"));
        scrollToElement(grid);

        Assert.assertTrue(
                "Address should be displayed as a String starting with the street name",
                getCells(grid).stream()
                        .anyMatch(cell -> cell.getText().startsWith("Street")));
    }

    @Test
    public void beanGrid_setColumns_columnsChanged() {
        openTabAndCheckForErrors("configuring-columns");
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
        Assert.assertEquals("Name", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Age", grid.getHeaderCell(1).getText());
        Assert.assertEquals("Address", grid.getHeaderCell(2).getText());
        Assert.assertTrue(
                "The cells on the first column should display person names",
                grid.getCell(0, 0).getText().startsWith("Person"));
    }

    @Test
    public void basicRenderers_rowsAreRenderedAsExpected() {
        openTabAndCheckForErrors("using-renderers");
        GridElement grid = $(GridElement.class).id("grid-basic-renderers");
        scrollToElement(grid);
        waitUntilCellHasText(grid, "Item 1");

        Assert.assertEquals("Item 1", grid.getCell(0, 0).getText());
        Assert.assertEquals("$ 73.10", grid.getCell(0, 1).getText());
        Assert.assertEquals("1/10/18 11:43:59 AM", grid.getCell(0, 2).getText());
        Assert.assertEquals("Jan 11, 2018", grid.getCell(0, 3).getText());
        assertRendereredContent("$$$", grid.getCell(0, 4).getInnerHTML());
        Assert.assertEquals("<button>Remove</button>", grid.getCell(0, 5).getInnerHTML());

        Assert.assertEquals("Item 2", grid.getCell(1, 0).getText());
        Assert.assertEquals("$ 24.05", grid.getCell(1, 1).getText());
        Assert.assertEquals("1/10/18 11:07:31 AM", grid.getCell(1, 2).getText());
        Assert.assertEquals("Jan 24, 2018", grid.getCell(1, 3).getText());
        assertRendereredContent("$", grid.getCell(1, 4).getInnerHTML());
        Assert.assertEquals("<button>Remove</button>", grid.getCell(1, 5).getInnerHTML());
    }

    @Test
    public void heightByRows_allRowsAreFetched() {
        openTabAndCheckForErrors("height-by-rows");
        GridElement grid = $(GridElement.class).id("grid-height-by-rows");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() == 50);

        Assert.assertEquals("Grid should have heightByRows set to true", "true",
                grid.getAttribute("heightByRows"));
    }

    @Test
    public void basicFeatures() {
        openTabAndCheckForErrors("basic-features");
        GridElement grid = $(GridElement.class).id("grid-basic-feature");
        scrollToElement(grid);
        waitUntil(driver -> grid.getAllColumns().size() == 11);

        TestBenchElement filteringField = grid
                .findElement(By.tagName("vaadin-text-field"));
        filteringField.sendKeys("sek");
        blur();

        Assert.assertThat(
                "The first company name should contain the applied filter string",
                grid.getCell(0, 0).getInnerHTML().toLowerCase(),
                CoreMatchers.containsString("sek"));
    }

    @Test
    public void disabledGrid_itemsAreDisabled() {
        openTabAndCheckForErrors("");
        GridElement grid = $(GridElement.class).id("disabled-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);
        Assert.assertFalse("Grid should be disabled", grid.isEnabled());

        GridTRElement row = grid.getRow(0);
        GridTHTDElement cell = row.getCell(grid.getColumn("Action"));
        WebElement button = cell.getContext().findElement(By.tagName("button"));

        Assert.assertFalse("The rendered button should be disabled",
                button.isEnabled());

        grid.scrollToRow(499);
        waitUntil(driver -> grid.getRowCount() == 500);

        row = grid.getRow(499);
        cell = row.getCell(grid.getColumn("Action"));
        button = cell.getContext().findElement(By.tagName("button"));

        Assert.assertFalse("The rendered button should be disabled",
                button.isEnabled());
    }

    @Test
    public void contextMenu() {
        openTabAndCheckForErrors("context-menu");
        GridElement grid = $(GridElement.class).id("context-menu-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        assertFirstCells(grid, "Person 1", "Person 2", "Person 3", "Person 4");

        grid.getCell(2, 0).contextClick();
        $("vaadin-item").first().click(); // Update button
        assertFirstCells(grid, "Person 1", "Person 2", "Person 3 Updated",
                "Person 4");

        grid.getCell(1, 0).contextClick();
        $("vaadin-item").get(1).click(); // Remove button
        assertFirstCells(grid, "Person 1", "Person 3 Updated", "Person 4",
                "Person 5");
    }

    @Test
    public void itemClickListener_singleClick_doubleClickFireClick() {
        openTabAndCheckForErrors("click-listeners");

        GridElement grid = $(GridElement.class).id("item-click-listener");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);
        row.click(10, 10);

        WebElement clickInfo = findElement(By.id("clicked-item"));

        Assert.assertEquals("Clicked Item: Person 1", clickInfo.getText());

        // Clear the message
        clickInfo.click();
        // self check
        Assert.assertEquals("", clickInfo.getText());

        GridTHTDElement headerCell = grid.getHeaderCell(0);
        headerCell.click(10, 10);

        // No event
        Assert.assertEquals("", clickInfo.getText());
    }

    @Test
    public void itemDoubleClickListener() {
        openTabAndCheckForErrors("click-listeners");

        GridElement grid = $(GridElement.class).id("item-doubleclick-listener");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);
        row.doubleClick();

        WebElement clickInfo = findElement(By.id("doubleclicked-item"));

        Assert.assertEquals("Double Clicked Item: Person 1",
                clickInfo.getText());

        // Clear the message
        clickInfo.click();
        // self check
        Assert.assertEquals("", clickInfo.getText());

        GridTHTDElement headerCell = grid.getHeaderCell(0);
        headerCell.doubleClick();

        // No event
        Assert.assertEquals("", clickInfo.getText());
    }

    @Test
    public void bufferedEditor_invalidName() {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id("buffered-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        WebElement edit = findElement(By.className("edit"));
        edit.click();

        // Write invalid name. There should be a status message with validation
        // error.
        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameField);

        nameInput.clear();
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        GridTHTDElement editColumn = row.getCell(grid.getAllColumns().get(2));
        editColumn.$("vaadin-button").attribute("class", "save").first()
                .click();

        String validation = findElement(By.id("validation")).getText();
        // There is an error in the status message
        Assert.assertEquals("Name should start with Person", validation);

        WebElement msg = findElement(By.id("buffered-editor-msg"));
        // No save events
        Assert.assertEquals("", msg.getText());

        editColumn.$("vaadin-button").attribute("class", "cancel").first()
                .click();

        Assert.assertEquals(personName, nameCell.getText());
        // Still no any save events
        Assert.assertEquals("", msg.getText());
    }

    @Test
    public void bufferedEditor_cancelWithEscape() {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id("buffered-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        WebElement edit = findElement(By.className("edit"));
        edit.click();

        // Test cancel by ESC
        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameField);

        nameInput.clear();
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ESCAPE);

        Assert.assertFalse("Edit button should be visible",
                nameCell.$("vaadin-text-field").exists());

        nameColumn = grid.getColumn("Name");
        nameCell = row.getCell(nameColumn);
        Assert.assertEquals("Field name should not have changed.", personName,
                nameCell.getText());

    }

    @Test
    public void bufferedEditor_validName() {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id("buffered-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");

        WebElement edit = findElement(By.className("edit"));
        edit.click();

        // check that shown Edit buttons are disabled
        WebElement nextEditButton = grid.getRow(1)
                .getCell(grid.getAllColumns().get(2)).$("vaadin-button")
                .first();
        Assert.assertEquals(Boolean.TRUE.toString(),
                nextEditButton.getAttribute("disabled"));

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        TestBenchElement subscriberCheckbox = subscriberCell
                .$("vaadin-checkbox").first();
        boolean isSubscriber = subscriberCheckbox
                .getAttribute("checked") != null;

        // Write valid name.
        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameField);

        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        subscriberCheckbox.click();

        GridColumnElement editColumn = grid.getAllColumns().get(2);

        TestBenchElement save = row.getCell(editColumn).$("vaadin-button")
                .first();
        save.click();

        String validation = findElement(By.id("validation")).getText();
        // Validation is empty
        Assert.assertEquals("", validation);

        // New data should be shown in the grid cell
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(String.valueOf(!isSubscriber),
                subscriberCell.getText());

        // There should be an event for the edited person
        WebElement msg = findElement(By.id("buffered-editor-msg"));
        Assert.assertEquals(personName + "foo, " + !isSubscriber,
                msg.getText());
    }

    @Test
    public void notBufferedEditor() {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id("not-buffered-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement subscriberCheckbox = subscriberCell
                .$("vaadin-checkbox").first();
        boolean isSubscriber = subscriberCheckbox
                .getAttribute("checked") != null;

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameField);

        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        subscriberCheckbox.click();

        // click on another row
        grid.getRow(1).click(10, 10);

        // New data should be shown in the grid cell
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(String.valueOf(!isSubscriber),
                subscriberCell.getText());

        // The edited person should have new data
        WebElement msg = findElement(By.id("not-buffered-editor-msg"));
        Assert.assertEquals(personName + "foo, " + !isSubscriber,
                msg.getText());
    }

    @Test
    public void notBufferedEditor_closeEditorUsingKeyboard() {
        assertCloseEditorUsingKeyBoard("not-buffered-editor");
    }

    @Test
    public void dynamicEditor_bufferedMode() {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);

        GridColumnElement editColumn = grid.getAllColumns().get(3);

        WebElement editButon = row.getCell(editColumn).$("vaadin-button")
                .first();
        editButon.click();

        // check that shown Edit buttons are disabled
        WebElement nextEditButton = grid.getRow(1).getCell(editColumn)
                .$("vaadin-button").first();
        Assert.assertEquals(Boolean.TRUE.toString(),
                nextEditButton.getAttribute("disabled"));

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameField);

        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        GridTHTDElement email = row.getCell(grid.getAllColumns().get(2));

        TestBenchElement emailField = email.$("vaadin-text-field").first();
        TestBenchElement emailInput = emailField.$("input").first();

        // clear the email and type wrong value
        emailInput.clear();
        emailInput.sendKeys("bar");
        emailInput.sendKeys(Keys.ENTER);

        TestBenchElement save = row.getCell(editColumn).$("vaadin-button")
                .first();
        save.click();

        // Check there is a validation error
        WebElement validation = findElement(By.id("email-validation"));
        Assert.assertEquals("Invalid email", validation.getText());

        GridTHTDElement subscriberCell = row
                .getCell(grid.getAllColumns().get(1));

        // Switch subscriber value off
        TestBenchElement checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        // email field should become read-only
        emailField = email.$("vaadin-text-field").first();
        emailInput = emailField.$("input").first();

        Assert.assertEquals(Boolean.TRUE.toString(),
                emailInput.getAttribute("readonly"));

        Assert.assertEquals("Not a subscriber",
                emailInput.getAttribute("value"));

        // Switch subscriber value on
        checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        emailField = email.$("vaadin-text-field").first();
        emailInput = emailField.$("input").first();
        emailInput.sendKeys("@example.com");
        emailInput.sendKeys(Keys.ENTER);
        save.click();

        WebElement updatedItemMsg = findElement(
                By.id("buffered-dynamic-editor-msg"));

        waitUntil(driver -> !updatedItemMsg.getText().isEmpty());

        Assert.assertEquals("Person 1foo, true, bar@example.com",
                updatedItemMsg.getText());
    }

    @Test
    public void dynamicEditor_bufferedMode_useKeyboardToSwitchEditorComponent() {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        // start to edit
        GridColumnElement editColumn = grid.getAllColumns().get(3);
        grid.getRow(0).getCell(editColumn).$("vaadin-button").first().click();

        assertBufferedEditing(grid);
    }

    @Test
    public void dynamicEditor_bufferedMode_updateSubscriberValue_useKeyboardToSwitchEditorComponent() {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id("buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        // start to edit
        GridColumnElement editColumn = grid.getAllColumns().get(3);
        row.getCell(editColumn).$("vaadin-button").first().click();

        GridTHTDElement subscriberCell = row
                .getCell(grid.getAllColumns().get(1));
        subscriberCell.$("vaadin-checkbox").first().click();

        GridTHTDElement nameCell = row.getCell(grid.getColumn("Name"));

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameField);
        nameInput.sendKeys(Keys.TAB);

        // skip checkbox and focus the email field
        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();

        TestBenchElement emailField = row.getCell(grid.getAllColumns().get(2))
                .$("vaadin-text-field").first();
        Assert.assertNotNull(emailField.getAttribute("focused"));
        Assert.assertEquals("Not a subscriber",
                emailField.getAttribute("value"));

        subscriberCell.$("vaadin-checkbox").first().click();

        assertBufferedEditing(grid);
    }

    @Test
    public void dynamicNotBufferedEditor() throws InterruptedException {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class)
                .id("not-buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameField);
        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);

        GridTHTDElement emailCell = row.getCell(grid.getAllColumns().get(2));
        Assert.assertEquals(1, emailCell.$("vaadin-text-field").all().size());

        TestBenchElement subscriberCheckbox = subscriberCell
                .$("vaadin-checkbox").first();
        subscriberCheckbox.click();

        // The editor component should disappear for non-subscriber
        Assert.assertEquals(0, emailCell.$("vaadin-text-field").all().size());

        subscriberCheckbox.click();
        // Now it should return back
        Assert.assertEquals(1, emailCell.$("vaadin-text-field").all().size());

        TestBenchElement emailInput = emailCell.$("vaadin-text-field").first()
                .$("input").first();
        emailInput.clear();
        emailInput.sendKeys("bar@example.com");
        emailCell.sendKeys(Keys.ENTER);

        // click on another row
        grid.getRow(1).click(10, 10);

        // New data should be shown in the grid cell
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(Boolean.TRUE.toString(), subscriberCell.getText());
        Assert.assertEquals("bar@example.com",
                row.getCell(grid.getAllColumns().get(2)).getText());

        // The edited person should have new data
        WebElement msg = findElement(By.id("not-buffered-dynamic-editor-msg"));
        Assert.assertEquals(personName + "foo, true, bar@example.com",
                msg.getText());
    }

    @Test
    public void dynamicNotBufferedEditor_navigateUsingKeyboard()
            throws InterruptedException {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class)
                .id("not-buffered-dynamic-editor");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);
        String personName = nameCell.getText();

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        assertElementHasFocus(nameField);

        nameInput.sendKeys("foo");
        nameInput.sendKeys(Keys.ENTER);
        nameInput.click();
        nameInput.sendKeys(Keys.TAB);

        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();

        new Actions(getDriver())
                .sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE)
                .sendKeys("org").build().perform();

        // click on another row
        grid.getRow(1).click(10, 10);

        // New data should be shown in the grid cell
        Assert.assertEquals(personName + "foo", nameCell.getText());
        Assert.assertEquals(Boolean.TRUE.toString(), subscriberCell.getText());
        Assert.assertEquals("mailss@example.org",
                row.getCell(grid.getAllColumns().get(2)).getText());

        // The edited person should have new data
        WebElement msg = findElement(By.id("not-buffered-dynamic-editor-msg"));
        Assert.assertEquals(personName + "foo, true, mailss@example.org",
                msg.getText());
    }

    @Test
    public void dynamicNotBufferedEditor_closeEditorUsingKeyboard()
            throws InterruptedException {
        GridElement grid = assertCloseEditorUsingKeyBoard(
                "not-buffered-dynamic-editor");

        GridTRElement row = grid.getRow(0);

        GridColumnElement emailColumn = grid.getColumn("E-mail");
        GridTHTDElement emailCell = row.getCell(emailColumn);

        row.doubleClick();

        TestBenchElement emailField = emailCell.$("vaadin-text-field").first();

        TestBenchElement emailInput = emailField.$("input").first();
        emailInput.click();
        emailInput.sendKeys(Keys.TAB);
        assertNotBufferedEditorClosed(grid);
    }

    @Test
    public void stylingDemo_classNamesGenerated() {
        openTabAndCheckForErrors("styling");
        GridElement grid = $(GridElement.class).id("class-name-generator");
        scrollToElement(grid);

        GridStylingIT.assertCellClassNames(grid, 0, 0, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 0, 1, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 0, 2, "subscriber");

        GridStylingIT.assertCellClassNames(grid, 5, 0, "");
        GridStylingIT.assertCellClassNames(grid, 5, 1, "minor");
        GridStylingIT.assertCellClassNames(grid, 5, 2, "");

        GridStylingIT.assertCellClassNames(grid, 9, 0, "subscriber");
        GridStylingIT.assertCellClassNames(grid, 9, 1, "subscriber minor");
        GridStylingIT.assertCellClassNames(grid, 9, 2, "subscriber");
    }

    private void assertElementHasFocus(WebElement element) {
        Assert.assertTrue("Element should have focus",
                (Boolean) executeScript(
                        "return document.activeElement === arguments[0]",
                        element));
    }

    private GridElement assertCloseEditorUsingKeyBoard(String gridId) {
        openTabAndCheckForErrors("grid-editor");

        GridElement grid = $(GridElement.class).id(gridId);
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);

        row.doubleClick();

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        nameInput.click();

        nameInput.sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB));

        assertNotBufferedEditorClosed(grid);

        GridColumnElement subscriberColumn = grid.getColumn("Subscriber");
        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        checkbox.sendKeys(Keys.TAB);

        assertNotBufferedEditorClosed(grid);

        // restore the previous state
        row.doubleClick();

        checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        // close the editor
        grid.getRow(1).click(5, 5);

        return grid;
    }

    private void assertNotBufferedEditorClosed(GridElement grid) {
        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTRElement row = grid.getRow(0);
        GridTHTDElement nameCell = row.getCell(nameColumn);
        Assert.assertEquals(
                "Unexpected shown text field in the name cell when the editor should be closed",
                0, nameCell.$("vaadin-text-field").all().size());
    }

    private void assertFirstCells(GridElement grid, String... cellContents) {
        IntStream.range(0, cellContents.length).forEach(i -> {
            Assert.assertEquals(cellContents[i], grid.getCell(i, 0).getText());
        });
    }

    public void assertVariants() {
        openTabAndCheckForErrors("");
        verifyThemeVariantsBeingToggled();
    }

    private WebElement getCellContent(GridTHTDElement cell) {
        return (WebElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0].firstElementChild;",
                cell);
    }

    private void assertRendereredContent(String expected, String content) {
        Assert.assertThat(content,
                CoreMatchers.allOf(
                        CoreMatchers.startsWith("<flow-component-renderer"),
                        CoreMatchers.containsString(expected),
                        CoreMatchers.endsWith("</flow-component-renderer>")));
    }

    private static String getSelectionMessage(Object oldSelection,
            Object newSelection, boolean isFromClient) {
        return String.format(
                "Selection changed from %s to %s, selection is from client: %s",
                oldSelection, newSelection, isFromClient);
    }

    private void scroll(WebElement grid, int index) {
        getCommandExecutor().executeScript(
                "arguments[0].scrollToIndex(" + index + ")", grid);
    }

    private void waitUntilCellHasText(WebElement grid, String text) {
        waitUntil(driver -> getCells(grid).stream()
                .filter(cell -> text.equals(cell.getText())).findFirst()
                .isPresent());
    }

    private void assertRowsSelected(GridElement grid, int first, int last) {
        IntStream.range(first, last).forEach(
                rowIndex -> Assert.assertTrue(isRowSelected(grid, rowIndex)));
    }

    private WebElement getRow(WebElement grid, int row) {
        return getInShadowRoot(grid, By.id("items"))
                .findElements(By.cssSelector("tr")).get(row);
    }

    private boolean isRowSelected(GridElement grid, int row) {
        return grid.getRow(row).isSelected();
    }

    private boolean hasCell(GridElement grid, String text) {
        return getCell(grid, text) != null;
    }

    private WebElement getCell(GridElement grid, String text) {
        return grid.getCell(text);
    }

    private boolean hasComponentRendereredCell(WebElement grid, String text) {
        return hasComponentRendereredCell(grid, text,
                "flow-component-renderer");
    }

    private void assertRendereredHeaderCell(GridTHTDElement headerCell,
            String text, boolean componentRenderer, boolean withSorter) {

        String html = headerCell.getInnerHTML();
        if (withSorter) {
            Assert.assertThat(html,
                    CoreMatchers.containsString("<vaadin-grid-sorter"));
        } else {
            Assert.assertThat(html, CoreMatchers
                    .not(CoreMatchers.containsString("<vaadin-grid-sorter")));
        }
        if (componentRenderer) {
            Assert.assertThat(html,
                    CoreMatchers.containsString("<flow-component-renderer"));
        }
        Assert.assertThat(html, CoreMatchers.containsString(text));
    }

    private boolean hasComponentRendereredHeaderCell(WebElement grid,
            String text) {
        return hasComponentRendereredCell(grid, text,
                "flow-component-renderer");
    }

    private boolean hasComponentRendereredCell(WebElement grid, String text,
            String componentTag) {
        List<WebElement> cells = grid
                .findElements(By.tagName("vaadin-grid-cell-content"));

        return cells.stream()
                .map(cell -> cell.findElements(By.tagName(componentTag)))
                .filter(list -> !list.isEmpty()).map(list -> list.get(0))
                .anyMatch(cell -> text.equals(cell.getAttribute("innerHTML")));
    }

    private void assertComponentRendereredDetails(WebElement grid, int rowIndex,
            String personName) {
        waitUntil(driver -> isElementPresent(
                By.id("person-card-" + (rowIndex + 1))), 20);

        WebElement element = findElement(
                By.id("person-card-" + (rowIndex + 1)));

        element = element.findElement(By.tagName("vaadin-horizontal-layout"));
        Assert.assertNotNull(element);

        List<WebElement> layouts = element
                .findElements(By.tagName("vaadin-vertical-layout"));
        Assert.assertNotNull(layouts);
        Assert.assertEquals(2, layouts.size());

        Pattern pattern = Pattern
                .compile("<label>Name:\\s?([\\w\\s]*)</label>");
        Matcher innerHTML = pattern
                .matcher(layouts.get(0).getAttribute("innerHTML"));
        Assert.assertTrue(
                "No result found for " + pattern.toString()
                        + " when searching for name: " + personName,
                innerHTML.lookingAt());
        Assert.assertEquals("Expected name was not same as found one.",
                personName, innerHTML.group(1));
    }

    private List<WebElement> getCells(WebElement grid) {
        return grid.findElements(By.tagName("vaadin-grid-cell-content"));
    }

    private void assertFrozenColumn(WebElement grid, String frozenStatusScript,
            String buttonId, String columnTag) {
        WebElement toggleIdColumnFrozen = findElement(By.id(buttonId));
        WebElement idColumn = grid.findElements(By.tagName(columnTag)).get(0);
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

    private void assertBufferedEditing(GridElement grid) {
        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        nameInput.click();

        nameInput.sendKeys("foo");

        // skip checkbox and focus the email field
        new Actions(getDriver()).sendKeys(Keys.TAB).sendKeys(Keys.TAB).build()
                .perform();

        // change the e-mail to .org
        new Actions(getDriver())
                .sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE)
                .sendKeys("org").build().perform();

        // press enter on the save button
        new Actions(getDriver()).sendKeys(Keys.TAB).sendKeys(Keys.ENTER).build()
                .perform();

        WebElement updatedItemMsg = findElement(
                By.id("buffered-dynamic-editor-msg"));

        waitUntil(driver -> !updatedItemMsg.getText().isEmpty());

        Assert.assertEquals("Person 1foo, true, mailss@example.org",
                updatedItemMsg.getText());
    }

    @Override
    protected String getTestPath() {
        return "/vaadin-grid-it-demo";
    }
}
