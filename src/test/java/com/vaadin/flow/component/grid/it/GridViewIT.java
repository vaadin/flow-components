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

import java.util.Arrays;
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
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.demo.TabbedComponentDemoTest;

/**
 * Integration tests for the {@link GridView}.
 */
public class GridViewIT extends TabbedComponentDemoTest {

    @Test
    public void dataIsShown() throws InterruptedException {
        openTabAndCheckForErrors("");
        WebElement grid = findElement(By.id("basic"));

        Assert.assertTrue(hasCell(grid, "Name"));

        Assert.assertTrue(hasCell(grid, "Person 1"));

        scroll(grid, 185);

        waitUntil(driver -> hasCell(grid, "Person 189"));
    }

    @Test
    public void lazyDataIsShown() throws InterruptedException {
        openTabAndCheckForErrors("");
        WebElement grid = findElement(By.id("lazy-loading"));

        scrollToElement(grid);

        Assert.assertTrue(hasCell(grid, "Name"));

        scroll(grid, 1010);

        Assert.assertTrue(hasCell(grid, "Person 1020"));
    }

    @Test
    public void gridAsSingleSelect() {
        openTabAndCheckForErrors("selection");
        WebElement grid = findElement(By.id("single-selection"));
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
        WebElement grid = findElement(By.id("multi-selection"));
        scrollToElement(grid);

        WebElement selectBtn = findElement(By.id("multi-selection-button"));
        WebElement messageDiv = findElement(By.id("multi-selection-message"));

        clickElementWithJs(selectBtn);
        Assert.assertEquals(
                getSelectionMessage(Collections.emptySet(),
                        GridView.items.subList(0, 5), false),
                messageDiv.getText());
        assertRowsSelected(grid, 0, 5);

        List<WebElement> checkboxes = grid
                .findElements(By.tagName("vaadin-checkbox")).stream()
                .filter(element -> "Select Row"
                        .equals(element.getAttribute("aria-label")))
                .collect(Collectors.toList());
        checkboxes.get(0).click();
        checkboxes.get(1).click();
        Assert.assertEquals(
                getSelectionMessage(GridView.items.subList(1, 5),
                        GridView.items.subList(2, 5), true),
                messageDiv.getText());
        assertRowsSelected(grid, 2, 5);

        checkboxes.get(5).click();
        Assert.assertTrue(isRowSelected(grid, 5));
        clickElementWithJs(selectBtn);
        assertRowsSelected(grid, 0, 5);
        Assert.assertFalse(isRowSelected(grid, 5));
    }

    @Test
    public void gridWithDisabledSelection() {
        openTabAndCheckForErrors("selection");
        WebElement grid = findElement(By.id("none-selection"));
        scrollToElement(grid);
        clickElementWithJs(grid
                .findElements(By.tagName("vaadin-grid-cell-content")).get(3));
        Assert.assertFalse(isRowSelected(grid, 1));
    }

