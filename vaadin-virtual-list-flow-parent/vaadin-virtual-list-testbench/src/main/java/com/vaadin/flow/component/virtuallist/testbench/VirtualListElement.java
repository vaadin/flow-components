/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist.testbench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;

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

    /**
     * Selects the row with the given index.
     *
     * @param rowIndex
     *            the row to select
     */
    public void select(int rowIndex) {
        if (!isRowInView(rowIndex)) {
            scrollToRow(rowIndex);
        }
        if (getSelectedIndexes().contains(rowIndex)) {
            return;
        }

        var element = this.findElement(By.xpath("child::div[@aria-posinset='" + (rowIndex + 1) + "']"));
        element.click();
    }

    /**
     * Deselects the row with the given index.
     *
     * @param rowIndex
     *            the row to deselect
     */
    public void deselect(int rowIndex) {
        if (!isRowInView(rowIndex)) {
            scrollToRow(rowIndex);
        }
        if (!getSelectedIndexes().contains(rowIndex)) {
            return;
        }

        var element = this.findElement(By.xpath("child::div[@aria-posinset='" + (rowIndex + 1) + "']"));
        element.click();
    }

    /**
     * Gets the indexes of the selected rows.
     *
     * @return the indexes of the selected rows
     */
    public Set<Integer> getSelectedIndexes() {
        var selectedIndexes = (ArrayList<Long>) executeScript("return arguments[0].selectedItems.map(i => arguments[0].items.indexOf(i))", this);
        return Set.copyOf(selectedIndexes.stream().map(Long::intValue).toList());
    }

}
