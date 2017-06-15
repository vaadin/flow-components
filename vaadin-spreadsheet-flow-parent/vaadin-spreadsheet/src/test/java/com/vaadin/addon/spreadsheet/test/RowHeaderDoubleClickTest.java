package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.testbench.parallel.Browser;

public class RowHeaderDoubleClickTest extends AbstractSpreadsheetTestCase {

    @Test
    public void loadFixture_doubleClickOnRowHeader_rowHeaderDoubleClickEventFired() {
        final SpreadsheetPage spreadsheetPage = headerPage
                .createNewSpreadsheet();

        headerPage.loadTestFixture(TestFixtures.RowHeaderDoubleClick);

        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class)
                .first();

        spreadsheet.getRowHeader(3).getResizeHandle().doubleClick();

        assertEquals("Double-click on row header",
                spreadsheetPage.getCellAt(1, 3).getValue());
    }

    @Override
    public List<DesiredCapabilities> getBrowsersToTest() {
        List<DesiredCapabilities> result = super.getBrowsersToTest();
        // Double click is not supported by PhantomJS.
        result.remove(Browser.PHANTOMJS.getDesiredCapabilities());
        // In manual testing Chrome works fine
        result.remove(Browser.CHROME.getDesiredCapabilities());
        return result;

    }
}
