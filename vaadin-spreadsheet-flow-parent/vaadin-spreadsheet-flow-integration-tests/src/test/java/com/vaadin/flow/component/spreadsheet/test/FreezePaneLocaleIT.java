/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

    @Test
    @Ignore("https://github.com/vaadin/flow-components/issues/4737")
    public void hideFirstSheet_borderStylesUpdated() {
        setSpreadsheet($(SpreadsheetElement.class).first());

        Assert.assertNotEquals("1px",
                getCellStyle("B5", "border-bottom-width"));

        getCellAt(1, 1).contextClick();
        clickItem("Hide sheet");

        Assert.assertEquals("1px", getCellStyle("B5", "border-bottom-width"));
    }

}
