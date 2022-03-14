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
import java.util.logging.Level;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/selection")
public class GridViewSelectionIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void gridAsSingleSelect() {
        GridElement grid = $(GridElement.class).id("single-selection");
        scrollToElement(grid);

        WebElement toggleButton = findElement(By.id("single-selection-toggle"));
        WebElement messageDiv = findElement(By.id("single-selection-message"));

        clickElementWithJs(toggleButton);
        Assert.assertEquals(
                getSelectionMessage(null, LegacyTestView.items.get(0), false),
                messageDiv.getText());
        Assert.assertTrue("Person 1 was not marked as selected",
                isRowSelected(grid, 0));
        clickElementWithJs(toggleButton);
        Assert.assertEquals(
                getSelectionMessage(LegacyTestView.items.get(0), null, false),
                messageDiv.getText());
        Assert.assertFalse("Person 1 was marked as selected",
                isRowSelected(grid, 0));

        // should be the cell in the first column's second row
        clickElementWithJs(getCell(grid, "Person 2"));
        Assert.assertTrue("Person 2 was not marked as selected",
                isRowSelected(grid, 1));
        Assert.assertEquals(
                getSelectionMessage(null, LegacyTestView.items.get(1), true),
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
        Assert.assertEquals(
                getSelectionMessage(LegacyTestView.items.get(1),
                        LegacyTestView.items.get(0), false),
                messageDiv.getText());
        clickElementWithJs(toggleButton);
        Assert.assertFalse("Person 1 was marked as selected",
                isRowSelected(grid, 0));

        // scroll to bottom
        scroll(grid, 1000);
        waitUntilCellHasText(grid, "Person 499");
        // select item that is not in cache
        clickElementWithJs(toggleButton);
        Assert.assertEquals(
                getSelectionMessage(null, LegacyTestView.items.get(0), false),
                messageDiv.getText());
        // scroll back up
        scroll(grid, 0);
        Assert.assertTrue("Person 1 was not marked as selected",
                isRowSelected(grid, 0));

        Assert.assertFalse(
                getLogEntries(Level.SEVERE).stream().findAny().isPresent());
    }

    @Test
    public void gridAsSingleSelectTestBenchAPI() {
        GridElement grid = $(GridElement.class).id("single-selection");
        grid.scrollIntoView();
        GridTHTDElement person2cell = grid.getCell("Person 2");
        GridTRElement person2row = person2cell.getRowElement();

        WebElement toggleButton = $(TestBenchElement.class)
                .id("single-selection-toggle");
        WebElement messageDiv = $(TestBenchElement.class)
                .id("single-selection-message");

        toggleButton.click();
        Assert.assertEquals(
                getSelectionMessage(null, LegacyTestView.items.get(0), false),
                messageDiv.getText());
        Assert.assertTrue("Person 1 was not marked as selected",
                isRowSelected(grid, 0));
        toggleButton.click();
        Assert.assertEquals(
                getSelectionMessage(LegacyTestView.items.get(0), null, false),
                messageDiv.getText());
        Assert.assertFalse("Person 1 was marked as selected",
                isRowSelected(grid, 0));

        person2row.select();
        Assert.assertTrue("Person 2 was not marked as selected",
                isRowSelected(grid, 1));
        Assert.assertEquals(
                getSelectionMessage(null, LegacyTestView.items.get(1), true),
                messageDiv.getText());

        // deselect non-selected row
        grid.getCell("Person 3").getRowElement().deselect(); // NO-OP
        Assert.assertTrue("Person 2 was not marked as selected",
                isRowSelected(grid, 1));
        Assert.assertEquals(
                getSelectionMessage(null, LegacyTestView.items.get(1), true),
                messageDiv.getText());

        person2row.deselect();
        Assert.assertFalse("Person 2 was marked as selected",
                isRowSelected(grid, 1));

        person2row.select();
        toggleButton.click();
        Assert.assertTrue("Person 1 was not marked as selected",
                isRowSelected(grid, 0));
        Assert.assertFalse("Person 2 was marked as selected",
                isRowSelected(grid, 1));
        Assert.assertEquals(
                getSelectionMessage(LegacyTestView.items.get(1),
                        LegacyTestView.items.get(0), false),
                messageDiv.getText());
        toggleButton.click();
        Assert.assertFalse("Person 1 was marked as selected",
                isRowSelected(grid, 0));

        // scroll to bottom
        scroll(grid, 1000);
        waitUntilCellHasText(grid, "Person 499");

        // select item that is not in cache
        toggleButton.click();
        Assert.assertEquals(
                getSelectionMessage(null, LegacyTestView.items.get(0), false),
                messageDiv.getText());

        // scroll back up
        scroll(grid, 0);
        Assert.assertTrue("Person 1 was not marked as selected",
                isRowSelected(grid, 0));

        Assert.assertFalse(
                getLogEntries(Level.SEVERE).stream().findAny().isPresent());
    }

