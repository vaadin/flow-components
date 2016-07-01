package com.vaadin.addon.spreadsheet.test;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;

public class StyleTests extends AbstractSpreadsheetTestCase {

    private SheetController sheetController;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sheetController = new SheetController(driver, testBench(driver),
                getDesiredCapabilities());
    }

    @Test
    public void cellBorders_mergeCells_NeighborCellsKeepBorderStyles() throws IOException {
        headerPage.loadFile("merged_borders.xlsx", this);
        headerPage.loadTestFixture(TestFixtures.StyleMergeReigions);

        compareScreen("merged_borders");
    }
}
