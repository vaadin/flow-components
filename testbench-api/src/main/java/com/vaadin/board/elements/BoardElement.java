package com.vaadin.board.elements;

/*
 * #%L
 * Vaadin Spreadsheet Testbench API
 * %%
 * Copyright (C) 2013 - 2016 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elements.AbstractComponentElement;
import com.vaadin.testbench.elementsbase.ServerClass;

/**
 * This is the base element class for accessing a Vaadin Board component
 * for TestBench testing.
 *
 * @author Vaadin Ltd.
 */
@ServerClass("com.vaadin.board.Board")
public class BoardElement extends AbstractComponentElement {

    /**
     * Gets the row header element at the given index.
     *
     * @param rowIndex
     *     Index of target row, 0-based
     * @return Row at the given index
     */
    public RowElement getRow(int rowIndex) {
        List<RowElement> rows = getRows();
        if (rows != null && rows.size() > rowIndex) {
            return rows.get(rowIndex);
        }
        throw new NoSuchElementException(
            String.format("There is no row with the index %d in the current board", rowIndex));
    }

    /**
     * Gets all rows from the board. Inner rows are not included.
     *
     * @return Row elements
     */
    public List<RowElement> getRows() {
        List<WebElement> elements = findElements(By.xpath("./vaadin-board-row"));
        List<RowElement> rElements = new ArrayList<RowElement>();
        for (TestBenchElement elem : wrapElements(elements, getCommandExecutor())) {
            rElements.add(elem.wrap(RowElement.class));
        }
        return rElements;
    }
}