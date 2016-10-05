package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

public class MergedCellFrozenTest extends AbstractSpreadsheetTestCase {

    @Test
    public void positioning_spreadsheetWithMergedCellsNextToFrozenArea_correctPositionAfterResize()
            throws InterruptedException {

        headerPage.loadFile("frozen_merged.xlsx", this);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        String cellText = "Merged cells in frozen area";

        SheetCellElement d2 = spreadsheet.getCellAt("D2");
        assertThat(d2.getValue(), equalTo(cellText));

        String left = d2.getCssValue("left");
        assertThat(left, equalTo("0px"));

        testBench(getDriver()).resizeViewPortTo(SCREENSHOT_WIDTH / 2,
                SCREENSHOT_HEIGHT / 2);

        Thread.sleep(1000);

        testBench(getDriver()).resizeViewPortTo(SCREENSHOT_WIDTH,
                SCREENSHOT_HEIGHT);

        Thread.sleep(1000);

        d2 = spreadsheet.getCellAt("D2");
        assertThat(d2.getValue(), equalTo(cellText));

        left = d2.getCssValue("left");
        assertThat(left, equalTo("0px"));
    }

    @Test
    public void positioning_spreadsheetWithMergedCellsFurtherOutsideFrozenArea_correctPositionAfterResize()
            throws InterruptedException {

        headerPage.loadFile("freezepanes_merged.xlsx", this);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        String cellText = "5";

        SheetCellElement e8 = spreadsheet.getCellAt("E8");
        assertThat(e8.getValue(), equalTo(cellText));

        String originalLeft = e8.getCssValue("left");

        testBench(getDriver()).resizeViewPortTo(SCREENSHOT_WIDTH / 2,
                SCREENSHOT_HEIGHT / 2);

        Thread.sleep(1000);

        testBench(getDriver()).resizeViewPortTo(SCREENSHOT_WIDTH,
                SCREENSHOT_HEIGHT);

        Thread.sleep(1000);

        e8 = spreadsheet.getCellAt("E8");
        assertThat(e8.getValue(), equalTo(cellText));

        assertThat(e8.getCssValue("left"), equalTo(originalLeft));
    }

}
