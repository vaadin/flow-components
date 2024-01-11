/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.board.testbench;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * This is the base element class for accessing a Vaadin Board component for
 * TestBench testing.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-board")
public class BoardElement extends TestBenchElement {
    /**
     * Gets the row element at the given index.
     *
     * @param rowIndex
     *            index of target row
     * @return the row at the given index
     */
    public RowElement getRow(int rowIndex) {
        List<RowElement> rows = getRows();
        if (rows != null && rows.size() > rowIndex) {
            return rows.get(rowIndex);
        }
        throw new NoSuchElementException(String.format(
                "There is no row with the index %d in the current board",
                rowIndex));
    }

    /**
     * Gets all rows from the board. Inner rows are not included.
     *
     * @return all row elements
     */
    public List<RowElement> getRows() {
        List<WebElement> elements = findElements(
                By.xpath("./vaadin-board-row"));
        List<RowElement> rElements = new ArrayList<RowElement>();
        for (TestBenchElement elem : wrapElements(elements,
                getCommandExecutor())) {
            rElements.add(elem.wrap(RowElement.class));
        }
        return rElements;
    }
}
