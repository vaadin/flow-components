/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.CustomEditorDelayedCallbackFixture;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * Covers the text-selection half of issues #9180 and #9036: the value set by
 * {@code onCustomEditorDisplayed} must end up selected, and stay selected.
 * <p>
 * Note that {@code autoselect} is not what selects the text — it fires on
 * focus, before the delayed callback sets its value. The fixture's explicit
 * {@code inputElement.select()} is.
 */
@TestPath("vaadin-spreadsheet")
public class CustomEditorSelectionIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
        loadTestFixture(TestFixtures.CustomEditorDelayedCallback);
    }

    @Test
    public void editorDisplayed_callbackSelectsInput_textIsSelected() {
        clickCell("B2");

        assertEditorTextSelected("B2");
    }

    @Test
    public void editorDisplayed_callbackSetsSameValueAgain_textIsSelected() {
        clickCell("B2");
        assertEditorTextSelected("B2");

        // Leave and come back. The callback sets the same text again, so there
        // is no value change for the client to react to — the case reported as
        // the selection no longer working.
        clickCell("A4");
        getCommandExecutor().waitForVaadin();
        clickCell("B2");

        assertEditorTextSelected("B2");
    }

    @Test
    public void editorDisplayed_callbackRefreshedCellWithoutGuard_callbackNotRefired() {
        clickCell("B2");
        assertEditorTextSelected("B2");

        // The fixture's value-change listener has no isFromClient guard, so the
        // value the callback sets reaches it and calls refreshCells. That
        // refresh has to reuse the editor, or the callback fires again on the
        // same cell and keeps going.
        Assert.assertEquals(
                "onCustomEditorDisplayed re-fired after the un-guarded refreshCells",
                "1", getCallbackCount());
    }

    /**
     * Waits until the callback has set its value in the given cell's editor,
     * then asserts that the whole text is selected and that the editor still
     * holds the focus. The focus check covers the other half of #9036: focus
     * must not move off the editor while the delayed callback runs.
     *
     * @param cellAddress
     *            address of the cell holding the editor, e.g. "B2"
     */
    private void assertEditorTextSelected(String cellAddress) {
        String expectedValue = CustomEditorDelayedCallbackFixture.CALLBACK_VALUE_PREFIX
                + columnIndexOf(cellAddress);
        EditorInputState state = waitUntil(driver -> {
            var current = getEditorInputState(cellAddress);
            return current != null && expectedValue.equals(current.value())
                    ? current
                    : null;
        });
        Assert.assertTrue(
                "Text in " + cellAddress + " not fully selected: " + state,
                state.isFullySelected());
        Assert.assertTrue("Editor in " + cellAddress + " lost focus: " + state,
                state.focused());
    }

    private String getCallbackCount() {
        return $(TestBenchElement.class).id("callbackCount").getText();
    }

    private int columnIndexOf(String cellAddress) {
        return cellAddress.charAt(0) - 'A';
    }
}
