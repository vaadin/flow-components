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

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * TestBench Element API for TreeGrid.
 *
 */
public class TreeGridElement extends GridElement {

    /**
     * Scrolls to the row with the given index.
     *
     * @param row
     *            the row to scroll to
     */
    public void scrollToRowAndWait(int row) {
        waitUntilLoadingFinished();
        scrollToRow(row);
        waitUntilLoadingFinished();
    }

    /**
     * Scrolls to the row with the given indexes. The indexes are hierarchical,
     * starting with the root index.
     *
     * @param indexes
     *            the indexes of the row to scroll to
     */
    public void scrollToRowAndWait(int... indexes) {
        waitUntilLoadingFinished();
        callFunction("scrollToIndex", indexes);
        waitUntilLoadingFinished();
    }

    /**
     * Scrolls to the row with the given flat index.
     *
     * @param row
     *            the row to scroll to
     */
    public void scrollToFlatRowAndWait(int row) {
        waitUntilLoadingFinished();
        scrollToFlatRow(row);
    }

    /**
     * Gets the grid cell for the given row and column index.
     * <p>
     * For the column index, only visible columns are taken into account.
     * <p>
     * Automatically scrolls the given row into view and waits for the row to
     * load.
     *
     * @param rowIndex
     *            the row index
     * @param colIndex
     *            the column index
     * @return the grid cell for the given coordinates
     */
    public GridTHTDElement getCellWaitForRow(int rowIndex, int colIndex) {
        GridColumnElement column = getVisibleColumns().get(colIndex);
        return getCellWaitForRow(rowIndex, column);
    }

    /**
     * Gets the grid cell for the given row and column.
     * <p>
     * Automatically scrolls the given row into view and waits for the row to
     * load.
     *
     * @param rowIndex
     *            the row index
     * @param column
     *            the column element for the column
     * @return the grid cell for the given coordinates
     */
    public GridTHTDElement getCellWaitForRow(int rowIndex,
            GridColumnElement column) {
        if (!((getFirstVisibleRowIndex() <= rowIndex
                && rowIndex <= getLastVisibleRowIndex()))) {
            scrollToFlatRowAndWait(rowIndex);
        }
        waitUntil(test -> !isLoadingExpandedRows());

        GridTRElement row = getRow(rowIndex);
        return row.getCell(column);
    }

    /**
     * Expands the row at the given index in the grid. This expects the first
     * column to have the hierarchy data.
     *
     * @param rowIndex
     *            0-based row index to expand
     * @see #expandWithClick(int, int)
     */
    public void expandWithClick(int rowIndex) {
        expandWithClick(rowIndex, 0);
    }

    /**
     * Expands the row at the given index in the grid with the given
     * hierarchical column index.
     *
     * @param rowIndex
     *            0-based row index to expand
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     */
    public void expandWithClick(int rowIndex, int hierarchyColumnIndex) {
        if (isRowExpanded(rowIndex, hierarchyColumnIndex)) {
            throw new IllegalStateException(
                    "The element at row " + rowIndex + " was expanded already");
        }
        getExpandToggleElement(rowIndex, hierarchyColumnIndex).click();
        waitUntilLoadingFinished();
    }

    /**
     * Collapses the row at the given index in the grid. This expects the first
     * column to have the hierarchy data.
     *
     * @param rowIndex
     *            0-based row index to collapse
     * @see #collapseWithClick(int, int)
     */
    public void collapseWithClick(int rowIndex) {
        collapseWithClick(rowIndex, 0);
    }

    /**
     * Collapses the row at the given index in the grid with the given
     * hierarchical column index.
     *
     * @param rowIndex
     *            0-based row index to collapse
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     */
    public void collapseWithClick(int rowIndex, int hierarchyColumnIndex) {
        if (isRowCollapsed(rowIndex, hierarchyColumnIndex)) {
            throw new IllegalStateException("The element at row " + rowIndex
                    + " was collapsed already");
        }
        getExpandToggleElement(rowIndex, hierarchyColumnIndex).click();
        waitUntilLoadingFinished();
    }

    /**
     * Returns whether the row at the given index is expanded or not.
     *
     * @param rowIndex
     *            0-based row index
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     * @return {@code true} if expanded, {@code false} if collapsed
     */
    public boolean isRowExpanded(int rowIndex, int hierarchyColumnIndex) {
        waitUntilLoadingFinished();
        WebElement expandElement = getExpandToggleElement(rowIndex,
                hierarchyColumnIndex);
        return expandElement != null
                && !"false".equals(expandElement.getDomProperty("expanded"));
    }

