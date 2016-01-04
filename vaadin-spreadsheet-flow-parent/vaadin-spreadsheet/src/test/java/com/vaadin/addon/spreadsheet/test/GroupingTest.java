package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.By;

import org.junit.Test;
import org.openqa.selenium.WebElement;

public class GroupingTest extends AbstractSpreadsheetTestCase {

    /**
     * Ticket #18546
     *
     * Note that this screenshot tests different themes so hence page must be
     * loaded between screenshot comparisons.
     */
    @Test
    public void grouping_themeHasChanged_theSpreadsheetIsRenderedCorrectly() throws Exception {
        loadPage("demo-reindeer", "Groupingtest.xlsx");
        compareScreen("grouping_styling_legacy");

        loadPage("demo", "Groupingtest.xlsx");
        compareScreen("grouping_styling_demo");
    }

    /**
     * Ticket #18912
     */
    @Test
    public void grouping_headersAreHidden_groupingElementsHaveCorrectSize() throws Exception {
        loadPage("demo", "grouping_without_headers.xlsx");

        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();

        WebElement colGrouping = spreadsheetElement
            .findElement(By.cssSelector(".col-group-pane .grouping.minus"));
        WebElement rowGrouping = spreadsheetElement
            .findElement(By.cssSelector(".row-group-pane .grouping.minus"));
        assertEquals(280, colGrouping.getSize().getWidth());
        assertEquals(110, rowGrouping.getSize().getHeight());
    }
}
