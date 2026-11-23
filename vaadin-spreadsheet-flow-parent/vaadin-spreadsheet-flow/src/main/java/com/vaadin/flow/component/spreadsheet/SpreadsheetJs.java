/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.Set;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

/**
 * The JavaScript that {@link Spreadsheet} runs on its element in the browser,
 * for {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface SpreadsheetJs extends JsInvokers {

    /**
     * Updates the formula bar of the spreadsheet with the given cell, in a
     * microtask.
     *
     * @param possibleName
     *            the name of the cell or of the selected range, or
     *            <code>null</code>
     * @param col
     *            the column of the cell
     * @param row
     *            the row of the cell
     */
    @JsExpression("queueMicrotask(() => this.updateFormulaBar($0, $1, $2));")
    void updateFormulaBar(String possibleName, int col, int row);

    /**
     * Sets the <code>resources</code> property of the spreadsheet to the given
     * resource keys.
     *
     * @param resourceKeys
     *            the keys of the resources
     */
    @JsExpression("this.resources=$0")
    void setResources(Set<String> resourceKeys);
}
