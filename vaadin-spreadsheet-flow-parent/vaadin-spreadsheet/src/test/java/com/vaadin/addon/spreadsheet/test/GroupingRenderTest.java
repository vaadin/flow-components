package com.vaadin.addon.spreadsheet.test;

import org.junit.Test;

public class GroupingRenderTest extends AbstractSpreadsheetTestCase {

    /**
     * SHEET-77
     */
    @Test
    public void testRenderingLargeGrouping() throws Exception {

        headerPage.loadFile("large-grouped.xlsx", this);
        compareScreen("grouping_render_large");
    }
}
