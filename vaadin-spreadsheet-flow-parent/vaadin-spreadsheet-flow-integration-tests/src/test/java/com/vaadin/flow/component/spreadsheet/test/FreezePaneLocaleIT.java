package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;

import org.junit.Assert;
import org.junit.Test;

public class FreezePaneLocaleIT extends AbstractSpreadsheetIT {

    @Test
    public void loadSpreadsheetWithFrozenColumns_setLocaleAfterLoad_SpreadsheetLoadedCorrectly() {
        // the UI does the work for this, we just have to verify it loaded
        getDriver().get(getBaseURL() + "/freeze-pane-locale");

        Assert.assertTrue("Spreadsheet did not load correctly",
                $(SpreadsheetElement.class).exists());
    }

    @Test
    public void hideFirstSheet_borderStylesUpdated() {
        getDriver().get(getBaseURL() + "/freeze-pane-locale");
        setSpreadsheet($(SpreadsheetElement.class).first());

        Assert.assertNotEquals("1px",
                getCellStyle("B5", "border-bottom-width"));

        getCellAt(1, 1).contextClick();
        clickItem("Hide sheet");

        Assert.assertEquals("1px", getCellStyle("B5", "border-bottom-width"));
    }

}
