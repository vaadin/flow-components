package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.vaadin.testbench.parallel.Browser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.addon.spreadsheet.elements.AddressUtil;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class NavigationTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetPage spreadsheetPage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        spreadsheetPage = headerPage.createNewSpreadsheet();
    }

    @Test
    public void testClickingOnCellsUpdatesAddressFieldAndUpdatesSelection()
            throws Exception {
        spreadsheetPage.clickOnCell("A2");
        assertAddressFieldValue("A2", spreadsheetPage.getAddressFieldValue());
        assertSelectedCell("A2", spreadsheetPage.isCellSelected("A2"));

        spreadsheetPage.clickOnCell("B1");
        assertAddressFieldValue("B1", spreadsheetPage.getAddressFieldValue());
        assertSelectedCell("B1", spreadsheetPage.isCellSelected("B1"));
        assertNotSelectedCell("A2", spreadsheetPage.isCellSelected("A2"));

        spreadsheetPage.clickOnCell("D7");
        assertAddressFieldValue("D7", spreadsheetPage.getAddressFieldValue());
        assertSelectedCell("D7", spreadsheetPage.isCellSelected("D7"));
        assertNotSelectedCell("B1", spreadsheetPage.isCellSelected("B1"));

        spreadsheetPage.clickOnCell("Q15");
        assertAddressFieldValue("Q15", spreadsheetPage.getAddressFieldValue());
        assertSelectedCell("Q15", spreadsheetPage.isCellSelected("Q15"));
    }

    @Test
    public void testUpdatingAddressFieldMovesSelection() throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);
        spreadsheetPage.setAddressFieldValue("A5");
        assertSelectedCell("A5", spreadsheetPage.isCellSelected("A5"));

        spreadsheetPage.setAddressFieldValue("G10");
        assertSelectedCell("G10", spreadsheetPage.isCellSelected("G10"));

        spreadsheetPage.setAddressFieldValue("D20");
        assertSelectedCell("D20", spreadsheetPage.isCellSelected("D20"));

        spreadsheetPage.setAddressFieldValue("AC2");
        assertSelectedCell("AC2", spreadsheetPage.isCellSelected("AC2"));
    }

    @Test
    public void testDragSelection() throws Exception {
        spreadsheetPage.dragFromCellToCell("C5", "F11");
        assertSelectionRange("C5:F11", true);
    }

    @Test
    public void testKeyboardSelection() throws Exception {
        spreadsheetPage.clickOnCell("H10");
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        WebElement cell = spreadsheetPage.getCellAt(8, 10);
        new Actions(getDriver()).moveToElement(cell).keyDown(Keys.SHIFT)
                .sendKeys(Keys.RIGHT).sendKeys(Keys.RIGHT).sendKeys(Keys.DOWN)
                .sendKeys(Keys.DOWN).keyUp(Keys.SHIFT).build().perform();
        assertSelectionRange("H10:J12", true);
        assertActiveCellInsideSelection("H10");
    }

    @Test
    public void testAddressFieldUpdatesWhenDragging() throws Exception {
        new Actions(getDriver()).clickAndHold(spreadsheetPage.getCellAt(8, 10))
                .moveToElement(spreadsheetPage.getCellAt(9, 10)).build()
                .perform();
        assertAddressFieldValue("1R x 2C",
                spreadsheetPage.getAddressFieldValue());
        new Actions(getDriver())
                .moveToElement(spreadsheetPage.getCellAt(10, 10)).build()
                .perform();
        assertAddressFieldValue("1R x 3C",
                spreadsheetPage.getAddressFieldValue());
        new Actions(getDriver())
                .moveToElement(spreadsheetPage.getCellAt(10, 11)).build()
                .perform();
        assertAddressFieldValue("2R x 3C",
                spreadsheetPage.getAddressFieldValue());
        new Actions(getDriver())
                .moveToElement(spreadsheetPage.getCellAt(10, 12)).build()
                .perform();
        assertAddressFieldValue("3R x 3C",
                spreadsheetPage.getAddressFieldValue());
        new Actions(getDriver()).release().build().perform();
        assertAddressFieldValue("H10", spreadsheetPage.getAddressFieldValue());
    }

    @Test
    @Ignore("Known bug in Spreadsheet component implementation (notimetofix) - Shift selection does not update address field")
    public void testAddressFieldUpdatesWhenShiftSelecting() throws Exception {
        spreadsheetPage.clickOnCell("H10");
        WebElement cell = spreadsheetPage.getCellAt(8, 10);
        new Actions(getDriver()).moveToElement(cell).keyDown(Keys.SHIFT)
                .sendKeys(Keys.RIGHT).build().perform();
        assertAddressFieldValue("1R x 2C",
                spreadsheetPage.getAddressFieldValue());
        new Actions(getDriver()).sendKeys(Keys.RIGHT).build().perform();
        assertAddressFieldValue("1R x 3C",
                spreadsheetPage.getAddressFieldValue());
        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertAddressFieldValue("2R x 3C",
                spreadsheetPage.getAddressFieldValue());
        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertAddressFieldValue("3R x 3C",
                spreadsheetPage.getAddressFieldValue());
        new Actions(getDriver()).keyUp(Keys.SHIFT).build().perform();
        assertAddressFieldValue("H10", spreadsheetPage.getAddressFieldValue());
    }

    @Test
    public void testGrowShrinkSelectionWithShiftArrowsHorizontal()
            throws Exception {
        assertSelectionRange("G10:J10", false);
        spreadsheetPage.clickOnCell("H10");
        assertNotSelectedCell("G10", spreadsheetPage.isCellSelected("G10"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertNotSelectedCell("I10", spreadsheetPage.isCellSelected("I10"));
        assertNotSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));
        new Actions(getDriver())
                .moveToElement(spreadsheetPage.getCellAt(8, 10))
                .keyDown(Keys.SHIFT).sendKeys(Keys.RIGHT).build().perform();
        assertNotSelectedCell("G10", spreadsheetPage.isCellSelected("G10"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertSelectedCell("I10", spreadsheetPage.isCellSelected("I10"));
        assertNotSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));
        new Actions(getDriver()).sendKeys(Keys.RIGHT).build().perform();
        assertNotSelectedCell("G10", spreadsheetPage.isCellSelected("G10"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertSelectedCell("I10", spreadsheetPage.isCellSelected("I10"));
        assertSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));
        new Actions(getDriver()).sendKeys(Keys.LEFT).build().perform();
        assertNotSelectedCell("G10", spreadsheetPage.isCellSelected("G10"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertSelectedCell("I10", spreadsheetPage.isCellSelected("I10"));
        assertNotSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));
        new Actions(getDriver()).sendKeys(Keys.LEFT).build().perform();
        assertNotSelectedCell("G10", spreadsheetPage.isCellSelected("G10"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertNotSelectedCell("I10", spreadsheetPage.isCellSelected("I10"));
        assertNotSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));
        new Actions(getDriver()).sendKeys(Keys.LEFT).keyUp(Keys.SHIFT).build()
                .perform();
        assertSelectedCell("G10", spreadsheetPage.isCellSelected("G10"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertNotSelectedCell("I10", spreadsheetPage.isCellSelected("I10"));
        assertNotSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));
    }

    @Test
    public void testGrowShrinkSelectionWithShiftArrowsVertical()
            throws Exception {
        assertSelectionRange("H9:H12", false);
        spreadsheetPage.clickOnCell("H10");
        assertNotSelectedCell("H9", spreadsheetPage.isCellSelected("H9"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertNotSelectedCell("H11", spreadsheetPage.isCellSelected("H11"));
        assertNotSelectedCell("H12", spreadsheetPage.isCellSelected("H12"));
        new Actions(getDriver())
                .moveToElement(spreadsheetPage.getCellAt(8, 10))
                .keyDown(Keys.SHIFT).sendKeys(Keys.DOWN).build().perform();
        assertNotSelectedCell("H9", spreadsheetPage.isCellSelected("H9"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertSelectedCell("H11", spreadsheetPage.isCellSelected("H11"));
        assertNotSelectedCell("H12", spreadsheetPage.isCellSelected("H12"));
        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertNotSelectedCell("H9", spreadsheetPage.isCellSelected("H9"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertSelectedCell("H11", spreadsheetPage.isCellSelected("H11"));
        assertSelectedCell("H12", spreadsheetPage.isCellSelected("H12"));
        new Actions(getDriver()).sendKeys(Keys.UP).build().perform();
        assertNotSelectedCell("H9", spreadsheetPage.isCellSelected("H9"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertSelectedCell("H11", spreadsheetPage.isCellSelected("H11"));
        assertNotSelectedCell("H12", spreadsheetPage.isCellSelected("H12"));
        new Actions(getDriver()).sendKeys(Keys.UP).build().perform();
        assertNotSelectedCell("H9", spreadsheetPage.isCellSelected("H9"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertNotSelectedCell("H11", spreadsheetPage.isCellSelected("H11"));
        assertNotSelectedCell("H12", spreadsheetPage.isCellSelected("H12"));
        new Actions(getDriver()).sendKeys(Keys.UP).keyUp(Keys.SHIFT).build()
                .perform();
        assertSelectedCell("H9", spreadsheetPage.isCellSelected("H9"));
        assertSelectedCell("H10", spreadsheetPage.isCellSelected("H10"));
        assertNotSelectedCell("H11", spreadsheetPage.isCellSelected("H11"));
        assertNotSelectedCell("H12", spreadsheetPage.isCellSelected("H12"));
    }

    @Test
    public void testEnterSelectionRangeInAddress() throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);
        spreadsheetPage.setAddressFieldValue("A1:C7");
        assertSelectionRange("A1:C7", true);
    }

    @Test
    public void testEnterSelectionRangeInAddress_outsideOfViewport()
            throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);
        spreadsheetPage.setAddressFieldValue("AT1:AV7");
        assertSelectionRange("AT1:AV7", true);
    }

    @Test
    public void testKeyboardNavigation() throws Exception {
        skipBrowser("Sending multiple keys fails in IE", Browser.IE9, Browser.IE10, Browser.IE11);
        skipBrowser("Fails randomly with PhantomJS", Browser.PHANTOMJS);

        spreadsheetPage.clickOnCell("J10");
        new Actions(getDriver()).sendKeys(Keys.RIGHT).build().perform();
        assertSelectedCell("K10", spreadsheetPage.isCellSelected("K10"));

        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertSelectedCell("K11", spreadsheetPage.isCellSelected("K11"));

        new Actions(getDriver()).sendKeys(Keys.ENTER, Keys.ENTER).build()
                .perform();
        assertSelectedCell("K12", spreadsheetPage.isCellSelected("K12"));

        new Actions(getDriver()).sendKeys(Keys.LEFT).build().perform();
        assertSelectedCell("J12", spreadsheetPage.isCellSelected("J12"));

        new Actions(getDriver()).sendKeys(Keys.UP).build().perform();
        assertSelectedCell("J11", spreadsheetPage.isCellSelected("J11"));

        new Actions(getDriver()).keyDown(Keys.SHIFT)
                .sendKeys(Keys.ENTER, Keys.ENTER).keyUp(Keys.SHIFT).build()
                .perform();
        assertSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));

        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
        assertSelectedCell("K10", spreadsheetPage.isCellSelected("K10"));

        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.TAB)
                .keyUp(Keys.SHIFT).build().perform();
        assertSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));
    }

    @Test
    @Ignore("Keys.RETURN loses active position indication")
    public void testNavigationInSelectionWithEnterAndTab() throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);

        spreadsheetPage.setAddressFieldValue("A1:C2");
        // Assert that everything is selected
        assertSelectionRange("A1:C2", true);

        // Press enter/return 2 times to end up in cell B1
        assertActiveCellInsideSelection("A1");
        new Actions(getDriver()).sendKeys(Keys.ENTER, Keys.ENTER).build()
                .perform();
        assertActiveCellInsideSelection("A2");
        new Actions(getDriver()).sendKeys(Keys.ENTER, Keys.ENTER).build()
                .perform();
        assertActiveCellInsideSelection("B1");

        // Continue from B1 by pressing TAB twice, getting to A2 and shift tab
        // from there to get back to C1
        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
        assertActiveCellInsideSelection("C1");
        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
        assertActiveCellInsideSelection("A2");
        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.TAB)
                .keyUp(Keys.SHIFT).build().perform();
        assertActiveCellInsideSelection("C1");

        // Everything should still be selected
        assertSelectionRange("A1:C2", true);
    }

    @Test
    public void testRightKeyDiscardsSelection() throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);

        spreadsheetPage.setAddressFieldValue("A1:B2");
        // Assert that everything is selected
        assertSelectionRange("A1:B2", true);

        new Actions(getDriver()).sendKeys(Keys.RIGHT).build().perform();
        assertNotSelectedCell("A1", spreadsheetPage.isCellSelected("A1"));
        assertNotSelectedCell("A2", spreadsheetPage.isCellSelected("A2"));
        assertSelectedCell("B1", spreadsheetPage.isCellSelected("B1"));
        assertNotSelectedCell("B2", spreadsheetPage.isCellSelected("B2"));
    }

    @Test
    public void testDownKeyDiscardsSelection() throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);

        spreadsheetPage.setAddressFieldValue("A1:B2");
        // Assert that everything is selected
        assertSelectionRange("A1:B2", true);

        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertNotSelectedCell("A1", spreadsheetPage.isCellSelected("A1"));
        assertSelectedCell("A2", spreadsheetPage.isCellSelected("A2"));
        assertNotSelectedCell("B1", spreadsheetPage.isCellSelected("B1"));
        assertNotSelectedCell("B2", spreadsheetPage.isCellSelected("B2"));
    }

    @Test
    public void testLeftKeyDiscardsSelection() throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);
        spreadsheetPage.setAddressFieldValue("B1:C2");
        // Assert that everything is selected
        assertSelectionRange("B1:C2", true);

        new Actions(getDriver()).sendKeys(Keys.LEFT).build().perform();
        assertSelectedCell("A1", spreadsheetPage.isCellSelected("A1"));
        assertSelectionRange("B1:C2", false);
    }

    @Test
    public void testUpKeyDiscardsSelection() throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);
        spreadsheetPage.setAddressFieldValue("A2:B3");
        // Assert that everything is selected
        assertSelectionRange("A2:B3", true);

        new Actions(getDriver()).sendKeys(Keys.UP).build().perform();
        assertSelectedCell("A1", spreadsheetPage.isCellSelected("A1"));
        assertSelectionRange("A2:B3", false);
    }

    @Test
    public void testShiftArrowsShrinksSelectionWhenActiveOnEdgeOfSelection()
            throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);
        // TODO well this test is a bit incomplete
        spreadsheetPage.setAddressFieldValue("C4:E6");
        assertSelectionRange("C4:E6", true);
    }

    @Test
    public void testSheetScrollsWhenPushingAgainstRightEdge() throws Exception {
        skipBrowser("AA1 is selected instead of AB1", Browser.IE11);
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);
        spreadsheetPage.setAddressFieldValue("Z1");

        // We need to press the key two times to make it scroll.
        new Actions(getDriver()).sendKeys(Keys.RIGHT).sendKeys(Keys.RIGHT)
                .build().perform();
        assertSelectedCell("AB1", spreadsheetPage.isCellSelected("AB1"));

        // We need to press the key two times to make it scroll.
        new Actions(getDriver()).sendKeys(Keys.RIGHT).sendKeys(Keys.RIGHT)
                .build().perform();
        assertSelectedCell("AD1", spreadsheetPage.isCellSelected("AD1"));
    }

    @Test
    public void testSheetScrollsWhenPushingAgainstBottomEdge() throws Exception {
        skipBrowser("setAddressFieldValue() does not work correctly with PhantomJS", Browser.PHANTOMJS);
        skipBrowser("Sending multiple keys fails in IE sometimes", Browser.IE9, Browser.IE10, Browser.IE11);

        spreadsheetPage.setAddressFieldValue("A40");

        // We need to press the key two times to make it scroll.
        new Actions(getDriver()).sendKeys(Keys.DOWN).sendKeys(Keys.DOWN)
                .build().perform();
        assertSelectedCell("A42", spreadsheetPage.isCellSelected("A42"));

        // We need to press the key two times to make it scroll.
        new Actions(getDriver()).sendKeys(Keys.DOWN).sendKeys(Keys.DOWN)
                .build().perform();
        assertSelectedCell("A44", spreadsheetPage.isCellSelected("A44"));
    }

    @Test
    public void testEnterAndTabWhenFormulaFieldIsFocused() throws Exception {
        skipBrowser("Sending multiple keys fails in IE", Browser.IE9, Browser.IE10, Browser.IE11);

        spreadsheetPage.clickOnCell("J10");
        spreadsheetPage.clickOnFormulaField();
        spreadsheetPage.setFormulaFieldValue("2");

        spreadsheetPage.clickOnCell("J10");
        new Actions(getDriver()).sendKeys(Keys.ENTER).sendKeys(Keys.ENTER).build()
                .perform();
        assertSelectedCell("J11", spreadsheetPage.isCellSelected("J11"));
        assertCellValue("2", "J10");

        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.ENTER)
                .sendKeys(Keys.ENTER).keyUp(Keys.SHIFT).build().perform();
        assertSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));

        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
        assertSelectedCell("K10", spreadsheetPage.isCellSelected("K10"));
        assertCellValue("2", "J10");

        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.TAB)
                .keyUp(Keys.SHIFT).build().perform();
        assertSelectedCell("J10", spreadsheetPage.isCellSelected("J10"));
    }

    @Test
    public void testClickOnColumnHeaderSelectsColumn() throws Exception {
        spreadsheetPage.clickOnColumnHeader("B");
        // We can't assert all the way down to B200, because of lazy loading.
        assertSelectionRange("B1:B20", true);
        assertSelectionRange("A1:A20", false);
        assertSelectionRange("C1:C20", false);
    }

    @Test
    public void testClickOnRowHeaderSelectsRow() throws Exception {
        spreadsheetPage.clickOnRowHeader(10);
        // We can't assert all the way to AZ because of lazy loading
        assertSelectionRange("A10:L10", true);
        assertSelectionRange("A9:L9", false);
        assertSelectionRange("A11:L11", false);
        assertActiveCellInsideSelection("A10");
    }

    @Test
    public void testShiftClickShouldSelect() throws Exception {
        skipBrowser("Fails on Firefox and PhantomJS", Browser.FIREFOX, Browser.PHANTOMJS);
        spreadsheetPage.clickOnCell("B2");
        assertSelectedCell("B2", spreadsheetPage.isCellSelected("B2"));
        // new Actions(getDriver()).keyDown(Keys.SHIFT).build().perform();
        // spreadsheetPage.clickOnCell("D7");
        // new Actions(getDriver()).keyUp(Keys.SHIFT).build().perform();
        spreadsheetPage.clickOnCell("D7", Keys.SHIFT);
        assertSelectionRange("B2:D7", true);
        assertActiveCellInsideSelection("B2");
    }

    @Test
    public void testShiftClickOnColumnHeader() throws Exception {
        skipBrowser("Fails on Firefox and PhantomJS", Browser.FIREFOX, Browser.PHANTOMJS);
        spreadsheetPage.clickOnCell("B2");
        assertSelectedCell("B2", spreadsheetPage.isCellSelected("B2"));
        // new Actions(getDriver()).keyDown(Keys.SHIFT).build().perform();
        // spreadsheetPage.clickOnColumnHeader("D");
        // new Actions(getDriver()).keyUp(Keys.SHIFT).build().perform();
        spreadsheetPage.clickOnColumnHeader("D", Keys.SHIFT);
        // We can't assert the entire range because of lazy loading
        assertSelectionRange("B1:D20", true);
        assertActiveCellInsideSelection("B2");
    }

    @Test
    public void testShiftClickOnRowHeader() throws Exception {
        skipBrowser("Range selection assertion fails", Browser.FIREFOX, Browser.PHANTOMJS);

        spreadsheetPage.clickOnCell("B10");
        assertSelectedCell("B10", spreadsheetPage.isCellSelected("B10"));
        // new Actions(getDriver()).keyDown(Keys.SHIFT).build().perform();
        // spreadsheetPage.clickOnRowHeader(15);
        // new Actions(getDriver()).keyUp(Keys.SHIFT).build().perform();
        spreadsheetPage.clickOnRowHeader(15, Keys.SHIFT);
        // We can't assert the entire range because of lazy loading
        assertSelectionRange("A10:L15", true);
        assertActiveCellInsideSelection("B10");
    }

    @Test
    public void testSelectCellsByCtrlClick() throws Exception {
        skipBrowser("Fails on Firefox and PhantomJS", Browser.FIREFOX, Browser.PHANTOMJS);

        // ("only works on windows due to https://code.google.com/p/selenium/issues/detail?id=4843 (patch pending)")
        spreadsheetPage.clickOnCell("A2");
        spreadsheetPage.clickOnCell("A1");
        assertSelectedCell("A1", spreadsheetPage.isCellSelected("A1"));
        new Actions(getDriver()).keyDown(Keys.CONTROL).build().perform();
        spreadsheetPage.clickOnCell("B2");
        assertSelectedCell("A1", spreadsheetPage.isCellSelected("A1"));
        assertSelectedCell("B2", spreadsheetPage.isCellSelected("B2"));
        spreadsheetPage.clickOnCell("F12");
        new Actions(getDriver()).keyUp(Keys.CONTROL).build().perform();
        assertSelectedCell("A1", spreadsheetPage.isCellSelected("A1"));
        assertSelectedCell("B2", spreadsheetPage.isCellSelected("B2"));
        assertSelectedCell("F12", spreadsheetPage.isCellSelected("F12"));
    }

    private void assertActiveCellInsideSelection(String cellAddress) {
        assertTrue(cellAddress + " should be the active cell in selection",
                spreadsheetPage.isCellActiveWithinSelection(cellAddress));
    }

    private void assertCellValue(String expected, String cellAddress) {
        String actual = spreadsheetPage.getCellValue(cellAddress);
        assertEquals(cellAddress + " value expected " + expected
                + " but actual " + actual, expected, actual);
    }

    /**
     * Asserts that a range like A1:B3 is selected.
     *
     * @param range
     *            the range to assert
     * @param selected
     *            whether to assert that it is selected or unselected.
     */
    private void assertSelectionRange(String range, boolean selected) {
        for (Point coordinate : AddressUtil.addressRangeToPoints(range)) {
            boolean cellSelected = spreadsheetPage.isCellSelected(
                    coordinate.getX(), coordinate.getY());
            if (selected) {
                assertTrue("Expected cell at " + coordinate.toString()
                        + " to be selected because part of range " + range,
                        cellSelected);
            } else {
                assertFalse(
                        "Expected cell to NOT be selected because NOT part of range "
                                + range, cellSelected);
            }
        }
    }
}
