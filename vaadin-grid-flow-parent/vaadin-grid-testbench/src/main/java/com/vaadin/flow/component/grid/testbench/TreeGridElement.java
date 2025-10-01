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

import java.util.Arrays;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * TestBench Element API for TreeGrid.
 */
public class TreeGridElement extends GridElement {
    /**
     * Scrolls to the row with the given index in the root level.
     *
     * @deprecated since 25.0. Please update your code to use
     *             {@link #scrollToRowByPath(int...)} with a single index where
     *             you want to scroll to a root-level row. In Vaadin 26, this
     *             deprecated method will be changed to accept a flat index and
     *             behave as {@link #scrollToRowByFlatIndex(int)}, which may
     *             break tests if not updated.
     */
    @Override
    @Deprecated(since = "25.0")
    public void scrollToRow(int row) {
        scrollToRowByPath(row);
    }

    /**
     * Scrolls to the row with the given index in the root level.
     *
     * @param row
     *            the row to scroll to
     * @deprecated since 25.0 and will be removed in Vaadin 26. Use
     *             {@link #scrollToRowByPath(int...)} with a single index
     *             instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public void scrollToRowAndWait(int row) {
        scrollToRowByPath(row);
    }

    /**
     * Scrolls to the row with the given indexes. The indexes are hierarchical,
     * starting with the root index.
     *
     * @param indexes
     *            the indexes of the row to scroll to
     * @deprecated since 25.0 and will be removed in Vaadin 26. Use
     *             {@link #scrollToRowByPath(int...)} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public void scrollToRowAndWait(int... indexes) {
        scrollToRowByPath(indexes);
    }

    /**
     * Scrolls to the row with the given flat index.
     *
     * @param row
     *            the row to scroll to
     * @deprecated since 25.0, and will be removed in Vaadin 26. Use
     *             {@link #scrollToRowByFlatIndex(int)} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public void scrollToFlatRowAndWait(int row) {
        scrollToRowByFlatIndex(row);
    }

    /**
     * Scrolls to a row at the given flat index, an index that represents the
     * position across all rows, including children of expanded rows.
     * <p>
     * This method works best with {@code HierarchyFormat#FLATTENED} data
     * providers, where the full hierarchy is known upfront and flat indexes
     * remain stable during scrolling.
     * <p>
     * When using {@code HierarchyFormat#NESTED} data providers, the hierarchy
     * is resolved lazily while scrolling, so flat indexes may shift as more
     * levels are discovered. With these data providers, consider using
     * {@link #scrollToRowByPath(int...)}, which targets rows by their
     * hierarchical path.
     *
     * @param rowFlatIndex
     *            the flat index of the row to scroll to
     */
    public void scrollToRowByFlatIndex(int rowFlatIndex) {
        super.scrollToRowByFlatIndex(rowFlatIndex);
    }

    /**
     * Scrolls to a row specified by the given hierarchical path and returns its
     * flat index. The returned index can then be used with other methods, for
     * example {@link #getRow(int)}:
     *
     * <pre>
     * int rowFlatIndex = treeGrid.scrollToRowByPath(2, 1);
     * assertEquals("Row 2-1", treeGrid.getRow(rowFlatIndex).getText());
     * </pre>
     * <p>
     * The hierarchical path is an array of 0-based indexes, where each index
     * refers to a child of the row at the previous index. Scrolling continues
     * until it reaches the last index in the array or encounters a collapsed
     * row.
     * <p>
     * For example, given {@code &#123; 2, 1, ... &#125;} as the path, this
     * method will first try to scroll to the row at index 2 in the root level.
     * If that row is expanded, it will then try to scroll to the row at index 1
     * among its children, and so forth.
     * <p>
     * <b>NOTE:</b> This method works only with tree grids using data providers
     * that return data in {@code HierarchyFormat#NESTED}. For
     * {@code HierarchyFormat#FLATTENED} data providers, use
     * {@link #scrollToRowByFlatIndex(int)} with a flat index instead.
     *
     * @param path
     *            an array of indexes representing the path to the target row
     * @return the flat index of the row that was scrolled to
     */
    public int scrollToRowByPath(int... path) {
        waitUntilLoadingFinished();
        return (int) (long) getCommandExecutor().getDriver()
                .executeAsyncScript("""
                        const [element, path, callback] = arguments;
                        const flatIndex = await element.scrollToIndex(...path);
                        callback(flatIndex);
                        """, this, path);
    }

    /**
     * Gets the grid cell for the given row and column index.
     * <p>
     * For the column index, only visible columns are taken into account.
     * <p>
     * Automatically scrolls the given row into view and waits for the row to
     * load.
     *
     * @param rowFlatIndex
     *            the flat index of the row
     * @param colIndex
     *            the column index
     * @return the grid cell for the given coordinates
     */
    public GridTHTDElement getCellWaitForRow(int rowFlatIndex, int colIndex) {
        GridColumnElement column = getVisibleColumns().get(colIndex);
        return getCellWaitForRow(rowFlatIndex, column);
    }

