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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;

/**
 * Represents a footer cell in a Grid component.
 */
public class GridFooterCell {
    
    private final GridElement grid;
    private final int rowIndex;
    private final int columnIndex;
    private final WebElement cellElement;
    
    /**
     * Creates a new GridFooterCell instance.
     *
     * @param grid
     *            the grid this cell belongs to
     * @param rowIndex
     *            the row index of this cell
     * @param columnIndex
     *            the column index of this cell
     * @param cellElement
     *            the WebElement representing this cell
     */
    public GridFooterCell(GridElement grid, int rowIndex, int columnIndex, 
                          WebElement cellElement) {
        this.grid = grid;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.cellElement = cellElement;
    }
    
    /**
     * Gets the row index of this cell.
     *
     * @return the row index
     */
    public int getRowIndex() {
        return rowIndex;
    }
    
    /**
     * Gets the column index of this cell.
     *
     * @return the column index
     */
    public int getColumnIndex() {
        return columnIndex;
    }
    
    /**
     * Gets the text content of this footer cell.
     *
     * @return the text content
     */
    public String getText() {
        return cellElement.getText();
    }
    
    /**
     * Gets the colspan attribute of this footer cell.
     *
     * @return the colspan value, or 1 if not set
     */
    public int getColspan() {
        String colspan = cellElement.getAttribute("colspan");
        if (colspan != null && !colspan.isEmpty()) {
            try {
                return Integer.parseInt(colspan);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }
    
    /**
     * Gets the rowspan attribute of this footer cell.
     *
     * @return the rowspan value, or 1 if not set
     */
    public int getRowspan() {
        String rowspan = cellElement.getAttribute("rowspan");
        if (rowspan != null && !rowspan.isEmpty()) {
            try {
                return Integer.parseInt(rowspan);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }
    
    /**
     * Checks if this footer cell spans multiple columns.
     *
     * @return true if colspan > 1
     */
    public boolean isJoined() {
        return getColspan() > 1;
    }
    
    /**
     * Checks if this footer cell spans multiple rows.
     *
     * @return true if rowspan > 1
     */
    public boolean isMultiRow() {
        return getRowspan() > 1;
    }
    
    /**
     * Gets the content element of this footer cell.
     *
     * @return the vaadin-grid-cell-content element
     */
    public TestBenchElement getContent() {
        return grid.getFooterCellContent(rowIndex, columnIndex);
    }
    
    /**
     * Checks if this footer cell is visible.
     *
     * @return true if the cell is visible, false otherwise
     */
    public boolean isVisible() {
        return cellElement.isDisplayed();
    }
    
    /**
     * Gets the underlying WebElement.
     *
     * @return the WebElement
     */
    public WebElement getWrappedElement() {
        return cellElement;
    }
    
    /**
     * Clicks on this footer cell.
     */
    public void click() {
        cellElement.click();
    }
}