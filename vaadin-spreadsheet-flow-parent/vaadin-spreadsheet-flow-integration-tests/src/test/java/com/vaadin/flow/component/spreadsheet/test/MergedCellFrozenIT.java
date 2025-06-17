/**
 * Copyright 2000-2025 Vaadin Ltd.
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

@TestPath("vaadin-spreadsheet")
public class MergedCellFrozenIT extends AbstractSpreadsheetIT {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 768;

    @Before
    public void init() {
        open();
    }

    @Test
    public void positioning_spreadsheetWithMergedCellsNextToFrozenArea_correctPositionAfterResize()
            throws InterruptedException {

        loadFile("frozen_merged.xlsx");
        var spreadsheet = getSpreadsheet();

        String cellText = "Merged cells in frozen area";

        var d2 = spreadsheet.getCellAt("D2");
        Assert.assertEquals(cellText, d2.getValue());

        String left = d2.getCssValue("left");
        Assert.assertEquals("0px", left);

        testBench().resizeViewPortTo(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);

        Thread.sleep(1000);

        testBench().resizeViewPortTo(WINDOW_WIDTH, WINDOW_HEIGHT);

        Thread.sleep(1000);

        d2 = spreadsheet.getCellAt("D2");
        Assert.assertEquals(cellText, d2.getValue());

        left = d2.getCssValue("left");
        Assert.assertEquals("0px", left);
    }

    @Test
    public void positioning_spreadsheetWithMergedCellsFurtherOutsideFrozenArea_correctPositionAfterResize()
            throws InterruptedException {

        loadFile("freezepanes_merged.xlsx");
        var spreadsheet = getSpreadsheet();

        String cellText = "5";

        var e8 = spreadsheet.getCellAt("E8");
        Assert.assertEquals(cellText, e8.getValue());

        String originalLeft = e8.getCssValue("left");

        testBench().resizeViewPortTo(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);

        Thread.sleep(1000);

        testBench().resizeViewPortTo(WINDOW_WIDTH, WINDOW_HEIGHT);

        Thread.sleep(1000);

        e8 = spreadsheet.getCellAt("E8");
        Assert.assertEquals(cellText, e8.getValue());

        Assert.assertEquals(originalLeft, e8.getCssValue("left"));
    }

}
