/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.testbench;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;

/**
 * This class represents a header (either row- or column) within the currently
 * active sheet of a Spreadsheet.
 *
 * @author Vaadin Ltd.
 */
public class SheetHeaderElement extends TestBenchElement {

    /**
     * The drag handle that resizes this header.
     *
     * GWT nested the handle inside the header cell. The Lit headers render
     * handles as siblings of the cells, each carrying the same {@code data-col}
     * / {@code data-row} as the header it resizes, so the handle is found by
     * coordinate rather than by descent.
     *
     * @return the resize handle, or {@code null} if this header has none
     */
    public TestBenchElement getResizeHandle() {
        WebElement handle = (WebElement) ((JavascriptExecutor) getCommandExecutor()
                .getDriver()).executeScript("""
                        const header = arguments[0];
                        const root = header.getRootNode();
                        if (!root || !root.querySelector) return null;
                        const col = header.dataset.col;
                        const row = header.dataset.row;
                        if (col !== undefined) {
                            return root.querySelector(
                                '.col-resize-handle[data-col="' + col + '"]');
                        }
                        if (row !== undefined) {
                            return root.querySelector(
                                '.row-resize-handle[data-row="' + row + '"]');
                        }
                        return null;
                        """, this);
        return handle == null ? null
                : wrapElement(handle, getCommandExecutor());
    }
}
