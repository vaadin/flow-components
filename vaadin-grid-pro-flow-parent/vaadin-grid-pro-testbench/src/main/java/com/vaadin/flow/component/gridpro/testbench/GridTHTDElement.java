/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.testbench;

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

    public boolean innerHTMLContains(String key) {
        return this.getInnerHTML().contains(key);
    }

    /**
     * Gets the column for this grid cell.
     *
     * @return the column element
     */
    public GridProColumnElement getColumn() {
        Double id = getPropertyDouble("_column", "__generatedTbId");
        GridProElement grid = getGrid();
        if (id == null) {
            grid.generatedColumnIdsIfNeeded();
            id = getPropertyDouble("_column", "__generatedTbId");
        }
        if (id == null) {
            throw new NoSuchElementException(
                    "Unable to find column. This should not really happen.");
        }
        return new GridProColumnElement(id.longValue(), grid);
    }

    /**
     * Gets the grid containing this element.
     *
     * @return the grid for this element
     */
    public GridProElement getGrid() {
        return ((TestBenchElement) executeScript(
                "return arguments[0].getRootNode().host", this))
                        .wrap(GridProElement.class);
    }

    @Override
    public SearchContext getContext() {
        return (SearchContext) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0];",
                this);
    }
}
