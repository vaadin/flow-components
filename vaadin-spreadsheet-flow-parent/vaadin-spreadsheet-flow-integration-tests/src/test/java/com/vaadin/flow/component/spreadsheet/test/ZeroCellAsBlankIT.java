package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ZeroCellAsBlankIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void zeroCellAsBlank_loadSheetWithSettingOn_zeroExpected()
            throws Exception {
        loadFile("show-zero-cells.xlsx");

        selectSheetAt(0);

        assertEquals("2", getSpreadsheet().getCellAt("B2").getValue());
        assertEquals("0", getSpreadsheet().getCellAt("B3").getValue());
        assertEquals("0", getSpreadsheet().getCellAt("B4").getValue());
        assertEquals("0", getSpreadsheet().getCellAt("B5").getValue());
    }

    @Test
    public void zeroCellAsBlank_loadSheetWithSettingOn_emptyExpected()
            throws Exception {
        loadFile("show-zero-cells.xlsx");

        selectSheetAt(1);

        assertEquals("2", getSpreadsheet().getCellAt("B2").getValue());
        assertEquals("", getSpreadsheet().getCellAt("B3").getValue());
        assertEquals("0", getSpreadsheet().getCellAt("B4").getValue());
        assertEquals("", getSpreadsheet().getCellAt("B5").getValue());
    }
}
