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

import com.vaadin.flow.component.grid.demo.GridView;
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
                "<div>Street S, number 30<br><small>16142</small></div>",
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
                "<div title=\"Person 2\">Person 2<br><small>28 years old</small></div>",
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
    public void gridWidthSorting() {
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
                CoreMatchers.containsString("Total: 499 people"));
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
                        "<label>Total: 499 people</label>"));
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

        Assert.assertEquals("Unexpected amount of columns", 4,
                grid.findElements(By.tagName("vaadin-grid-column")).size());

        Assert.assertEquals("Address", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Name", grid.getHeaderCell(1).getText());
        Assert.assertEquals("Age", grid.getHeaderCell(2).getText());
        Assert.assertEquals("Postal Code", grid.getHeaderCell(3).getText());
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
        WebElement grid = findElement(By.id("grid-basic-renderers"));
        scrollToElement(grid);
        waitUntilCellHasText(grid, "Item 1");

        List<WebElement> cells = grid
                .findElements(By.tagName("vaadin-grid-cell-content"));

        int offset = getCellsOffsetFromTheHeaders(grid, cells);

        assertCellContent("Item 1", cells.get(offset));
        assertCellContent("$ 72.76", cells.get(offset + 1));
        assertCellContent("1/10/18 11:19:11 AM", cells.get(offset + 2));
        assertCellContent("Jan 25, 2018", cells.get(offset + 3));
        assertRendereredContent("$$$", cells.get(offset + 4));
        assertCellContent("<button>Remove</button>", cells.get(offset + 5));

        assertCellContent("Item 2", cells.get(offset + 6));
        assertCellContent("$ 30.87", cells.get(offset + 7));
        assertCellContent("1/10/18 11:14:54 AM", cells.get(offset + 8));
        assertCellContent("Jan 19, 2018", cells.get(offset + 9));
        assertRendereredContent("$", cells.get(offset + 10));
        assertCellContent("<button>Remove</button>", cells.get(offset + 11));
    }

    @Test
    public void heightByRows_allRowsAreFetched() {
        openTabAndCheckForErrors("height-by-rows");
        GridElement grid = $(GridElement.class).id("grid-height-by-rows");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() == 49);

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

        grid.scrollToRow(498);
        waitUntil(driver -> grid.getRowCount() == 499);

        row = grid.getRow(498);
        cell = row.getCell(grid.getColumn("Action"));
        button = cell.getContext().findElement(By.tagName("button"));

        Assert.assertFalse("The rendered button should be disabled",
                button.isEnabled());
    }

    private WebElement getCellContent(GridTHTDElement cell) {
        return (WebElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0].firstElementChild;",
                cell);
    }

    private int getCellsOffsetFromTheHeaders(WebElement grid,
            List<WebElement> cells) {
        int numberOfColumns = grid
                .findElements(By.tagName("vaadin-grid-column")).size();
        for (int i = numberOfColumns; i < cells.size(); i++) {
            WebElement cell = cells.get(i);
            String content = cell.getAttribute("innerHTML");
            if (!content.trim().isEmpty()
                    && !content.startsWith("<flow-component-renderer ")
                    && !content.startsWith("<button>")) {
                return i;
            }
        }
        return 0;
    }

    private void assertRendereredContent(String expected, WebElement cell) {
        Assert.assertThat(cell.getAttribute("innerHTML"),
                CoreMatchers.allOf(
                        CoreMatchers.startsWith("<flow-component-renderer"),
                        CoreMatchers.containsString(expected),
                        CoreMatchers.endsWith("</flow-component-renderer>")));
    }

    private void assertCellContent(String expected, WebElement cell) {
        Assert.assertEquals("Wrong content of the rendered cell", expected,
                cell.getAttribute("innerHTML"));
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
        try {
            /*
             * Wait a bit for the changes to propagate from the server to the
             * client. Without this wait, some elements can be stale when this
             * method is executed, causing instability on the tests.
             */
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        waitUntil(driver -> {
            List<WebElement> elements = grid
                    .findElements(By.className("custom-details"));
            return elements.stream()
                    .filter(el -> el.getAttribute("id")
                            .equals("person-card-" + (rowIndex + 1)))
                    .findAny().isPresent();
        });
        WebElement element = grid.findElements(By.className("custom-details"))
                .stream()
                .filter(el -> el.getAttribute("id")
                        .equals("person-card-" + (rowIndex + 1)))
                .findFirst().get();

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

    @Override
    protected String getTestPath() {
        return "/vaadin-grid";
    }
}
