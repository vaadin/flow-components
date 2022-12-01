package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;

@TestPath("vaadin-spreadsheet")
public class ZeroCellAsBlankIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void zeroCellAsBlank_loadSheetWithSettingOn_zeroExpected()
            throws Exception {
        loadFile("show-zero-cells.xlsx");

        selectSheetAt(0);

        Assert.assertEquals("2", getSpreadsheet().getCellAt("B2").getValue());
        Assert.assertEquals("0", getSpreadsheet().getCellAt("B3").getValue());
        Assert.assertEquals("0", getSpreadsheet().getCellAt("B4").getValue());
        Assert.assertEquals("0", getSpreadsheet().getCellAt("B5").getValue());
    }

    @Test
    public void zeroCellAsBlank_loadSheetWithSettingOn_emptyExpected()
            throws Exception {
        loadFile("show-zero-cells.xlsx");

        selectSheetAt(1);

        Assert.assertEquals("2", getSpreadsheet().getCellAt("B2").getValue());
        Assert.assertEquals("", getSpreadsheet().getCellAt("B3").getValue());
        Assert.assertEquals("0", getSpreadsheet().getCellAt("B4").getValue());
        Assert.assertEquals("", getSpreadsheet().getCellAt("B5").getValue());
    }
}
