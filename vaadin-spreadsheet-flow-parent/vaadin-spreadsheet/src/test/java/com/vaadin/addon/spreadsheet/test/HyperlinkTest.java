package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.addon.spreadsheet.test.testutil.PopupHelper;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.parallel.Browser;

/**
 * Tests for hyperlinks.
 *
 */
public class HyperlinkTest extends AbstractSpreadsheetTestCase {

    private PopupHelper popup;
    private SheetController sheetController;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        headerPage.loadFile("spreadsheet_hyperlinks.xlsx", this);
        popup = new PopupHelper(driver);
        sheetController = new SheetController(driver, testBench(driver),
                getDesiredCapabilities());
        testBench(driver).waitForVaadin();
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_internalFromFormulaMovesToCorrectCell() {
        testInternal("A4", "B4");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_internalFromLinkMovesToCorrectCell() {
        testInternal("A5", "B5");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_externalFromLinkOpensPopupToCorrectPage() {
        testExternal("A3");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_externalFromFormulaOpensPopupToCorrectPage() {
        testExternal("A2");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_externalFromMergedCellOpensPopupToCorrectPage() {
        testExternal("C7");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_internalFromFileNameFormulaMovesToCorrectSheetAndCell() {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(
                "hyper_links.xlsx", this);
        testBench(driver).waitForVaadin();
        // ensure hyperlink switches to correct cell
        testInternal("B30", "B10");
        // ensure correct sheet
        testInternal("A3", "A3");
        assertEquals("Unexpected formula for cell A3, ", "=5000",
                spreadsheetPage.getFormulaFieldValue());
    }

    private void testInternal(String initial, String expected) {
        sheetController.clickCell(initial);
        waitUntilSelected(expected);
    }

    private void testExternal(String cell) {
        sheetController.clickCell(cell);
        popup.switchToPopup();
        waitUntilUrlContains("google");
        popup.backToMainWindow();
    }

    private void waitUntilSelected(final String expected) {
        waitUntil(new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver arg0) {
                return expected.equals(sheetController.getSelectedCell());
            }

            @Override
            public String toString() {
                // ...waiting for ...
                return expected + " to get selected";
            }
        });
    }

    private void waitUntilUrlContains(final String expected) {
        waitUntil(new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver arg0) {
                return driver.getCurrentUrl().contains(expected);
            }

            @Override
            public String toString() {
                // ...waiting for ...
                return String.format("current url to contain '%s'", expected);
            }
        });
    }

    @Override
    public List<DesiredCapabilities> getBrowsersToTest() {
        List<DesiredCapabilities> result = super.getBrowsersToTest();
        // Fails in phantomJS, probably because of being considered a touch
        // device. Enable PhantomJS after (SHEET-54)
        result.remove(Browser.PHANTOMJS.getDesiredCapabilities());
        return result;
    }

}