    @Test
    public void gridAsMultiSelect() {
        GridElement grid = $(GridElement.class).id("multi-selection");
        scrollToElement(grid);

        WebElement selectBtn = findElement(By.id("multi-selection-button"));
        WebElement messageDiv = findElement(By.id("multi-selection-message"));

        clickElementWithJs(selectBtn);
        Assert.assertEquals(
                getSelectionMessage(LegacyTestView.items.subList(0, 2),
                        LegacyTestView.items.subList(0, 5), false),
                messageDiv.getText());
        assertRowsSelected(grid, 0, 5);

        WebElement checkbox = getCellContent(grid.getCell(0, 0));
        checkbox.click();
        checkbox = getCellContent(grid.getCell(1, 0));
        checkbox.click();
        Assert.assertEquals(
                getSelectionMessage(LegacyTestView.items.subList(1, 5),
                        LegacyTestView.items.subList(2, 5), true),
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
        Assert.assertEquals("Select all should have been deselected", false,
                grid.findElement(By.id("selectAllCheckbox"))
                        .getPropertyBoolean("checked"));

        getCellContent(grid.getCell(0, 0)).click();
        Assert.assertEquals("Select all should have been reselected", true,
                grid.findElement(By.id("selectAllCheckbox"))
                        .getPropertyBoolean("checked"));

    }

    /**
     * Test that aria-multiselectable and aria-selected should NOT be present
     * when SelectionMode is set to NONE.
     */
    @Test
    public void gridAriaSelectionAttributesWhenSelectionModeIsNone() {
        GridElement grid = $(GridElement.class).id("none-selection");
        scrollToElement(grid);
        TestBenchElement table = grid.$("table").first();
        // table should not have aria-multiselectable attribute
        Assert.assertFalse(table.hasAttribute("aria-multiselectable"));

        // the aria-selected attribute must have been removed from the row
        for (int i = grid.getFirstVisibleRowIndex(); i < grid
                .getLastVisibleRowIndex(); i++) {
            GridTRElement row = grid.getRow(i);
            Assert.assertFalse(row.hasAttribute("aria-selected"));
            // make sure the attribute was removed from all cells in the row as
            // well
            Assert.assertFalse(row.$("td").all().stream()
                    .anyMatch(cell -> cell.hasAttribute("aria-selected")));
        }
    }

    /**
     * Test that aria-multiselectable=false & the selectable children should
     * have aria-selected=true|false depending on their state
     */
    @Test
    public void gridAriaSelectionAttributesWhenSelectionModeIsSingle() {
        GridElement grid = $(GridElement.class).id("single-selection");
        scrollToElement(grid);
        GridTRElement firstRow = grid.getRow(0);
        firstRow.select();
        TestBenchElement table = grid.$("table").first();
        // table should have aria-multiselectable
        Assert.assertTrue(table.hasAttribute("aria-multiselectable"));
        // aria-multiselectable should be set to false
        Assert.assertFalse(Boolean
                .parseBoolean(table.getAttribute("aria-multiselectable")));

        Assert.assertTrue(firstRow.hasAttribute("aria-selected"));
        Assert.assertTrue(
                Boolean.parseBoolean(firstRow.getAttribute("aria-selected")));
        Assert.assertFalse(Boolean
                .parseBoolean(grid.getRow(1).getAttribute("aria-selected")));
    }

    /**
     * Test that aria-multiselectable=true & the selectable children should have
     * aria-selected=true|false depending on their state
     */
    @Test
    public void gridAriaSelectionAttributesWhenSelectionModeIsMulti() {
        GridElement grid = $(GridElement.class).id("multi-selection");
        scrollToElement(grid);
        TestBenchElement table = grid.$("table").first();
        // table should have aria-multiselectable set to true
        Assert.assertTrue(Boolean
                .parseBoolean(table.getAttribute("aria-multiselectable")));

        Assert.assertTrue(Boolean
                .parseBoolean(grid.getRow(0).getAttribute("aria-selected")));
        Assert.assertTrue(Boolean
                .parseBoolean(grid.getRow(1).getAttribute("aria-selected")));
        Assert.assertFalse(Boolean
                .parseBoolean(grid.getRow(2).getAttribute("aria-selected")));

        grid.select(2);
        Assert.assertTrue(Boolean
                .parseBoolean(grid.getRow(2).getAttribute("aria-selected")));
    }

    @Test
    public void gridWithDisabledSelection() {
        GridElement grid = $(GridElement.class).id("none-selection");
        scrollToElement(grid);
        clickElementWithJs(grid
                .findElements(By.tagName("vaadin-grid-cell-content")).get(3));
        Assert.assertFalse(isRowSelected(grid, 1));
    }

    private static String getSelectionMessage(Object oldSelection,
            Object newSelection, boolean isFromClient) {
        return String.format(
                "Selection changed from %s to %s, selection is from client: %s",
                oldSelection, newSelection, isFromClient);
    }

    private void scroll(GridElement grid, int index) {
        grid.scrollToRow(index);
    }

    private boolean isRowSelected(GridElement grid, int row) {
        return grid.getRow(row).isSelected();
    }

    private void assertRowsSelected(GridElement grid, int first, int last) {
        IntStream.range(first, last).forEach(
                rowIndex -> Assert.assertTrue(isRowSelected(grid, rowIndex)));
    }

    private WebElement getCell(GridElement grid, String text) {
        return grid.getCell(text);
    }

    private WebElement getCellContent(GridTHTDElement cell) {
        return (WebElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0].firstElementChild;",
                cell);
    }

    private void waitUntilCellHasText(WebElement grid, String text) {
        waitUntil(driver -> {
            List<?> cellContentTexts = (List<?>) getCommandExecutor()
                    .executeScript(
                            "return Array.from(arguments[0].querySelectorAll('vaadin-grid-cell-content')).map(cell => cell.textContent)",
                            grid);
            return cellContentTexts.contains(text);
        });
    }
}
