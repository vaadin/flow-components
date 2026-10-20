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

import com.vaadin.flow.testutil.TestPath;

/**
 * Test for spreadsheets that have both hidden and frozen rows/columns.
 *
 */
@TestPath("vaadin-spreadsheet")
public class HiddenAndFrozenIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void freezePane_sheetWithHiddenAndFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        assertFreezePanePositionedCorrectly("hidden_and_frozen.xlsx", 11, 9,
                "O15");
    }

    @Test
    public void freezePane_sheetWithMoreHiddenThanFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        assertFreezePanePositionedCorrectly("more_hidden_than_frozen.xlsx", 1,
                1, "E7");
    }

    @Test
    public void freezePane_sheetWithRandomHiddenAndFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        assertFreezePanePositionedCorrectly("randomly_hidden_and_frozen.xlsx",
                5, 3, "H15");
    }

    @Test
    public void freezePane_sheetWithImplicitlyHiddenFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        assertFreezePanePositionedCorrectly("scrolled_frozen.xlsx", 2, 4, "G5");
    }

    private void assertFreezePanePositionedCorrectly(String filename,
            int frozenRows, int frozenColumns, String regularCell) {
        loadFile(filename);

        assertFrozenHeaderCount("row", "row", frozenRows);
        assertFrozenHeaderCount("column", "col", frozenColumns);
        Assert.assertEquals("First regular cell not where it's supposed to be,",
                "regular", getCellValue(regularCell));
    }

    /**
     * Count the header cells drawn in a frozen strip, skipping the ones a
     * hidden row or column collapses to nothing.
     * <p>
     * The row and column headers have a frozen strip each, so the two are
     * counted separately.
     */
    private void assertFrozenHeaderCount(String element, String axis,
            int expected) {
        Long count = (Long) ((org.openqa.selenium.JavascriptExecutor) getDriver())
                .executeScript("""
                        const [element, axis] = [arguments[1], arguments[2]];
                        const header = arguments[0].shadowRoot.querySelector(
                            `vaadin-spreadsheet-internal-${element}-header`);
                        const strip = header && header.shadowRoot
                            .querySelector('.frozen-header-strip');
                        if (!strip) return 0;
                        const cells = strip.querySelectorAll(
                            `.${axis}-header-cell`);
                        let visible = 0;
                        for (const cell of cells) {
                            const style = getComputedStyle(cell);
                            if (style.display === 'none') continue;
                            if (parseFloat(style.height) === 0) continue;
                            if (parseFloat(style.width) === 0) continue;
                            visible++;
                        }
                        return visible;
                        """, getSpreadsheet(), element, axis);

        Assert.assertEquals(
                String.format("Unexpected frozen %s-header count,", axis),
                expected, count.intValue());
    }
}
