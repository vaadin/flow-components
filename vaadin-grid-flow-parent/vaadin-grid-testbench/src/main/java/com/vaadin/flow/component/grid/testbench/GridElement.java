/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.testbench;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-grid&gt;</code> element.
 */
@Element("vaadin-grid")
public class GridElement extends TestBenchElement {

    protected void waitUntilLoadingFinished() {
        waitUntil(e -> !isLoading());
    }

    protected boolean isLoading() {
        return (Boolean) executeScript(
                "return arguments[0]._dataProviderController.isLoading() || (!!arguments[0].$connector && arguments[0].$connector.hasRootRequestQueue())",
                this);
    }

    /**
     * Scrolls to the row with the given index.
     *
     * @param row
     *            the row to scroll to
     */
    public void scrollToRow(int row) {
        callFunction("scrollToIndex", row);
        waitUntilLoadingFinished();
    }

    /**
     * Scrolls to the row with the given flat row index.
     *
     * @param row
     *            the row to scroll to
     */
    protected void scrollToFlatRow(int row) {
        callFunction("_scrollToFlatIndex", row);
        waitUntilLoadingFinished();
    }

    /**
     * Gets the page size used when fetching data.
     *
     * @return the page size
     */
    public int getPageSize() {
        return getPropertyInteger("pageSize");
    }

    /**
     * Gets the index of the first row which is at least partially visible.
     *
     * @return the index of the first visible row, -1 if Grid is empty
     */
    public int getFirstVisibleRowIndex() {
        Object index = executeScript("return arguments[0]._firstVisibleIndex",
                this);
        if (index != null) {
            return ((Long) index).intValue();
        } else {
            return -1;
        }
    }

    /**
     * Gets the total number of rows.
     *
     * @return the number of rows
     */
    public int getRowCount() {
        waitUntilLoadingFinished();
        return getPropertyDouble("_flatSize").intValue();
    }

    /**
     * Gets the grid cell for the given row and column index.
     * <p>
     * For the column index, only visible columns are taken into account.
     * <p>
     * Automatically scrolls the given row into view
     *
     * @param rowIndex
     *            the row index
     * @param colIndex
     *            the column index
     * @return the grid cell for the given coordinates
     */
    public GridTHTDElement getCell(int rowIndex, int colIndex) {
        GridColumnElement column = getVisibleColumns().get(colIndex);
        return getCell(rowIndex, column);
    }

    /**
     * Gets the grid cell for the given row and column.
     * <p>
     * Automatically scrolls the given row into view
     *
     * @param rowIndex
     *            the row index
     * @param column
     *            the column element for the column
     * @return the grid cell for the given coordinates
     */
    public GridTHTDElement getCell(int rowIndex, GridColumnElement column) {
        if (!isRowInView(rowIndex)) {
            scrollToFlatRow(rowIndex);
        }

        GridTRElement row = getRow(rowIndex);
        return row.getCell(column);
    }

    /**
     * Finds the first cell inside the rendered range with a text content
     * matching the given string.
     *
     * @param contents
     *            the string to look for
     * @return a grid cell containing the given string
     * @throws NoSuchElementException
     *             if no cell with the given string was found
     */
    public GridTHTDElement getCell(String contents)
            throws NoSuchElementException {

        String script = "const grid = arguments[0];"
                + "const contents = arguments[1];"
                + "const rowsInDom = Array.from(arguments[0].$.items.children);"
                + "var tds = [];"
                + "rowsInDom.forEach(function(tr) { Array.from(tr.children).forEach(function(td) { tds.push(td);})});"
                + "const matches = tds.filter(function(td) { return td._content.textContent == contents});"
                + "return matches.length ? matches[0] : null;";
        TestBenchElement td = (TestBenchElement) executeScript(script, this,
                contents);
        if (td == null) {
            throw new NoSuchElementException(
                    "No cell with text content '" + contents + "' found");
        }

        return td.wrap(GridTHTDElement.class);
    }