    @Test
    public void gridWithColumnTemplate() {
        openTabAndCheckForErrors("using-templates");
        WebElement grid = findElement(By.id("template-renderer"));
        scrollToElement(grid);
        Assert.assertTrue(hasHtmlCell(grid, "0"));
        Assert.assertTrue(hasHtmlCell(grid,
                "<div title=\"Person 1\">Person 1<br><small>23 years old</small></div>"));
        Assert.assertTrue(hasHtmlCell(grid,
                "<div>Street S, number 30<br><small>16142</small></div>"));

        WebElement buttonsCell = getHtmlCell(grid,
                "<button>Update</button><button>Remove</button>");
        List<WebElement> buttons = buttonsCell
                .findElements(By.tagName("button"));
        Assert.assertEquals(2, buttons.size());

        clickElementWithJs(buttons.get(0));
        waitUntil(driver -> hasHtmlCell(grid,
                "<div title=\"Person 1 Updated\">Person 1 Updated<br><small>23 years old</small></div>"));

        clickElementWithJs(buttons.get(0));
        waitUntil(driver -> hasHtmlCell(grid,
                "<div title=\"Person 1 Updated Updated\">Person 1 Updated Updated<br><small>23 years old</small></div>"));

        clickElementWithJs(buttons.get(1));
        waitUntilNot(driver -> hasHtmlCell(grid,
                "<div title=\"Person 1 Updated Updated\">Person 1 Updated Updated<br><small>23 years old</small></div>"));
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
        WebElement grid = findElement(By.id("grid-with-details-row"));
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
        WebElement grid = findElement(By.id("grid-with-details-row-2"));
        scrollToElement(grid);

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));

        List<WebElement> toggleDetailsButtons = grid
                .findElements(By.tagName("button"));

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));
        assertAmountOfOpenDetails(grid, 0);

        clickElementWithJs(toggleDetailsButtons.get(1));
        assertAmountOfOpenDetails(grid, 1);

        Assert.assertTrue(grid.findElement(By.className("custom-details"))
                .getAttribute("innerHTML")
                .contains("Hi! My name is <b>Person 2!</b>"));

        clickElementWithJs(toggleDetailsButtons.get(3));
        assertAmountOfOpenDetails(grid, 2);

        clickElementWithJs(toggleDetailsButtons.get(1));
        Assert.assertFalse(
                "Details should be closed after clicking the button again",
                grid.findElement(By.className("custom-details"))
                        .getAttribute("innerHTML")
                        .contains("Hi! My name is <b>Person 2!</b>"));
    }

    private void assertAmountOfOpenDetails(WebElement grid,
            int expectedAmount) {
        waitUntil(driver -> grid.findElements(By.className("custom-details"))
                .size() == expectedAmount);
        Assert.assertEquals(expectedAmount,
                grid.findElements(By.className("custom-details")).size());
    }

    @Test
    public void groupedColumns() {
        openTabAndCheckForErrors("configuring-columns");
        WebElement grid = findElement(By.id("grid-column-grouping"));
        scrollToElement(grid);

        String columnGroupTag = "vaadin-grid-column-group";
        WebElement topLevelColumn = grid
                .findElement(By.tagName(columnGroupTag));
        List<WebElement> secondLevelColumns = topLevelColumn
                .findElements(By.tagName(columnGroupTag));
        Assert.assertEquals(2, secondLevelColumns.size());
        secondLevelColumns.forEach(columnGroup -> {
            List<WebElement> childColumns = columnGroup
                    .findElements(By.tagName("vaadin-grid-column"));
            Assert.assertEquals(2, childColumns.size());
        });
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
        WebElement grid = findElement(By.id("grid-sortable-columns"));
        scrollToElement(grid);

        WebElement nameColumnSorter = getCell(grid, "Name")
                .findElement(By.tagName("vaadin-grid-sorter"));
        WebElement ageColumnSorter = getCell(grid, "Age")
                .findElement(By.tagName("vaadin-grid-sorter"));
        WebElement addressColumnSorter = getCell(grid, "Address")
                .findElement(By.tagName("vaadin-grid-sorter"));

        clickElementWithJs(nameColumnSorter);
        assertSortMessageEquals(QuerySortOrder.asc("name").build(), true);
        clickElementWithJs(addressColumnSorter);
        assertSortMessageEquals(
                QuerySortOrder.asc("street").thenAsc("number").build(), true);
        clickElementWithJs(addressColumnSorter);
        assertSortMessageEquals(
                QuerySortOrder.desc("street").thenDesc("number").build(), true);
        clickElementWithJs(addressColumnSorter);
        assertSortMessageEquals(Collections.emptyList(), true);

        // enable multi sort
        clickElementWithJs(findElement(By.id("grid-multi-sort-toggle")));
        clickElementWithJs(nameColumnSorter);
        clickElementWithJs(ageColumnSorter);
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
    public void gridWithHeaderWithTemplateRenderer_headerAndFooterAreRenderered() {
        openTabAndCheckForErrors("using-templates");
        WebElement grid = findElement(By.id("grid-header-with-templates"));
        scrollToElement(grid);

        WebElement topLevelColumn = grid
                .findElement(By.tagName("vaadin-grid-column-group"));

        Assert.assertThat(
                "There should be a cell with the renderered 'Basic Information' header",
                topLevelColumn.getAttribute("innerHTML"),
                CoreMatchers.containsString("Basic Information"));

        Assert.assertThat("There should be a cell with the renderered footer",
                topLevelColumn.getAttribute("innerHTML"),
                CoreMatchers.containsString("Total: 499 people"));

        List<WebElement> secondLevelColumns = topLevelColumn
                .findElements(By.tagName("vaadin-grid-column"));

        Assert.assertThat(
                "There should be a cell with the renderered 'Name' header",
                secondLevelColumns.get(0).getAttribute("innerHTML"),
                CoreMatchers.containsString("Name"));

        Assert.assertThat(
                "There should be a cell with the renderered 'Age' header",
                secondLevelColumns.get(1).getAttribute("innerHTML"),
                CoreMatchers.containsString("Age"));
    }

    @Test
    public void gridWithHeaderWithComponentRenderer_headerAndFooterAreRenderered() {
        openTabAndCheckForErrors("using-components");
        WebElement grid = findElement(By.id("grid-header-with-components"));
        scrollToElement(grid);

        Assert.assertTrue(
                "There should be a cell with the renderered 'Basic Information' header",
                hasComponentRendereredHeaderCell(grid,
                        "<label>Basic Information</label>"));

        Assert.assertTrue(
                "There should be a cell with the renderered 'Name' header",
                hasComponentRendereredHeaderCell(grid, "<label>Name</label>"));

        Assert.assertTrue(
                "There should be a cell with the renderered 'Age' header",
                hasComponentRendereredHeaderCell(grid, "<label>Age</label>"));

        Assert.assertTrue("There should be a cell with the renderered footer",
                hasComponentRendereredHeaderCell(grid,
                        "<label>Total: 499 people</label>"));
    }

    @Test
    public void beanGrid_columnsForPropertiesAddedWithCorrectHeaders() {
        openTabAndCheckForErrors("configuring-columns");
        WebElement grid = findElement(By.id("bean-grid"));
        scrollToElement(grid);

        Assert.assertEquals("Unexpected amount of columns", 4,
                grid.findElements(By.tagName("vaadin-grid-column")).size());

        List<WebElement> cells = getCells(grid);

        WebElement firstHeader = getCell(grid, "Address");

        int index = cells.indexOf(firstHeader);
        for (String header : Arrays.asList("Address", "Name", "Age",
                "Postal Code")) {
            Assert.assertEquals("Missing expected column header " + header,
                    header, cells.get(index).getText());
            index++;
        }
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
    public void basicRenderers_rowsAreRenderedAsExpected() {
        openTabAndCheckForErrors("using-renderers");
        WebElement grid = findElement(By.id("grid-basic-renderers"));
        scrollToElement(grid);
        waitUntilCellHasText(grid, "Item 1");

        List<WebElement> cells = grid
                .findElements(By.tagName("vaadin-grid-cell-content"));

        assertCellContent("Item 1", cells.get(0));
        assertCellContent("$ 72.76", cells.get(1));
        assertCellContent("1/10/18 11:19:11 AM", cells.get(2));
        assertCellContent("Jan 25, 2018", cells.get(3));
        assertRendereredContent("$$$", cells.get(4));
        assertCellContent("<button>Remove</button>", cells.get(5));

        assertCellContent("Item 2", cells.get(6));
        assertCellContent("$ 30.87", cells.get(7));
        assertCellContent("1/10/18 11:14:54 AM", cells.get(8));
        assertCellContent("Jan 19, 2018", cells.get(9));
        assertRendereredContent("$", cells.get(10));
        assertCellContent("<button>Remove</button>", cells.get(11));
    }

    private void assertRendereredContent(String expected, WebElement cell) {
        Assert.assertThat(cell.getAttribute("innerHTML"), CoreMatchers.allOf(
                CoreMatchers.startsWith("<flow-grid-component-renderer"),
                CoreMatchers.containsString(expected),
                CoreMatchers.endsWith("</flow-grid-component-renderer>")));
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

    private void assertRowsSelected(WebElement grid, int first, int last) {
        IntStream.range(first, last).forEach(
                rowIndex -> Assert.assertTrue(isRowSelected(grid, rowIndex)));
    }

    private WebElement getRow(WebElement grid, int row) {
        return getInShadowRoot(grid, By.id("items"))
                .findElements(By.cssSelector("tr")).get(row);
    }

    private boolean isRowSelected(WebElement grid, int row) {
        return getRow(grid, row).getAttribute("selected") != null;
    }

    private boolean hasCell(WebElement grid, String text) {
        return getCell(grid, text) != null;
    }

    private WebElement getCell(WebElement grid, String text) {
        return getCells(grid).stream()
                .filter(cell -> text.equals(cell.getText())).findAny()
                .orElse(null);
    }

    private boolean hasHtmlCell(WebElement grid, String html) {
        return getHtmlCell(grid, html) != null;
    }

    private WebElement getHtmlCell(WebElement grid, String text) {
        return getCells(grid).stream()
                .filter(cell -> text.equals(cell.getAttribute("innerHTML")))
                .findAny().orElse(null);
    }

    private boolean hasComponentRendereredCell(WebElement grid, String text) {
        return hasComponentRendereredCell(grid, text,
                "flow-grid-component-renderer");
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
        waitUntil(driver -> {
            List<WebElement> elements = grid
                    .findElements(By.className("custom-details"));
            return elements.size() > rowIndex;
        });
        List<WebElement> elements = grid
                .findElements(By.className("custom-details"));
        WebElement element = elements.get(rowIndex);

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
