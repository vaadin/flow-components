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
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractTreeGridIT extends AbstractComponentIT {

    private TreeGridElement treeGrid;

    public void setupTreeGrid() {
        waitUntil(e -> $(TreeGridElement.class).exists(), 2);
        treeGrid = $(TreeGridElement.class).first();
    }

    /**
     * Returns {@link TreeGridElement} created in {@link #setupTreeGrid()}. Or
     * null if not initialized.
     *
     * @return the optional grid element
     */
    protected TreeGridElement getTreeGrid() {
        return treeGrid;
    }

    /**
     * Finds element with id 'log' and checks if its value contains given text.
     * Uses {@link String#contains(CharSequence)} to search.
     *
     * @param txt
     *            the text to search
     * @return {@code true} when text if found.
     */
    protected boolean logContainsText(String txt) {
        String value = (String) executeScript("return arguments[0].value",
                findElement(By.id("log")));
        return value != null && value.contains(txt);
    }

    /**
     * Finds element with id 'log' and clears it if its input field.
     */
    protected void clearLog() {
        executeScript("arguments[0].value=''", findElement(By.id("log")));
    }

    /**
     * Asserts that TreeGrid contains same texts in cells as the given
     * {@code expectedCellTexts} starting from given {@code startRowIndex} and
     * {@code cellIndex}.
     *
     * @param startRowIndex
     *            First row index. Starts from 0.
     * @param cellIndex
     *            the first cell index. Starts from 0.
     * @param expectedCellTexts
     *            Expected cell texts
     */
    protected void assertCellTexts(int startRowIndex, int cellIndex,
            String... expectedCellTexts) {
        int rowIndex = startRowIndex;

        for (String expectedText : expectedCellTexts) {
            String actualText = treeGrid.getCell(rowIndex, cellIndex).getText();

            Assert.assertEquals(
                    "Expected cell text [%s] but got %s in row %s cell %s"
                            .formatted(expectedText, actualText, rowIndex,
                                    cellIndex),
                    expectedText, actualText);

            rowIndex++;
        }
    }

    protected void assertRowExpanded(int startRowIndex,
            boolean... expectedStates) {
        int rowIndex = startRowIndex;
        for (boolean expectedState : expectedStates) {
            Assert.assertEquals(
                    "Row with index %d has unexpected expanded state"
                            .formatted(rowIndex),
                    String.valueOf(expectedState),
                    treeGrid.getExpandToggleElement(rowIndex, 0)
                            .getDomProperty("expanded"));
            rowIndex++;
        }
    }

    protected void assertRowLevel(int startRowIndex, int... expectedStates) {
        int rowIndex = startRowIndex;
        for (int expectedState : expectedStates) {
            Assert.assertEquals(
                    "Row with index %d has unexpected level"
                            .formatted(rowIndex),
                    String.valueOf(expectedState),
                    treeGrid.getExpandToggleElement(rowIndex, 0)
                            .getDomProperty("level"));

            Assert.assertEquals(
                    "Row with index %d has unexpected aria-level value"
                            .formatted(rowIndex),
                    String.valueOf(expectedState + 1),
                    treeGrid.getRow(rowIndex).getDomAttribute("aria-level"));

            rowIndex++;
        }
    }
}
