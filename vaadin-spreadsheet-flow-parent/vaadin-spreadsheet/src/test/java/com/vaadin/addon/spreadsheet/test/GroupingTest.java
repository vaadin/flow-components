package com.vaadin.addon.spreadsheet.test;

import org.junit.Test;

public class GroupingTest extends AbstractSpreadsheetTestCase {

    /**
     * Ticket #18546
     *
     * Note that this screenshot tests different themes so hence page must be
     * loaded between screenshot comparisons.
     */
    @Test
    public void testGroupingStyle() throws Exception {
        loadPage("demo-reindeer", "Groupingtest.xlsx");
        compareScreen("grouping_styling_legacy");

        loadPage("demo", "Groupingtest.xlsx");
        compareScreen("grouping_styling_demo");
    }
}
