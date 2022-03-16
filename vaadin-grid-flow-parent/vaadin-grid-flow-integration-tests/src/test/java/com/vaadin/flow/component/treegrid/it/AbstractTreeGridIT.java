package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractTreeGridIT extends AbstractComponentIT {

    private TreeGridElement grid;

    public void setupTreeGrid() {
        waitUntil(e -> $(TreeGridElement.class).exists(), 500);
        grid = $(TreeGridElement.class).first();
    }

    /**
     * Returns {@link TreeGridElement} created in {@link #setupTreeGrid()}. Or
     * null if not initialized.
     *
     * @return the optional grid element
     */
    protected TreeGridElement getTreeGrid() {
        return grid;
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
     * {@code cellTexts} starting from given {@code startRowIndex} and
     * {@code cellIndex}.
     *
     * @param startRowIndex
     *            First row index. Starts from 0.
     * @param cellIndex
     *            the first cell index. Starts from 0.
     * @param cellTexts
     *            Expected cell texts
     */
    protected void assertCellTexts(int startRowIndex, int cellIndex,
            String... cellTexts) {
        int index = startRowIndex;
        for (String cellText : cellTexts) {
            assertCellText(index, cellIndex, cellText);
            index++;
        }
    }

    private void assertCellText(int rowIndex, int cellIndex,
            String expectedText) {
        if (!((grid.getFirstVisibleRowIndex() <= rowIndex
                && rowIndex <= grid.getLastVisibleRowIndex()))) {
            grid.scrollToRowAndWait(rowIndex);
        }
        GridColumnElement column = grid.getVisibleColumns().get(cellIndex);
        try {
            waitUntil(
                    test -> grid.hasRow(rowIndex) && expectedText.equals(
                            grid.getRow(rowIndex).getCell(column).getText()),
                    500);
        } catch (Exception e) {
            Assert.fail(String.format(
                    "Expected cell text [%s] but got %s in row %s cell %s",
                    expectedText, e.getClass().getSimpleName(), rowIndex,
                    cellIndex));
        }
    }
}
