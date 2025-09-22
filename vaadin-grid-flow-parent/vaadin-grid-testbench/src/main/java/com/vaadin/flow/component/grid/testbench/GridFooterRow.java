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
import org.openqa.selenium.WebElement;

/**
 * Represents a footer row in a Grid component.
 */
public class GridFooterRow {
    
    private final GridElement grid;
    private final int rowIndex;
    private final WebElement rowElement;
    
    /**
     * Creates a new GridFooterRow instance.
     *
     * @param grid
     *            the grid this footer row belongs to
     * @param rowIndex
     *            the index of this footer row
     * @param rowElement
     *            the WebElement representing this row
     */
    public GridFooterRow(GridElement grid, int rowIndex, WebElement rowElement) {
        this.grid = grid;
        this.rowIndex = rowIndex;
        this.rowElement = rowElement;
    }
    
    /**
     * Gets the index of this footer row.
     *
     * @return the row index
     */
    public int getRowIndex() {
        return rowIndex;
    }
    
    /**
     * Gets all footer cells in this row.
     *
     * @return list of footer cells
     */
    public List<GridFooterCell> getCells() {
        List<WebElement> cellElements = rowElement.findElements(By.tagName("td"));
        List<GridFooterCell> cells = new ArrayList<>();
        for (int i = 0; i < cellElements.size(); i++) {
            cells.add(new GridFooterCell(grid, rowIndex, i, cellElements.get(i)));
        }
        return cells;
    }
    
    /**
     * Gets a specific footer cell by column index.
     *
     * @param columnIndex
     *            the column index
     * @return the footer cell at the given column index
     */
    public GridFooterCell getCell(int columnIndex) {
        List<WebElement> cellElements = rowElement.findElements(By.tagName("td"));
        if (columnIndex >= cellElements.size()) {
            throw new IndexOutOfBoundsException(
                    "Column index " + columnIndex + " is out of bounds. Row has " + 
                    cellElements.size() + " cells.");
        }
        return new GridFooterCell(grid, rowIndex, columnIndex, cellElements.get(columnIndex));
    }
    
    /**
     * Gets the number of cells in this footer row.
     *
     * @return the number of cells
     */
    public int getCellCount() {
        return rowElement.findElements(By.tagName("td")).size();
    }
    
    /**
     * Checks if this footer row is visible.
     *
     * @return true if the row is visible, false otherwise
     */
    public boolean isVisible() {
        return rowElement.isDisplayed();
    }
    
    /**
     * Gets all text content from cells in this row.
     *
     * @return list of text content from all cells
     */
    public List<String> getCellTexts() {
        return getCells().stream()
                .map(GridFooterCell::getText)
                .collect(Collectors.toList());
    }
}