    /**
     * Returns whether the row at the given index is collapsed or not.
     *
     * @param rowIndex
     *            0-based row index
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     * @return {@code true} if collapsed, {@code false} if expanded
     */
    public boolean isRowCollapsed(int rowIndex, int hierarchyColumnIndex) {
        return !isRowExpanded(rowIndex, hierarchyColumnIndex);
    }

    /**
     * Check whether the given indices correspond to a cell that contains a
     * visible hierarchy toggle element.
     *
     * @param rowIndex
     *            0-based row index
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     * @return {@code true} if this cell has the expand toggle visible
     */
    public boolean hasExpandToggle(int rowIndex, int hierarchyColumnIndex) {
        try {
            WebElement expandElement = getExpandToggleElement(rowIndex,
                    hierarchyColumnIndex);
            return expandElement != null && expandElement.isDisplayed()
                    && "false".equals(expandElement.getDomProperty("leaf"));
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Gets the 'vaadin-grid-tree-toggle' element for the given row.
     *
     * @param rowIndex
     *            0-based row index
     * @param hierarchyColumnIndex
     *            0-based index of the hierarchy column
     * @return the {@code span} element that is clicked for expanding/collapsing
     *         a rows
     * @throws NoSuchElementException
     *             if there is no expand element for this row
     */
    public WebElement getExpandToggleElement(int rowIndex,
            int hierarchyColumnIndex) {
        GridTHTDElement cell = getCell(rowIndex, hierarchyColumnIndex);
        return cell == null ? null : cell.$("vaadin-grid-tree-toggle").first();
    }

    /**
     * Returns a number of expanded rows in the grid element. Notice that
     * returned number does not mean that grid has yet finished rendering all
     * visible expanded rows.
     *
     * @return the number of expanded rows
     */
    public long getNumberOfExpandedRows() {
        waitUntilLoadingFinished();
        return (long) executeScript("return arguments[0].expandedItems.length;",
                this);
    }

    /**
     * Returns {@code true} if details are open or the given row index.
     *
     * @param rowIndex
     *            the 0-based row index
     * @return {@code true} if details are shown in the target row
     */
    public boolean isDetailsOpen(int rowIndex) {
        try {
            return getRow(rowIndex).getDetails().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Returns true if given index has tr element for the row
     *
     * @param row
     *            the row index
     * @return <code>true</code> if there is tr element for the row,
     *         <code>false</code> otherwise
     */
    public boolean hasRow(int row) {
        try {
            return getRow(row) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if grid is loading expanded rows.
     *
     * @return <code>true</code> if grid is loading expanded rows,
     *         <code>false</code> otherwise
     */
    public boolean isLoadingExpandedRows() {
        return (Boolean) executeScript(
                "return !!arguments[0].$connector ? (arguments[0].$connector.hasEnsureSubCacheQueue() || arguments[0].$connector.hasParentRequestQueue()) : arguments[0]._dataProviderController.isLoading()",
                this);
    }

    @Override
    protected boolean isLoading() {
        return super.isLoading() || isLoadingExpandedRows();
    }

    /**
     * Gets the total number of rows.
     * <p>
     * Note that for TreeGrid this does not return reliable results if rows are
     * expanded. Due to the lazy-loading nature of the grid, children of
     * expanded rows are only loaded into the grid when they are scrolled into
     * view. Likewise, they are removed again from the grid at some point when
     * they are scrolled out of view. These child rows then only count against
     * the total row count while they are loaded into the grid. Effectively,
     * that means that the total row count will depend on the scroll position of
     * the grid.
     * <p>
     * We are looking into making this more reliable by adding additional APIs
     * to TreeGrid that would allow the component to keep track of the total
     * number of rows just based on the expanded rows, and regardless of the
     * scroll position. Please see
     * <a href="https://github.com/vaadin/flow-components/issues/7269">this
     * issue</a> for more details.
     *
     * @return the number of rows
     */
    @Override
    public int getRowCount() {
        return super.getRowCount();
    }

    /**
     * Scrolls the TreeGrid to the end.
     */
    public void scrollToEnd() {
        executeScript(
                "arguments[0].scrollToIndex(...Array(10).fill(Infinity));",
                this);
    }
}
