/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    void scrollToRowByFlatIndex(int rowFlatIndex) {
        waitUntilLoadingFinished();
        callFunction("_scrollToFlatIndex", rowFlatIndex);
        waitUntilLoadingFinished();
    }

    /**
     * Scrolls the grid horizontally to make the column with the given index
     * visible. The index refers to visible columns, in their visual order.
     * 
     * @param columnIndex
     *            the index of the column to scroll to
     */
    public void scrollToColumn(int columnIndex) {
        callFunction("scrollToColumn", columnIndex);
    }

    /**
     * Scrolls the grid horizontally to make the given column visible.
     * 
     * @param column
     *            the column to scroll to
     */
    public void scrollToColumn(GridColumnElement column) {
        if (column == null) {
            return;
        }
        callFunction("scrollToColumn", column);
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
            scrollToRowByFlatIndex(rowIndex);
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
            scrollToRowByFlatIndex(rowIndex);
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
        @SuppressWarnings("unchecked")
        List<TestBenchElement> columns = (List<TestBenchElement>) executeScript(
                """
                        const [grid] = arguments;
                        return grid._getColumns().sort((a, b) => a._order - b._order);
                        """,
                this);
        return columns.stream()
                .map(element -> element.wrap(GridColumnElement.class)).toList();
    }

    /**
     * Gets the currently visible columns in the grid, including any selection
     * checkbox column.
     *
     * @return a list of grid column elements which can be used to refer to the
     *         given column
     */
    public List<GridColumnElement> getVisibleColumns() {
        @SuppressWarnings("unchecked")
        List<TestBenchElement> columns = (List<TestBenchElement>) executeScript(
                """
                        const [grid] = arguments;
                        return grid._getColumns()
                            .filter((column) => !column.hidden)
                            .sort((a, b) => a._order - b._order);
                        """, this);
        return columns.stream()
                .map(element -> element.wrap(GridColumnElement.class)).toList();
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
     * Finds the cell element for the given row and column in header.
     *
     * @param rowIndex
     *            the index of the row in the header
     * @param columnIndex
     *            the index of the column in the header
     * @return the GridTHTDElement for the given row and column in header.
     */
    public GridTHTDElement getHeaderCell(int rowIndex, int columnIndex) {
        WebElement theader = $("*").id("header");
        List<WebElement> headerRows = theader.findElements(By.tagName("tr"));
        List<WebElement> headerCells = headerRows.get(rowIndex)
                .findElements(By.tagName("th"));
        var cell = headerCells.get(columnIndex);
        return wrapElement(cell, getCommandExecutor())
                .wrap(GridTHTDElement.class);
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
     * Finds the vaadin-grid-cell-content element for the given row and column
     * in footer.
     *
     * @param rowIndex
     *            the index of the row in the footer
     * @param columnIndex
     *            the index of the column in the footer
     * @return the vaadin-grid-cell-content element for the given row and column
     *         in footer.
     */
    public TestBenchElement getFooterCellContent(int rowIndex,
            int columnIndex) {
        WebElement tfoot = $("*").id("footer");
        List<WebElement> footerRows = tfoot.findElements(By.tagName("tr"));
        List<WebElement> footerCells = footerRows.get(rowIndex)
                .findElements(By.tagName("td"));
        String slotName = footerCells.get(columnIndex)
                .findElement(By.tagName("slot")).getDomAttribute("name");

        return findElement(By.cssSelector(
                "vaadin-grid-cell-content[slot='" + slotName + "']"));
    }

    /**
     * Finds the cell element for the given row and column in footer.
     *
     * @param rowIndex
     *            the index of the row in the footer
     * @param columnIndex
     *            the index of the column in the footer
     * @return the GridTHTDElement for the given row and column in footer.
     */
    public GridTHTDElement getFooterCell(int rowIndex, int columnIndex) {
        WebElement tfoot = $("*").id("footer");
        List<WebElement> footerRows = tfoot.findElements(By.tagName("tr"));
        List<WebElement> footerCells = footerRows.get(rowIndex)
                .findElements(By.tagName("td"));
        var cell = footerCells.get(columnIndex);
        return wrapElement(cell, getCommandExecutor())
                .wrap(GridTHTDElement.class);
    }

    /**
     * Selects the row with the given index.
     * <p>
     * Automatically scrolls the given row into view.
     *
     * @param rowIndex
     *            the row to select
     */
    public void select(int rowIndex) {
        select(getRow(rowIndex, true));
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
        } else if (!row.isSelected()) {
            activateRow(row);
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
        } else if (row.isSelected()) {
            activateRow(row);
        }
    }

    /**
     * Activates the row by dispatching a {@code row-activate} event, the same
     * event the grid fires when a row is activated with the keyboard. This
     * drives single-selection (and row details) through the connector without a
     * real click, so it doesn't trigger item-click listeners as a side effect.
     *
     * @param row
     *            the row to activate
     */
    private void activateRow(GridTRElement row) {
        executeScript("""
                arguments[0].dispatchEvent(new CustomEvent('row-activate', {
                    detail: { model: { item: arguments[1]._item } }
                }))
                """, this, row);
    }

    /**
     * Get the multi-select column of the grid. Returns null, if the grid is not
     * in multi-selection mode, or doesn't have a multi-selection column.
     *
     * @return the multi-select column, or null
     */
    private GridColumnElement getMultiSelectColumn() {
        TestBenchElement column = (TestBenchElement) executeScript(
                """
                        const [grid] = arguments;
                        return grid._getColumns().find((column) => column.selectAll !== undefined) ?? null;
                        """,
                this);
        if (column == null) {
            return null;
        }
        return column.wrap(GridColumnElement.class);
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
     * JavaScript arrow function that, given a grid row (a {@code tr} element),
     * returns the text content of its visible cells in column order. Hidden
     * columns are skipped. The text extraction matches
     * {@link GridTHTDElement#getText()}: the {@code textContent} of the nodes
     * assigned to the cell's slot are joined, and a value that is only
     * whitespace is returned as an empty string.
     */
    private static final String ROW_CELL_CONTENTS_FUNCTION = """
            (row) => Array.from(row.children)
                .filter((cell) => cell._column && !cell._column.hidden)
                .sort((a, b) => a._column._order - b._column._order)
                .map((cell) => {
                    const slot = cell.firstElementChild;
                    if (!slot || !slot.assignedNodes) {
                        return '';
                    }
                    const text = Array.from(slot.assignedNodes())
                        .map((node) => node.textContent).join('');
                    return text.trim() === '' ? '' : text;
                })
            """;

    /**
     * Gets the text content of the rows currently visible in the viewport as a
     * 2D list, ordered by row index. This is a fast operation requiring only a
     * single browser round-trip.
     * <p>
     * Rows that are rendered but scrolled out of view are not included; use
     * {@link #getAllCellContents()} or {@link #getCellContents(int, int)} to
     * include rows outside the viewport. Only visible columns are included, and
     * the text of each cell matches {@link GridTHTDElement#getText()}.
     *
     * @return a list of rows, each a list holding the text of every visible
     *         column
     */
    public List<List<String>> getVisibleCellContents() {
        waitUntilLoadingFinished();
        // The grid renders a buffer of rows outside the viewport, so filter the
        // rendered rows down to the ones that are at least partially visible.
        String script = "const [grid] = arguments;" + "const rowCellContents = "
                + ROW_CELL_CONTENTS_FUNCTION + ";"
                + "const first = grid._firstVisibleIndex;"
                + "const last = grid._lastVisibleIndex;"
                + "return Array.from(grid._getRenderedRows())"
                + "    .filter((row) => row.index >= first && row.index <= last)"
                + "    .sort((a, b) => a.index - b.index)"
                + "    .map(rowCellContents);";
        @SuppressWarnings("unchecked")
        List<List<String>> result = (List<List<String>>) executeScript(script,
                this);
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Gets the text content of the given row range as a 2D list, ordered by row
     * index. The grid is scrolled as needed to load the requested rows.
     * <p>
     * Only visible columns are included, and the text of each cell matches
     * {@link GridTHTDElement#getText()}.
     *
     * @param fromRow
     *            starting row index (inclusive)
     * @param toRow
     *            ending row index (inclusive)
     * @return a list of rows, each a list holding the text of every visible
     *         column
     * @throws IndexOutOfBoundsException
     *             if the grid is empty, if the row indexes are out of bounds,
     *             or if {@code fromRow} is greater than {@code toRow}
     */
    public List<List<String>> getCellContents(int fromRow, int toRow)
            throws IndexOutOfBoundsException {
        int rowCount = getRowCount();
        if (rowCount == 0) {
            throw new IndexOutOfBoundsException(
                    "Cannot get cell contents: the grid is empty");
        }
        if (fromRow < 0 || toRow < 0 || fromRow >= rowCount || toRow >= rowCount
                || fromRow > toRow) {
            throw new IndexOutOfBoundsException(
                    "fromRow and toRow: expected to be 0.." + (rowCount - 1)
                            + " with fromRow <= toRow, but were " + fromRow
                            + " and " + toRow);
        }
        return collectCellContents(fromRow, toRow);
    }

    /**
     * Scrolls through the given row range and collects the text content of each
     * row. The caller is responsible for validating the range against the row
     * count.
     */
    private List<List<String>> collectCellContents(int fromRow, int toRow) {
        // Only a window of rows is rendered at a time, so scroll through the
        // range and collect each rendered row by its index to avoid duplicates.
        String script = "const [grid, fromRow, toRow] = arguments;"
                + "const rowCellContents = " + ROW_CELL_CONTENTS_FUNCTION + ";"
                + "return Array.from(grid._getRenderedRows())"
                + "    .filter((row) => row.index >= fromRow && row.index <= toRow)"
                + "    .map((row) => [row.index, rowCellContents(row)]);";

        Map<Integer, List<String>> cellsByRow = new HashMap<>();
        int scrollRow = fromRow;
        while (scrollRow <= toRow) {
            scrollToRowByFlatIndex(scrollRow);

            @SuppressWarnings("unchecked")
            List<List<Object>> chunk = (List<List<Object>>) executeScript(
                    script, this, fromRow, toRow);

            int maxIndex = scrollRow;
            for (List<Object> row : chunk) {
                int index = ((Number) row.get(0)).intValue();
                @SuppressWarnings("unchecked")
                List<String> contents = (List<String>) row.get(1);
                cellsByRow.putIfAbsent(index, contents);
                maxIndex = Math.max(maxIndex, index);
            }
            // Advance past the rows just collected. The scroll target is always
            // rendered, so maxIndex >= scrollRow and the loop makes progress.
            scrollRow = maxIndex + 1;
        }

        List<List<String>> result = new ArrayList<>();
        for (int i = fromRow; i <= toRow; i++) {
            result.add(cellsByRow.get(i));
        }
        return result;
    }

    /**
     * Gets the text content of all rows in the grid as a 2D list, ordered by
     * row index. The grid is scrolled through all pages, so this may take a few
     * seconds for large grids, but is much faster than calling
     * {@link GridTHTDElement#getText()} on individual cells.
     * <p>
     * Only visible columns are included, and the text of each cell matches
     * {@link GridTHTDElement#getText()}.
     *
     * @return a list of rows, each a list holding the text of every visible
     *         column
     */
    public List<List<String>> getAllCellContents() {
        int rowCount = getRowCount();
        if (rowCount == 0) {
            return new ArrayList<>();
        }
        return collectCellContents(0, rowCount - 1);
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
