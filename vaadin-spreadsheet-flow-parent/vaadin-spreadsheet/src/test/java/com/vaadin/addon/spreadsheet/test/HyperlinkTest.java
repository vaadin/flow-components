package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.remote.DesiredCapabilities;

public class HyperlinkTest extends Test1 {
    @Test
    public void testBasicHyperlinks() {
        // FIXME on IE
        // "org.openqa.selenium.UnhandledAlertException: Modal dialog present:"
        // happens when opening the popup
        if (getDesiredCapabilities().getBrowserName().equalsIgnoreCase(
                DesiredCapabilities.internetExplorer().getBrowserName())) {
            return;
        }
        loadServerFixture("HYPERLINKS");

        sheetController.clickCell("B3");
        try {
            popup.switchToPopup();
        } catch (UnhandledAlertException uae) {
        }
        page.assertTextPresent(new String[] { "HTTP Status 404 - /file-path",
                "HTTP ERROR 404" });
        popup.backToMainWindow();

        sheetController.selectCell("B2");
        sheetController.clickCell("C3");
        Assert.assertEquals(sheetController.getCellContent("C3"), "new value");

    }

    @Test
    @Ignore("Fails in IE9 & 10, Chrome and Phantom")
    public void testFromUpload() {

        loadSheetFile("spreadsheet_hyperlinks.xlsx");

        sheetController.clickCell("A4");
        Assert.assertEquals(sheetController.getSelectedCell(), "B4");

        // FIXME on IE
        // "org.openqa.selenium.UnhandledAlertException: Modal dialog present:"
        // happens when opening the popup
        if (getDesiredCapabilities().getBrowserName().equalsIgnoreCase(
                DesiredCapabilities.internetExplorer().getBrowserName())) {
            return;
        }
        sheetController.clickCell("A3");
        try {
            popup.switchToPopup();
        } catch (UnhandledAlertException uae) {
        }
        page.assertUrlContains("google");
        popup.backToMainWindow();

        // sheetController.clickCell("A2");
        // switchToPopup();
        // assertCurrentUrlContains("google");
        // backToMainWindow();

        // sheetController.clickCell("A4");
        // Assert.assertEquals("B4", sheetController.getSelectedCell());
    }
}
