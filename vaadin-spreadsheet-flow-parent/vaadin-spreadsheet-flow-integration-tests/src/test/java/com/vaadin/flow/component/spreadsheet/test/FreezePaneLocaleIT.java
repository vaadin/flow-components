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

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet/freeze-pane-locale")
public class FreezePaneLocaleIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void loadSpreadsheetWithFrozenColumns_setLocaleAfterLoad_SpreadsheetLoadedCorrectly() {
        // the UI does the work for this, we just have to verify it loaded
        Assert.assertTrue("Spreadsheet did not load correctly",
                $(SpreadsheetElement.class).exists());
    }

    /**
     * Hiding a sheet brings the next sheet's cell styling with it.
     * <p>
     * The first sheet draws a black border under B5, the second sheet has
     * nothing there, so B5 falls back to the grid line. The cell has to change
     * colour when the first sheet goes away, or it keeps a border belonging to
     * a sheet nobody can see.
     * <p>
     * Expecting border-bottom-width not to be 1px before hiding could never
     * hold: every cell carries a 1px grid line. That is the failure reported in
     * <a href=
     * "https://github.com/vaadin/flow-components/issues/4737">flow-components
     * #4737</a>. The colour is what tells the two sheets apart.
     */
    @Test
    public void hideFirstSheet_borderStylesUpdated() {
        setSpreadsheet($(SpreadsheetElement.class).first());

        String gridLine = getCellStyle("E7", "border-bottom-color");
        Assert.assertNotEquals("The first sheet draws its own border under B5",
                gridLine, getCellStyle("B5", "border-bottom-color"));

        getCellAt(1, 1).contextClick();
        clickItem("Hide sheet");

        Assert.assertEquals(
                "B5 is empty on the second sheet, so it shows the grid line",
                gridLine, getCellStyle("B5", "border-bottom-color"));
    }
}