    /**
     * Gets the grid cell for the given row and column.
     * <p>
     * Automatically scrolls the given row into view and waits for the row to
     * load.
     *
     * @param rowFlatIndex
     *            the flat index of the row
     * @param column
     *            the column element for the column
     * @return the grid cell for the given coordinates
     */
    public GridTHTDElement getCellWaitForRow(int rowFlatIndex,
            GridColumnElement column) {
        if (!((getFirstVisibleRowIndex() <= rowFlatIndex
                && rowFlatIndex <= getLastVisibleRowIndex()))) {
            scrollToRowByFlatIndex(rowFlatIndex);
        }

        GridTRElement row = getRow(rowFlatIndex);
        return row.getCell(column);
    }

    /**
     * Expands the row at the given index in the grid. This expects the first
     * column to have the hierarchy data.
     *
     * @param rowFlatIndex
     *            the flat index of the row to expand
     * @see #expandWithClick(int, int)
     */
    public void expandWithClick(int rowFlatIndex) {
        expandWithClick(rowFlatIndex, 0);
    }

    /**
     * Expands the row at the given index in the grid with the given
     * hierarchical column index.
     *
     * @param rowFlatIndex
     *            the flat index of the row to expand
     * @param hierarchyColumnIndex
     *            the index of the hierarchy column
     */
    public void expandWithClick(int rowFlatIndex, int hierarchyColumnIndex) {
        if (isRowExpanded(rowFlatIndex, hierarchyColumnIndex)) {
            throw new IllegalStateException("The element at row " + rowFlatIndex
                    + " was expanded already");
        }
        getExpandToggleElement(rowFlatIndex, hierarchyColumnIndex).click();
        waitUntilLoadingFinished();
    }

    /**
     * Collapses the row at the given index in the grid. This expects the first
     * column to have the hierarchy data.
     *
     * @param rowFlatIndex
     *            the flat index of the row to collapse
     * @see #collapseWithClick(int, int)
     */
    public void collapseWithClick(int rowFlatIndex) {
        collapseWithClick(rowFlatIndex, 0);
    }

    /**
     * Collapses the row at the given index in the grid with the given
     * hierarchical column index.
     *
     * @param rowFlatIndex
     *            the flat index of the row to collapse
     * @param hierarchyColumnIndex
     *            the index of the hierarchy column
     */
    public void collapseWithClick(int rowFlatIndex, int hierarchyColumnIndex) {
        if (isRowCollapsed(rowFlatIndex, hierarchyColumnIndex)) {
            throw new IllegalStateException("The element at row " + rowFlatIndex
                    + " was collapsed already");
        }
        getExpandToggleElement(rowFlatIndex, hierarchyColumnIndex).click();
        waitUntilLoadingFinished();
    }

    /**
     * Returns whether the row at the given index is expanded or not.
     *
     * @param rowFlatIndex
     *            the flat index of the row
     * @param hierarchyColumnIndex
     *            the index of the hierarchy column
     * @return {@code true} if expanded, {@code false} if collapsed
     */
    public boolean isRowExpanded(int rowFlatIndex, int hierarchyColumnIndex) {
        waitUntilLoadingFinished();
        WebElement expandElement = getExpandToggleElement(rowFlatIndex,
                hierarchyColumnIndex);
        return expandElement != null
                && !"false".equals(expandElement.getDomProperty("expanded"));
    }

    /**
     * Returns whether the row at the given index is collapsed or not.
     *
     * @param rowFlatIndex
     *            the flat index of the row
     * @param hierarchyColumnIndex
     *            the index of the hierarchy column
     * @return {@code true} if collapsed, {@code false} if expanded
     */
    public boolean isRowCollapsed(int rowFlatIndex, int hierarchyColumnIndex) {
        return !isRowExpanded(rowFlatIndex, hierarchyColumnIndex);
    }

    /**
     * Check whether the given indices correspond to a cell that contains a
     * visible hierarchy toggle element.
     *
     * @param rowFlatIndex
     *            the flat index of the row
     * @param hierarchyColumnIndex
     *            the index of the hierarchy column
     * @return {@code true} if this cell has the expand toggle visible
     */
    public boolean hasExpandToggle(int rowFlatIndex, int hierarchyColumnIndex) {
        try {
            WebElement expandElement = getExpandToggleElement(rowFlatIndex,
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
     * @param rowFlatIndex
     *            the flat index of the row
     * @param hierarchyColumnIndex
     *            the index of the hierarchy column
     * @return the {@code span} element that is clicked for expanding/collapsing
     *         a rows
     * @throws NoSuchElementException
     *             if there is no expand element for this row
     */
    public WebElement getExpandToggleElement(int rowFlatIndex,
            int hierarchyColumnIndex) {
        GridTHTDElement cell = getCell(rowFlatIndex, hierarchyColumnIndex);
        return cell == null ? null : cell.$("vaadin-grid-tree-toggle").first();
    }

    /**
     * Returns {@code true} if details are open or the given row index.
     *
     * @param rowFlatIndex
     *            the flat index of the row
     * @return {@code true} if details are shown in the target row
     */
    public boolean isDetailsOpen(int rowFlatIndex) {
        try {
            return getRow(rowFlatIndex).getDetails().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Returns true if given index has tr element for the row
     *
     * @param rowFlatIndex
     *            the flat index of the row
     * @return <code>true</code> if there is tr element for the row,
     *         <code>false</code> otherwise
     */
    public boolean hasRow(int rowFlatIndex) {
        try {
            return getRow(rowFlatIndex) != null;
        } catch (Exception e) {
            return false;
        }
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
        int[] path = new int[11];
        Arrays.fill(path, -1);
        scrollToRowByPath(path);
    }
}
