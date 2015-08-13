package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
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

    /**
     * Test for #18564 where hyperlink to same sheet caused no selection if the
     * sheet was defined in the hyperlink.
     */
    @Test
    public void hyperlink_sheetWithLinkToSameSheet_selectionIsMoved() {
        testInternal("A6", "B6");
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

    /**
     * SHEET-86. Test that hyperlinks are immediately updated.
     */
    @Test
    public void hyperlinkState_hyperlinkModified_hyperlinkUpdated()
            throws IOException {
        SpreadsheetElement sheet = $(SpreadsheetElement.class).first();
        SheetCellElement cell = sheet.getCellAt("A9");

        // Enter hyperlink and check that it is followed
        cell.setValue("=HYPERLINK(\"#Sheet1!B9\")");
        cell.click();
        waitUntilSelected("B9");

        // Clear hyperlink and check that no link is followed
        sheetController.action(Keys.LEFT);
        sheetController.action(Keys.DELETE);
        sheetController.action(Keys.DOWN);
        cell.click();
        waitUntilSelected("A9");
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
