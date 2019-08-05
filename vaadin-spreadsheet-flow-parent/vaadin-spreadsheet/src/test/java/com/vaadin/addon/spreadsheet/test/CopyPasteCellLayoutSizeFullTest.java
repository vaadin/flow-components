package com.vaadin.addon.spreadsheet.test;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.demoapps.EmptySpreadsheetSizeFullUI;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.annotations.RunLocally;
import com.vaadin.testbench.parallel.Browser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;


public class CopyPasteCellLayoutSizeFullTest extends AbstractSpreadsheetTestCase {

    @Override
    protected Class<?> getUIClass() {
        return EmptySpreadsheetSizeFullUI.class;
    }

    @Rule
    public ErrorCollector collector = new ErrorCollector();
    private SheetController sheetController;


    @Override
    public void setUp() throws Exception {
        setDebug(true);
        super.setUp();
        sheetController = new SheetController(driver, testBench(driver),
                getDesiredCapabilities());
    }

    @Test
    public void spreadsheetHandlerOnPaste_PasteCellsWhichOtherCellsDependingOn_UpdatesDependentCells() {
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("1");
        spreadsheet.getCellAt("A2").setValue("2");
        spreadsheet.getCellAt("A3").setValue("3");
        spreadsheet.getCellAt("B3").setValue("=A3+1");
        spreadsheet.getCellAt("E4").setValue("=E3+10");

        copyPasteRegion("A3", "B3", "D3", false);
        final String expectedValue = "14";
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E4").getValue().equals(expectedValue);
            }
        });
    }

    private void copyPasteRegion(String startCopyCell, String endCopyCell, String pasteStartCell, boolean clearLog) {
        sheetController.selectRegion(startCopyCell, endCopyCell);
        copy();
        sheetController.clickCell(pasteStartCell);
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return "D3".equals(sheetController.getSelectedCell());
            }
        });
        if (clearLog) {
            clearLog();
        }
        paste();
    }


    private void paste() {
        new Actions(getDriver())
                .sendKeys(Keys.chord(Keys.CONTROL, "v")).build().perform();
    }

    private void copy() {
        new Actions(getDriver())
                .sendKeys(Keys.chord(Keys.CONTROL, "c")).build().perform();
    }

}
