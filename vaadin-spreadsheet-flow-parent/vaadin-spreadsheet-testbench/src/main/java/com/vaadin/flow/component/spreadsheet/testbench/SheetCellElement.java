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
            // mousedown with correct coordinates is needed so the
            // spreadsheet updates cell selection before editing starts.
            getCommandExecutor().getDriver().executeAsyncScript("""
                    var cell = arguments[0];
                    var shadowRoot = arguments[1].shadowRoot;
                    var root = shadowRoot
                        .querySelector('.v-spreadsheet');
                    var value = arguments[2];
                    var callback = arguments[3];
                    var r = cell.getBoundingClientRect();
                    var cx = r.left + r.width / 2;
                    var cy = r.top + r.height / 2;
                    var opts = {bubbles: true, cancelable: true,
                        view: window, clientX: cx, clientY: cy};
                    cell.dispatchEvent(new MouseEvent('mousedown', opts));
                    cell.dispatchEvent(new MouseEvent('mouseup', opts));
                    cell.dispatchEvent(new MouseEvent('click', opts));
                    cell.dispatchEvent(new MouseEvent('dblclick', opts));
                    // Build expected class prefix to verify cellinput
                    // is on the correct cell
                    var cellClasses = cell.className.match(
                        /col\\d+|row\\d+/g) || [];
                    var expectedPrefix = cellClasses.join(' ');
                    // Poll until cellinput is visible on the correct
                    // cell AND has focus. Focus check is critical:
                    // startEditingCell defers input.setFocus(true), and
                    // dispatching Tab before that runs causes
                    // onCellInputFocus to re-start editing on the next
                    // cell after Tab moves the selection.
                    var attempts = 0;
                    function waitForInput() {
                        var ci = root.querySelector('#cellinput');
                        // className is non-empty when editing is active
                        // (set by startEditingCell), and cleared by
                        // stopEditingCell. activeElement check ensures
                        // the deferred focus from startEditingCell has
                        // completed.
                        var ready = ci
                            && ci.className.indexOf(expectedPrefix) === 0
                            && shadowRoot.activeElement === ci;
                        if (ready) {
                            ci.value = value;
                            ci.dispatchEvent(new KeyboardEvent('keydown', {
                                key: 'Tab', code: 'Tab', keyCode: 9,
                                which: 9, bubbles: true, cancelable: true
                            }));
                            // Wait for the cellinput to be closed after
                            // the commit, so that the next setValue call
                            // starts with a clean state.
                            waitForClose();
                        } else if (++attempts < 50) {
                            setTimeout(waitForInput, 100);
                        } else {
                            callback(false);
                        }
                    }
                    function waitForClose() {
                        var ci = root.querySelector('#cellinput');
                        if (!ci || ci.className === '') {
                            callback(true);
                        } else if (++attempts < 50) {
                            setTimeout(waitForClose, 100);
                        } else {
                            callback(false);
                        }
                    }
                    waitForInput();
                    """, this, parent, newValue);
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
     * @return true if this cell has a pop-up button, false otherwise
     */
    public boolean hasPopupButton() {
        List<WebElement> buttons = findElements(By.className("popupbutton"));
        return !buttons.isEmpty();
    }

    /**
     * Click the PopupButton in the cell.
     *
     * @throws IllegalArgumentException
     *             if the cell doesn't contain a PopupButton
     */
    public void popupButtonClick() {
        if (!hasPopupButton()) {
            throw new IllegalStateException(
                    "This cell doesn't have a PopupuButton");
        }

        findElement(By.className("popupbutton")).click();
    }

    void setParent(SpreadsheetElement parent) {
        this.parent = parent;
    }

    /**
     * Determines if the cell has invalid formula indicator
     *
     * @return true if this cell has a invalid formula indicator
     */
    public boolean hasInvalidFormulaIndicator() {
        List<WebElement> indicators = findElements(
                By.className("cell-invalidformula-triangle"));
        return !indicators.isEmpty();
    }

    /**
     * Determines if the cell has comment indicator
     *
     * @return true if this cell has a comment indicator
     */
    public boolean hasCommentIndicator() {
        List<WebElement> indicators = findElements(
                By.className("cell-comment-triangle"));
        return !indicators.isEmpty();
    }

}