    /**
     * Gets the index of the last row which is at least partially visible.
     *
     * @return the index of the last visible row, -1 if Grid is empty
     */
    public int getLastVisibleRowIndex() {
        Object index = executeScript("return arguments[0]._lastVisibleIndex",
                this);
        if (index != null) {
            return ((Long) index).intValue();
        } else {
            return -1;
        }
    }

    /**
     * Checks if the given row is in the visible viewport.
     *
     * @param rowIndex
     *            the row to check
     * @return <code>true</code> if the row is at least partially in view,
     *         <code>false</code> otherwise
     */
    private boolean isRowInView(int rowIndex) {
        return (getFirstVisibleRowIndex() <= rowIndex
                && rowIndex <= getLastVisibleRowIndex());
    }

    /**
     * Gets the rows (present in the DOM) specified by the lower and upper row
     * indexes.
     *
     * @param firstRowIndex
     *            the lower row index to be retrieved (inclusive)
     * @param lastRowIndex
     *            the upper row index to be retrieved (inclusive)
     * @return a {@link GridTRElement} list with the rows contained between the
     *         given coordinates.
     * @throws IndexOutOfBoundsException
     *             if either of the provided row indexes do not exist
     */
    public List<GridTRElement> getRows(int firstRowIndex, int lastRowIndex)
            throws IndexOutOfBoundsException {
        int rowCount = getRowCount();
        if (firstRowIndex < 0 || lastRowIndex < 0 || firstRowIndex >= rowCount
                || lastRowIndex >= rowCount) {
            throw new IndexOutOfBoundsException(
                    "firstRowIndex and lastRowIndex: expected to be 0.."
                            + (rowCount - 1) + " but were " + firstRowIndex
                            + " and " + lastRowIndex);
        }
        String script = "var grid = arguments[0];"
                + "var firstRowIndex = arguments[1];"
                + "var lastRowIndex = arguments[2];"
                + "var rowsInDom = grid._getRenderedRows();"
                + "return Array.from(rowsInDom).filter((row) => { return row.index >= firstRowIndex && row.index <= lastRowIndex;});";
        Object rows = executeScript(script, this, firstRowIndex, lastRowIndex);
        if (rows != null) {
            return ((ArrayList<?>) rows).stream().map(
                    elem -> ((TestBenchElement) elem).wrap(GridTRElement.class))
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the {@code tr} element for the given row index.
     *
     * @param rowIndex
     *            the row index
     * @return the {@code tr} element for the row, or {@code null} if the row is
     *         not in viewport
     * @throws IndexOutOfBoundsException
     *             if no row with given index exists
     */
    public GridTRElement getRow(int rowIndex) throws IndexOutOfBoundsException {
        return getRow(rowIndex, false);
    }

    /**
     * Gets the {@code tr} element for the given row index.
     * <p>
     * Returns {@code null} if the row is not in viewport and the provided
     * {@code scroll} parameter is {@code false}.
     *
     * @param rowIndex
     *            the row index
     * @param scroll
     *            whether to scroll to the row index
     * @return the {@code tr} element for the row, or {@code null} if the row is
     *         not in viewport and the provided {@code scroll} parameter is
     *         {@code false}
     * @throws IndexOutOfBoundsException
     *             if no row with given index exists
     */
    public GridTRElement getRow(int rowIndex, boolean scroll)
            throws IndexOutOfBoundsException {
        if (scroll && !isRowInView(rowIndex)) {
            scrollToFlatRow(rowIndex);
        }
        var rows = getRows(rowIndex, rowIndex);
        return rows.size() == 1 ? rows.get(0) : null;
    }

    /**
     * Gets all columns defined for the grid, including any selection checkbox
     * column.
     *
     * @return a list of grid column elements which can be used to refer to the
     *         given column
     */
    public List<GridColumnElement> getAllColumns() {
        generatedColumnIdsIfNeeded();
        String getVisibleColumnsJS = "return arguments[0]._getColumns().sort(function(a,b) { return a._order - b._order;}).map(function(column) { return column.__generatedTbId;});";
        @SuppressWarnings("unchecked")
        List<Long> elements = (List<Long>) executeScript(getVisibleColumnsJS,
                this);
        return elements.stream()
                .map(generatedId -> new GridColumnElement(generatedId, this))
                .collect(Collectors.toList());
    }

    protected void generatedColumnIdsIfNeeded() {
        String generateIds = "const grid = arguments[0];"
                + "if (!grid.__generatedTbId) {"//
                + "  grid.__generatedTbId = 1;"//
                + "}" //
                + "grid._getColumns().forEach(function(column) {"
                + "  if (!column.__generatedTbId) {"
                + "    column.__generatedTbId = grid.__generatedTbId++;" //
                + "  }" //
                + "});";

        executeScript(generateIds, this);
        //
    }

    /**
     * Gets the currently visible columns in the grid, including any selection
     * checkbox column.
     *
     * @return a list of grid column elements which can be used to refer to the
     *         given column
     */
    public List<GridColumnElement> getVisibleColumns() {
        generatedColumnIdsIfNeeded();
        String getVisibleColumnsJS = "return arguments[0]._getColumns().filter(function(column) {return !column.hidden;}).sort(function(a,b) { return a._order - b._order;}).map(function(column) { return column.__generatedTbId;});";
        List<Long> elements = (List<Long>) executeScript(getVisibleColumnsJS,
                this);
        return elements.stream().map(id -> new GridColumnElement(id, this))
                .collect(Collectors.toList());

    }

    /**
     * Gets the column with the given header text.
     * <p>
     * If multiple columns are found with the same header text, returns the
     * first column.
     *
     * @param headerText
     *            the text in the header
     * @return the grid column element for the given column
     * @throws NoSuchElementException
     *             if no column was found
     */
    public GridColumnElement getColumn(String headerText)
            throws NoSuchElementException {
        return getVisibleColumns().stream().filter(
                column -> headerText.equals(column.getHeaderCell().getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No column with header '" + headerText
                                + "' was found"));
    }

    /**
     * Gets the header cell for the given visible column index.
     *
     * @param columnIndex
     *            the index of the column
     * @return a cell element for the header cell
     */
    public GridTHTDElement getHeaderCell(int columnIndex) {
        return getVisibleColumns().get(columnIndex).getHeaderCell();
    }

    /**
     * Finds the vaadin-grid-cell-content element for the given row and column
     * in header.
     *
     * @param rowIndex
     *            the index of the row in the header
     * @param columnIndex
     *            the index of the column in the header
     * @return the vaadin-grid-cell-content element for the given row and column
     *         in header.
     */
    public TestBenchElement getHeaderCellContent(int rowIndex,
            int columnIndex) {
        WebElement thead = $("*").id("header");
        List<WebElement> headerRows = thead.findElements(By.tagName("tr"));
        List<WebElement> headerCells = headerRows.get(rowIndex)
                .findElements(By.tagName("th"));
        String slotName = headerCells.get(columnIndex)
                .findElement(By.tagName("slot")).getDomAttribute("name");

        return findElement(By.cssSelector(
                "vaadin-grid-cell-content[slot='" + slotName + "']"));
    }

    /**
     * Gets the footer cell for the given visible column index.
     *
     * @param columnIndex
     *            the index of the column
     * @return a cell element for the footer cell
     */
    public GridTHTDElement getFooterCell(int columnIndex) {
        return getVisibleColumns().get(columnIndex).getFooterCell();
    }

    /**
     * Selects the row with the given index.
     *
     * @param rowIndex
     *            the row to select
     */
    public void select(int rowIndex) {
        select(getRow(rowIndex));
    }

    /**
     * Selects the row.
     *
     * @param row
     *            the row to select
     */
    void select(GridTRElement row) {
        GridColumnElement multiSelectColumn = getMultiSelectColumn();
        if (multiSelectColumn != null) {
            GridTHTDElement cell = row.getCell(multiSelectColumn);
            CheckboxElement checkbox = wrapElement(cell.getFirstChildElement(),
                    getCommandExecutor()).wrap(CheckboxElement.class);
            if (!checkbox.isChecked()) {
                checkbox.getWrappedElement().click();
            }
        } else {
            setActiveItem(row);
        }
    }

    /**
     * Deselects the row with the given index.
     *
     * @param rowIndex
     *            the row to deselect
     */
    public void deselect(int rowIndex) {
        deselect(getRow(rowIndex));
    }

    /**
     * Deselects the row with the given index.
     *
     * @param row
     *            the row to deselect
     */
    void deselect(GridTRElement row) {
        GridColumnElement multiSelectColumn = getMultiSelectColumn();
        if (multiSelectColumn != null) {
            GridTHTDElement cell = row.getCell(multiSelectColumn);
            CheckboxElement checkbox = wrapElement(cell.getFirstChildElement(),
                    getCommandExecutor()).wrap(CheckboxElement.class);
            if (checkbox.isChecked()) {
                checkbox.getWrappedElement().click();
            }
        } else {
            removeActiveItem(row);
        }
    }

    private void setActiveItem(GridTRElement row) {
        executeScript("arguments[0].activeItem=arguments[1]._item", this, row);
    }

    private void removeActiveItem(GridTRElement row) {
        final String JS_DEACTIVATE_IF_ACTIVE = "if(arguments[0]._itemsEqual(arguments[0].activeItem, "
                + "arguments[1]._item)) { arguments[0].activeItem=null;}";
        executeScript(JS_DEACTIVATE_IF_ACTIVE, this, row);
    }

    /**
     * Get the multi-select column of the grid. Returns null, if the grid is not
     * in multi-selection mode, or doesn't have a multi-selection column.
     *
     * @return the multi-select column, or null
     */
    private GridColumnElement getMultiSelectColumn() {
        generatedColumnIdsIfNeeded();
        List<Long> columnIds = (List<Long>) executeScript(
                "return arguments[0]._getColumns().filter(function(col) { return typeof col.selectAll != 'undefined';}).map(function(column) { return column.__generatedTbId;});",
                this);
        if (columnIds.isEmpty())
            return null;
        return new GridColumnElement(columnIds.get(0), this);
    }

    /**
     * Click select all check box
     */
    public void clickSelectAll() {
        CheckboxElement selectAllCheckbox = $(CheckboxElement.class)
                .id("selectAllCheckbox");
        selectAllCheckbox.click();
    }

    /**
     * Gets all the currently visible rows.
     *
     * @return a {@link GridTRElement} list representing the currently visible
     *         rows.
     */
    public List<GridTRElement> getVisibleRows() {
        return getRows(getFirstVisibleRowIndex(), getLastVisibleRowIndex());
    }

    /**
     * Gets the grid cells for the given row and column elements.
     *
     * @param rowIndex
     *            the row index
     * @param columnElements
     *            the column elements
     * @return a {@link GridTHTDElement} list with the cells for the given
     *         coordinates.
     */
    public List<GridTHTDElement> getCells(int rowIndex,
            GridColumnElement... columnElements) {
        GridTRElement row = getRow(rowIndex);
        return row != null ? row.getCells(columnElements) : new ArrayList<>();
    }

    /**
     * Gets the grid cells for the given row.
     *
     * @param rowIndex
     *            the row index
     * @return a {@link GridTHTDElement} list with the cells for the given
     *         coordinates.
     */
    public List<GridTHTDElement> getCells(int rowIndex) {
        return getCells(rowIndex,
                getAllColumns().toArray(new GridColumnElement[0]));
    }

    /**
     * Gets the empty state content.
     *
     * @return the empty state content
     * @throws NoSuchElementException
     *             if no empty state content was found
     */
    public TestBenchElement getEmptyStateContent() {
        try {
            return findElement(By.cssSelector("[slot='empty-state']"));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(
                    "No empty state content was found");
        }
    }
}
