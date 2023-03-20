/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.testbench;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;

import com.vaadin.testbench.TestBenchElement;

/**
 * A TestBench element representing a <code>&lt;td&gt;</code> or
 * <code>&lt;th&gt;</code> element in a grid.
 */
public class GridTHTDElement extends TestBenchElement {

    @Override
    public String getText() {
        // The first child element of a cell is a slot. The following JS finds
        // the elements assigned to that slot and then joins the `textContent`
        // of the elements slots
        String text = (String) executeScript("var cell = arguments[0];"
                + "return Array.from(cell.firstElementChild.assignedNodes()).map(function(node) { return node.textContent;}).join('');",
                this);
        if (text.trim().isEmpty()) {
            return "";
        } else {
            return text;
        }
    }

    public String getInnerHTML() {
        // The first child element of a cell is a slot. The following JS finds
        // the elements assigned to that slot and then joins the `innerHTML`
        // of the elements slots
        String text = (String) executeScript("var cell = arguments[0];"
                + "return Array.from(cell.firstElementChild.assignedNodes()).map(function(node) { return node.innerHTML;}).join('');",
                this);
        if (text.trim().isEmpty()) {
            return "";
        } else {
            return text;
        }
    }

    /**
     * Gets the text content of the elements assigned to the given slot
     *
     * @param slot
     *            a <code>&lt;slot&gt;</code> element
     * @return the combined text content of all elements assigned to the given
     *         slot
     */
    protected static String getSlotText(TestBenchElement slot) {
        List<TestBenchElement> content = (List<TestBenchElement>) slot
                .callFunction("assignedNodes");
        StringBuilder text = new StringBuilder();
        content.forEach(element -> text.append(element.getText()));

        return text.toString();
    }

    /**
     * Gets the row index for this grid cell.
     *
     * @return the row index
     */
    public int getRow() {
        return getPropertyInteger("parentElement", "index");
    }

    /**
     * Gets the column for this grid cell.
     *
     * @return the column element
     */
    public GridColumnElement getColumn() {
        Double id = getPropertyDouble("_column", "__generatedTbId");
        GridElement grid = getGrid();
        if (id == null) {
            grid.generatedColumnIdsIfNeeded();
            id = getPropertyDouble("_column", "__generatedTbId");
        }
        if (id == null) {
            throw new NoSuchElementException(
                    "Unable to find column. This should not really happen.");
        }
        return new GridColumnElement(id.longValue(), grid);
    }

    /**
     * Gets the grid containing this element.
     *
     * @return the grid for this element
     */
    public GridElement getGrid() {
        return ((TestBenchElement) executeScript(
                "return arguments[0].getRootNode().host", this))
                        .wrap(GridElement.class);
    }

    /**
     * Gets the TR parent of this element.
     *
     * @return the parent TR element
     */
    public GridTRElement getRowElement() {
        return getPropertyElement("parentElement").wrap(GridTRElement.class);
    }

    @Override
    public SearchContext getContext() {
        return (SearchContext) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0];",
                this);
    }
}
