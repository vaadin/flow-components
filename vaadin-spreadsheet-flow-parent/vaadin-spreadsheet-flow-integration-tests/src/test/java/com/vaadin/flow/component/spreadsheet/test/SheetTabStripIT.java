/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.SheetTabStripPage;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * The sheet tab strip of a workbook with more sheets than the strip has room
 * for: scrolling it, selecting sheets whose tabs are scrolled out of view, and
 * the status label at its end.
 */
@TestPath("vaadin-spreadsheet/sheet-tab-strip")
public class SheetTabStripIT extends AbstractSpreadsheetIT {

    // Visible sheets of multiple_sheets.xlsx, in order. The hidden sheet
    // between the second and the third has no tab.
    private static final String FIRST_SHEET = "Customer List";
    private static final String SECOND_SHEET = "Customers Contacts by Region";
    private static final String THIRD_SHEET = "Sheet1 loremlipsumlongertabname";
    private static final String LAST_SHEET = "Sheet3";
    private static final int LAST_SHEET_INDEX = 10;

    @Before
    public void init() {
        open();
        setSpreadsheet($(SpreadsheetElement.class).waitForSingle());
    }

    @Test
    public void scrollSheets_movesStripOneTabPerStep() {
        Assert.assertEquals(FIRST_SHEET, getShownTabs().get(0));

        getSpreadsheet().scrollSheets(2);
        Assert.assertEquals(THIRD_SHEET, getShownTabs().get(0));

        getSpreadsheet().scrollSheets(-1);
        Assert.assertEquals(SECOND_SHEET, getShownTabs().get(0));
    }

    @Test
    public void scrollSheetsToEnd_showsLastTab() {
        Assert.assertFalse(getShownTabs().contains(LAST_SHEET));

        getSpreadsheet().scrollSheetsToEnd();
        List<String> shownTabs = getShownTabs();
        Assert.assertEquals(LAST_SHEET, shownTabs.get(shownTabs.size() - 1));
        Assert.assertFalse(shownTabs.contains(FIRST_SHEET));

        // The strip is as far as it goes, so scrolling on changes nothing.
        getSpreadsheet().scrollSheets(1);
        Assert.assertEquals(shownTabs, getShownTabs());
    }

    @Test
    public void scrollSheetsToStart_showsFirstTab() {
        getSpreadsheet().scrollSheetsToEnd();
        Assert.assertNotEquals(FIRST_SHEET, getShownTabs().get(0));

        getSpreadsheet().scrollSheetsToStart();
        Assert.assertEquals(FIRST_SHEET, getShownTabs().get(0));
    }

    @Test
    public void selectSheetAt_tabScrolledOutOfView_selectsSheet() {
        getSpreadsheet().scrollSheetsToEnd();
        Assert.assertFalse(getShownTabs().contains(FIRST_SHEET));

        getSpreadsheet().selectSheetAt(0);
        assertSelectedSheet(FIRST_SHEET);

        // With the strip back at the start, the last tab is cut off at its
        // end.
        Assert.assertFalse(getShownTabs().contains(LAST_SHEET));

        getSpreadsheet().selectSheetAt(LAST_SHEET_INDEX);
        assertSelectedSheet(LAST_SHEET);
    }

    @Test
    public void selectSheet_tabScrolledOutOfView_selectsSheet() {
        getSpreadsheet().scrollSheetsToEnd();
        Assert.assertFalse(getShownTabs().contains(SECOND_SHEET));

        getSpreadsheet().selectSheet(SECOND_SHEET);
        assertSelectedSheet(SECOND_SHEET);

        Assert.assertFalse(getShownTabs().contains(LAST_SHEET));

        getSpreadsheet().selectSheet(LAST_SHEET);
        assertSelectedSheet(LAST_SHEET);
    }

    @Test
    public void selectSheet_statusLabelShown_selectsSheet() {
        clickButton("set-status-label");
        waitUntil(driver -> getSpreadsheet().getInfoLabel() != null);
        Assert.assertFalse(getShownTabs().contains(LAST_SHEET));

        getSpreadsheet().selectSheet(LAST_SHEET);
        assertSelectedSheet(LAST_SHEET);
    }

    @Test
    public void getInfoLabel_statusLabelSetAndCleared_followsStatusLabel() {
        Assert.assertNull("No status label is set yet",
                getSpreadsheet().getInfoLabel());

        clickButton("set-status-label");
        waitUntil(driver -> getSpreadsheet().getInfoLabel() != null);
        Assert.assertEquals(SheetTabStripPage.STATUS_LABEL,
                getSpreadsheet().getInfoLabel().getText());

        clickButton("clear-status-label");
        waitUntil(driver -> getSpreadsheet().getInfoLabel() == null);
    }

    /**
     * Asserts that the server has the given sheet active, and that the strip
     * marks its tab as selected and shows it whole.
     */
    private void assertSelectedSheet(String sheetName) {
        waitUntil(driver -> sheetName
                .equals($(SpanElement.class).id("active-sheet").getText()));
        Assert.assertEquals(sheetName,
                getTabStrip().$("div").withClassName("sheet-tab", "active")
                        .single().getDomAttribute("title"));
        Assert.assertTrue("The tab of " + sheetName + " should be shown",
                getShownTabs().contains(sheetName));
    }

    /**
     * The names of the tabs that the strip shows whole, from left to right. The
     * tabs show between the scroll buttons and the status label, when there is
     * one, and are cut off past that.
     */
    @SuppressWarnings("unchecked")
    private List<String> getShownTabs() {
        return (List<String>) executeScript("""
                const root = arguments[0].shadowRoot;
                const shown = root.querySelector('.sheet-tabsheet-container')
                    .getBoundingClientRect();
                const label = root.querySelector('.sheet-tabsheet-infolabel');
                const right = label
                    ? Math.min(shown.right, label.getBoundingClientRect().left)
                    : shown.right;
                return Array.from(root.querySelectorAll('.sheet-tab'))
                    .filter((tab) => {
                        const box = tab.getBoundingClientRect();
                        return box.left >= shown.left - 0.5
                            && box.right <= right + 0.5;
                    })
                    .map((tab) => tab.title);
                """, getTabStrip());
    }

    private TestBenchElement getTabStrip() {
        return getSpreadsheet().$("vaadin-spreadsheet-internal-sheet-tabs")
                .single();
    }

    private void clickButton(String id) {
        $(NativeButtonElement.class).id(id).click();
    }
}
