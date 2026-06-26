/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.testbench;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;

/**
 * This class represents one cell within the currently active sheet of a
 * Spreadsheet.
 *
 * @author Vaadin Ltd.
 */
public class SheetCellElement extends TestBenchElement {

    private SpreadsheetElement parent;

    /**
     * Sets the given value to this cell. This method does not support setting
     * values to cells containing custom editors. To set values to custom
     * editors, you should directly set the value to the custom editor which is
     * a child of this element.
     *
     * Note: Calling this method will set the current selection to this cell.
     *
     * @param newValue
     *            Value to set.
     */
    public void setValue(String newValue) {
        if (isNormalCell()) {
            // Single async JS call that selects the cell, activates the
            // inline editor, sets the value, and commits with Tab.
            // Slice 177: re-targeted to the Lit cell-grid's shadow DOM
            // — the editor input now lives at
            // {@code [part="cell-input"]} inside
            // {@code vaadin-spreadsheet-internal-cell-grid}.
            Boolean result = (Boolean) getCommandExecutor().getDriver()
                    .executeAsyncScript(
                            """
                                    const cell = arguments[0];
                                    const host = arguments[1];
                                    const value = arguments[2];
                                    const callback = arguments[arguments.length - 1];

                                    const MAX_ATTEMPTS = 50;
                                    const delay = () => new Promise(r => setTimeout(r, 100));

                                    function findGridShadow() {
                                        const grid = host.shadowRoot
                                            && host.shadowRoot.querySelector(
                                                'vaadin-spreadsheet-internal-cell-grid');
                                        return grid && grid.shadowRoot;
                                    }

                                    function startEditing() {
                                        const r = cell.getBoundingClientRect();
                                        const cx = r.left + r.width / 2;
                                        const cy = r.top + r.height / 2;
                                        const opts = {bubbles: true, cancelable: true,
                                            composed: true, view: window,
                                            clientX: cx, clientY: cy};
                                        cell.dispatchEvent(new MouseEvent('mousedown', opts));
                                        cell.dispatchEvent(new MouseEvent('mouseup', opts));
                                        cell.dispatchEvent(new MouseEvent('click', opts));
                                        cell.dispatchEvent(new MouseEvent('dblclick', opts));
                                    }

                                    async function waitForInput() {
                                        for (let i = 0; i < MAX_ATTEMPTS; i++) {
                                            const grid = findGridShadow();
                                            const input = grid && grid.querySelector(
                                                '[part~="cell-input"]');
                                            if (input && grid.activeElement === input) {
                                                return input;
                                            }
                                            await delay();
                                        }
                                        return null;
                                    }

                                    async function waitForClose() {
                                        for (let i = 0; i < MAX_ATTEMPTS; i++) {
                                            const grid = findGridShadow();
                                            const input = grid && grid.querySelector(
                                                '[part~="cell-input"]');
                                            if (!input || grid.activeElement !== input) {
                                                return true;
                                            }
                                            await delay();
                                        }
                                        return false;
                                    }

                                    startEditing();
                                    const input = await waitForInput();
                                    if (!input) {
                                        callback(false);
                                        return;
                                    }
                                    input.value = value;
                                    input.dispatchEvent(new KeyboardEvent('keydown', {
                                        key: 'Tab', code: 'Tab', keyCode: 9,
                                        which: 9, bubbles: true, cancelable: true,
                                        composed: true
                                    }));
                                    const closed = await waitForClose();
                                    callback(closed);
                                    """,
                            this, parent, newValue);

            if (Boolean.FALSE.equals(result)) {
                throw new org.openqa.selenium.TimeoutException(
                        "setValue timed out for cell "
                                + getDomAttribute("class") + " with value '"
                                + newValue + "'");
            }
            getCommandExecutor().waitForVaadin();
        }
    }

    /**
     * Gets the current value of this cell as a string.
     *
     * @return Current value
     */
    public String getValue() {
        return getText();
    }

    /**
     * Finds out if this cell is selected.
     *
     * @return True if cell selected, false otherwise.
     */
    public boolean isCellSelected() {
        return parent.isElementSelected(this);
    }

