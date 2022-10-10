package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;

import org.junit.Assert;
import org.junit.Test;

public class FreezePaneLocaleUITest extends AbstractSpreadsheetIT {

    @Test
    public void loadSpreadsheetWithFrozenColumns_setLocaleAfterLoad_SpreadsheetLoadedCorrectly() {
        // the UI does the work for this, we just have to verify it loaded
        getDriver().get(getBaseURL() + "/freeze-pane-locale");

        Assert.assertTrue("Spreadsheet did not load correctly",
                $(SpreadsheetElement.class).exists());
    }

}
