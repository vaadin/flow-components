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
import org.openqa.selenium.WebElement;

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
     * Returns id by clearing spaces from the given text.
     *
     * @param id
     *            the text to make id from
     * @return the new id
     */
    protected String makeId(String id) {
        return id.replace(" ", "");
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
     * Finds element by given text by translating it to id with
     * {@link #makeId(String)} and finding element by that id.
     * <p>
     * Shortcut for calling:
     *
     * <pre>
     * findElement(By.id(makeId(text)))
     * </pre>
     *
     * @param text
     *            the target text
     * @return the found element
     */
    protected WebElement findElementByText(String text) {
        return findElement(By.id(makeId(text)));
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
}
