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
package com.vaadin.flow.component.grid.testbench;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

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
                "return arguments[0]._cache.isLoading() || (!!arguments[0].$connector && arguments[0].$connector.hasRootRequestQueue())",
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
     * @return the index of the first visible row
     */
    public int getFirstVisibleRowIndex() {
        return ((Long) executeScript("return arguments[0]._firstVisibleIndex",
                this)).intValue();
    }

    /**
     * Gets the total number of rows.
     *
     * @return the number of rows
     */
    public int getRowCount() {
        waitUntilLoadingFinished();
        return getPropertyDouble("_effectiveSize").intValue();
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
            scrollToRow(rowIndex);
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
     * @return the index of the last visible row
     */
    public int getLastVisibleRowIndex() {
        // Private for now because this seems to be slightly incorrect
        return ((Long) executeScript("return arguments[0]._lastVisibleIndex",
                this)).intValue();
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
        // Private for now because this seems to be slightly incorrect
        return (getFirstVisibleRowIndex() <= rowIndex
                && rowIndex <= getLastVisibleRowIndex());
    }

    /**
     * Gets the <code>tr</code> element for the given row index.
     *
     * @param rowIndex
     *            the row index
     * @return the tr element for the row
     */
    public GridTRElement getRow(int rowIndex) {
        String script = "var grid = arguments[0];"
                + "var rowIndex = arguments[1];"
                + "var rowsInDom = grid.$.items.children;"
                + "var rowInDom = Array.from(rowsInDom).filter(function(row) { return !row.hidden && row.index == rowIndex;})[0];"
                + "return rowInDom;";
        return ((TestBenchElement) executeScript(script, this, rowIndex))
                .wrap(GridTRElement.class);
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
                .findElement(By.tagName("slot")).getAttribute("name");

        return findElement(By.cssSelector(
                "vaadin-grid-cell-content[slot='" + slotName + "']"));
    }

    /**
     * Find all {@link WebElement}s using the given {@link By} selector.
     *
     * @deprecated this method will not working for Chrome 96+, because of the
     *             breaking changes in ChromeDriver.
     * @param by
     *            the selector used to find elements
     * @return a list of found elements
     */
    @Deprecated
    public List<WebElement> findInShadowRoot(By by) {
        return getShadowRoot().findElements(by);
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
                checkbox.click();
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
                checkbox.click();
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
        List<Long> columnIds = (List<Long>) executeScript(
                "return arguments[0]._getColumns().filter(function(col) { return typeof col.selectAll != 'undefined';}).map(function(column) { return column.__generatedTbId;});",
                this);
        if (columnIds.isEmpty())
            return null;
        return new GridColumnElement(columnIds.get(0), this);
    }
}
