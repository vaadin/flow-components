package com.vaadin.flow.component.gridpro.testbench;

/*
 * #%L
 * Vaadin GridPro Testbench API
 * %%
 * Copyright (C) 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * A TestBench element representing a <code>&lt;vaadin-grid-pro&gt;</code> element.
 */
@Element("vaadin-grid-pro")
public class GridProElement extends TestBenchElement {
    /**
     * Scrolls to the row with the given index.
     *
     * @param row
     *            the row to scroll to
     */
    public void scrollToRow(int row) {
        callFunction("_scrollToIndex", row);
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
        return ((Long) executeScript(
                "return arguments[0]._firstVisibleIndex+arguments[0]._vidxOffset",
                this)).intValue();
    }

    /**
     * Gets the total number of rows.
     *
     * @return the number of rows
     */
    public int getRowCount() {
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
        GridProColumnElement column = getVisibleColumns().get(colIndex);
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
    public GridTHTDElement getCell(int rowIndex, GridProColumnElement column) {
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
        return ((Long) executeScript(
                "return arguments[0]._lastVisibleIndex+arguments[0]._vidxOffset",
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
    public List<GridProColumnElement> getAllColumns() {
        generatedColumnIdsIfNeeded();
        String getVisibleColumnsJS = "return arguments[0]._getColumns().sort(function(a,b) { return a._order - b._order;}).map(function(column) { return column.__generatedTbId;});";
        @SuppressWarnings("unchecked")
        List<Long> elements = (List<Long>) executeScript(getVisibleColumnsJS,
                this);
        return elements.stream()
                .map(generatedId -> new GridProColumnElement(generatedId, this))
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
    public List<GridProColumnElement> getVisibleColumns() {
        generatedColumnIdsIfNeeded();
        String getVisibleColumnsJS = "return arguments[0]._getColumns().filter(function(column) {return !column.hidden;}).sort(function(a,b) { return a._order - b._order;}).map(function(column) { return column.__generatedTbId;});";
        List<Long> elements = (List<Long>) executeScript(getVisibleColumnsJS,
                this);
        return elements.stream().map(id -> new GridProColumnElement(id, this))
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
    public GridProColumnElement getColumn(String headerText)
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
        WebElement thead = findInShadowRoot(By.id("header")).get(0);
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
     * @param by
     *            the selector used to find elements
     * @return a list of found elements
     */
    public List<WebElement> findInShadowRoot(By by) {
        return getShadowRoot().findElements(by);
    }

    private WebElement getShadowRoot() {
        waitUntil(driver -> getCommandExecutor()
                .executeScript("return arguments[0].shadowRoot", this) != null);
        WebElement shadowRoot = (WebElement) getCommandExecutor()
                .executeScript("return arguments[0].shadowRoot", this);
        Assert.assertNotNull("Could not locate shadowRoot in the element",
                shadowRoot);
        return shadowRoot;
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
        if (isMultiselect()) {
            getRow(rowIndex).select();
        } else {
            setActiveItem(getRow(rowIndex));
        }
    }

    /**
     * Deselects the row with the given index.
     *
     * @param rowIndex
     *            the row to deselect
     */
    public void deselect(int rowIndex) {
        getRow(rowIndex).deselect();
    }

    private void setActiveItem(GridTRElement row) {
        executeScript("arguments[0].activeItem=arguments[1]._item", this, row);
    }

    /**
     * Checks if the grid is in multi select mode.
     *
     * @return <code>true</code> if the grid is in multi select mode as defined
     *         by the Flow grid, <code>false</code> otherwise
     */
    private boolean isMultiselect() {
        return (boolean) executeScript(
                "return arguments[0]._getColumns().filter(function(col) { return typeof col.selectAll != 'undefined';}).length > 0",
                this);
    }

}
