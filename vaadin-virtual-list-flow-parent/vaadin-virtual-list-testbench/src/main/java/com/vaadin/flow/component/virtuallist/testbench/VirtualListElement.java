/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.virtuallist.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing an <code>&lt;vaadin-virtual-list&gt;</code>
 * element.
 */
@Element("vaadin-virtual-list")
public class VirtualListElement extends TestBenchElement {

    /**
     * Scrolls to the row with the given index.
     *
     * @param rowIndex
     *            the row to scroll to
     */
    public void scrollToRow(int rowIndex) {
        callFunction("scrollToIndex", rowIndex);
    }

    /**
     * Gets the index of the first row which is at least partially visible.
     *
     * @return the index of the first visible row
     */
    public int getFirstVisibleRowIndex() {
        executeScript("arguments[0].__requestDebounce.flush();", this);
        return getPropertyInteger("firstVisibleIndex");
    }

    /**
     * Gets the index of the last row which is at least partially visible.
     *
     * @return the index of the last visible row
     */
    public int getLastVisibleRowIndex() {
        executeScript("arguments[0].__requestDebounce.flush();", this);
        return getPropertyInteger("lastVisibleIndex");
    }

    /**
     * Checks if the given row is in the visible viewport.
     *
     * @param rowIndex
     *            the row to check
     * @return <code>true</code> if the row is at least partially in view,
     *         <code>false</code> otherwise
     */
    public boolean isRowInView(int rowIndex) {
        return getFirstVisibleRowIndex() <= rowIndex
                && rowIndex <= getLastVisibleRowIndex();
    }

    /**
     * Gets the total number of rows.
     *
     * @return the number of rows
     */
    public int getRowCount() {
        return getPropertyInteger("items", "length");
    }

}
