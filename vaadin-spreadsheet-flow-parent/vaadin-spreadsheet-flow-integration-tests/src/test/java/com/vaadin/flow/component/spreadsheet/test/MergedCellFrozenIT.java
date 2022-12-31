package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat(d2.getValue(), equalTo(cellText));

        String left = d2.getCssValue("left");
        assertThat(left, equalTo("0px"));

        testBench().resizeViewPortTo(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);

        Thread.sleep(1000);

        testBench().resizeViewPortTo(WINDOW_WIDTH, WINDOW_HEIGHT);

        Thread.sleep(1000);

        d2 = spreadsheet.getCellAt("D2");
        assertThat(d2.getValue(), equalTo(cellText));

        left = d2.getCssValue("left");
        assertThat(left, equalTo("0px"));
    }

    @Test
    public void positioning_spreadsheetWithMergedCellsFurtherOutsideFrozenArea_correctPositionAfterResize()
            throws InterruptedException {

        loadFile("freezepanes_merged.xlsx");
        var spreadsheet = getSpreadsheet();

        String cellText = "5";

        var e8 = spreadsheet.getCellAt("E8");
        assertThat(e8.getValue(), equalTo(cellText));

        String originalLeft = e8.getCssValue("left");

        testBench().resizeViewPortTo(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);

        Thread.sleep(1000);

        testBench().resizeViewPortTo(WINDOW_WIDTH, WINDOW_HEIGHT);

        Thread.sleep(1000);

        e8 = spreadsheet.getCellAt("E8");
        assertThat(e8.getValue(), equalTo(cellText));

        assertThat(e8.getCssValue("left"), equalTo(originalLeft));
    }

}