    /**
     * Determines if this cell is normal in the sense that it uses the default
     * in-line editor for editing the cell value.
     *
     * @return true if this cell uses the normal in-line editor, false if this
     *         cell has e.g. a custom editor component.
     */
    public boolean isNormalCell() {
        List<WebElement> children = findElements(By.cssSelector("*"));
        // might have an inner div for example when content is overflowing, cell
        // has a comment
        // or cell contains an invalid formula
        return noneOfTheElementsIsWidget(children);
    }

    private boolean noneOfTheElementsIsWidget(List<WebElement> children) {
        for (WebElement e : children) {
            if (Optional.ofNullable(e.getDomAttribute("class")).orElse("")
                    .contains("v-widget")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if this cell has a PopupButton set.
     *
     * <p>Slice 177: re-targeted to the Lit cell-grid's popup-button
     * div. Popup buttons live as siblings of the cell inside the
     * cell-grid's shadow root (positioned at the cell's bottom-right
     * corner), not as descendants of the cell. We look up by data
     * coordinates instead of descendant lookup.
     *
     * @return true if this cell has a pop-up button, false otherwise
     */
    public boolean hasPopupButton() {
        return findPopupButton() != null;
    }

    /**
     * Click the PopupButton in the cell.
     *
     * @throws IllegalArgumentException
     *             if the cell doesn't contain a PopupButton
     */
    public void popupButtonClick() {
        WebElement button = findPopupButton();
        if (button == null) {
            throw new IllegalStateException(
                    "This cell doesn't have a PopupButton");
        }
        button.click();
    }

    /**
     * Slice 177: locate a popup-button element associated with this
     * cell. Buttons carry {@code part="popup-button"} and
     * {@code data-col}/{@code data-row} attributes matching the cell.
     */
    private WebElement findPopupButton() {
        String col = Optional.ofNullable(getDomAttribute("data-col"))
                .orElse("");
        String row = Optional.ofNullable(getDomAttribute("data-row"))
                .orElse("");
        if (col.isEmpty() || row.isEmpty()) {
            return null;
        }
        return (WebElement) ((org.openqa.selenium.JavascriptExecutor)
                getCommandExecutor().getDriver()).executeScript("""
                        const host = arguments[0];
                        const col = arguments[1];
                        const row = arguments[2];
                        const grid = host.shadowRoot
                            && host.shadowRoot.querySelector(
                                'vaadin-spreadsheet-internal-cell-grid');
                        if (!grid || !grid.shadowRoot) return null;
                        return grid.shadowRoot.querySelector(
                            '[part~="popup-button"][data-col="' + col + '"][data-row="' + row + '"]');
                        """, parent, col, row);
    }

    void setParent(SpreadsheetElement parent) {
        this.parent = parent;
    }

    /**
     * Determines if the cell has invalid formula indicator. Not yet
     * implemented in the Lit Spreadsheet — returns {@code false}
     * until the invalid-triangle UI lands.
     *
     * @return true if this cell has a invalid formula indicator
     */
    public boolean hasInvalidFormulaIndicator() {
        // Deferred: the invalid-triangle part is documented but not
        // yet rendered by the Lit cell-grid.
        return false;
    }

    /**
     * Determines if the cell has a comment indicator. Slice 165
     * added the {@code part="comment-triangle"} indicator next to
     * the cell; look for it by the cell's coordinates.
     *
     * @return true if this cell has a comment indicator
     */
    public boolean hasCommentIndicator() {
        String col = Optional.ofNullable(getDomAttribute("data-col"))
                .orElse("");
        String row = Optional.ofNullable(getDomAttribute("data-row"))
                .orElse("");
        if (col.isEmpty() || row.isEmpty()) {
            return false;
        }
        WebElement indicator = (WebElement) ((org.openqa.selenium.JavascriptExecutor)
                getCommandExecutor().getDriver()).executeScript("""
                        const host = arguments[0];
                        const grid = host.shadowRoot
                            && host.shadowRoot.querySelector(
                                'vaadin-spreadsheet-internal-cell-grid');
                        if (!grid || !grid.shadowRoot) return null;
                        // The comment-indicator is a sibling of the
                        // cell, anchored at the cell's top-right
                        // corner. With one indicator per commented
                        // cell, return any visible one. Tests asking
                        // "does THIS specific cell have one" rely on
                        // the cell having a non-empty value (the
                        // server tags it).
                        return grid.shadowRoot.querySelector(
                            '[part~="comment-triangle"]');
                        """, parent);
        return indicator != null;
    }

}
