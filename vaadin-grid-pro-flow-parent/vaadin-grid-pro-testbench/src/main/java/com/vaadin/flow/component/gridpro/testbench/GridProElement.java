/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.testbench;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-grid-pro&gt;</code>
 * element.
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
        callFunction("scrollToIndex", row);
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
        WebElement thead = $("thead").id("header");
        List<WebElement> headerRows = thead.findElements(By.tagName("tr"));
        List<WebElement> headerCells = headerRows.get(rowIndex)
                .findElements(By.tagName("th"));
        String slotName = headerCells.get(columnIndex)
                .findElement(By.tagName("slot")).getAttribute("name");

        return findElement(By.cssSelector(
                "vaadin-grid-cell-content[slot='" + slotName + "']"));
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
}
