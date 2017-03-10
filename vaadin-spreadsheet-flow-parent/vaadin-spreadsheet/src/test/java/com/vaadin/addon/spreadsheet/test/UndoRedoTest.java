package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.google.common.base.Predicate;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.By;
import com.vaadin.testbench.parallel.Browser;

public class UndoRedoTest extends AbstractSpreadsheetTestCase {

    @Rule
    public ErrorCollector collector = new ErrorCollector();
    private SheetController sheetController;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sheetController = new SheetController(driver, testBench(driver),
                getDesiredCapabilities());
    }

    @Test
    public void undo_cellValueIsSetAndUndone_cellHasNoValue() {
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");

        undo();

        assertEquals("", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void redo_cellValueIsSetAndUndoneAndRedone_cellHasValue() {
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");
        undo();

        redo();

        assertEquals("a", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void undo_cellValuesHasDeletedAndUndone_cellsHaveValue() {
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getCellAt("A2").setValue("b");
        deleteValueFromA1andA2(spreadsheet);

        undo();

        String selectionValue = String.format("A1=%s, A2=%s", spreadsheet.getCellAt("A1").getValue(),
                spreadsheet.getCellAt("A2").getValue());
        assertEquals("A1=a, A2=b", selectionValue);
    }

    @Test
    public void undo_cellValuesHasDeletedAndUndoneAndRedone_cellsHasNoValue() {
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getCellAt("A2").setValue("b");
        deleteValueFromA1andA2(spreadsheet);
        undo();

        redo();

        String selectionValue = String.format("A1=%s, A2=%s", spreadsheet.getCellAt("A1").getValue(),
                spreadsheet.getCellAt("A2").getValue());
        assertEquals("A1=, A2=", selectionValue);
    }

    @Test
    public void undo_cellValuesHasDeletedAndUndoneRedoneAndUndone_cellsHaveValues() {
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getCellAt("A2").setValue("b");
        deleteValueFromA1andA2(spreadsheet);
        undo();
        redo();

        undo();

        String selectionValue = String.format("A1=%s, A2=%s", spreadsheet.getCellAt("A1").getValue(),
                spreadsheet.getCellAt("A2").getValue());
        assertEquals("A1=a, A2=b", selectionValue);
    }

    @Test
    public void undo_addRowAndUndone_addedRowIsRemoved() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Insert new row").click();

        undo();

        assertEquals("a", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void redo_addRowAndUndoneAndRedo_rowIsAdded() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Insert new row").click();
        undo();

        redo();

        assertEquals("", spreadsheet.getCellAt("A1").getValue());
        assertEquals("a", spreadsheet.getCellAt("A2").getValue());
    }

    @Test
    public void undo_removeRowAndUndone_removedRowIsAdded() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();

        undo();

        assertEquals("a", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void redo_removeRowAndUndoneAndRedo_rowIsRemoved() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();
        undo();

        redo();

        assertEquals("", spreadsheet.getCellAt("A1").getValue());
    }


    @Test
    public void undo_removeRowWithCommentAndUndo_cellStillHasComment() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        headerPage.loadFile("cell_comments.xlsx", this); // A1 has a comment
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();

        undo();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A1").hasCommentIndicator();
            }
        });
    }

    @Test
    public void undo_userAddsCommentAndRemovesTheRowAndUndo_cellStillHasComment() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").contextClick();
        spreadsheet.getContextMenu().getItem("Insert comment").click();
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();

        undo();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A1").hasCommentIndicator();
            }
        });
    }


    @Test
    public void undo_removeRowsWithStyledCellsAndUndo_cellsHaveStyles() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        headerPage.loadFile("spreadsheet_styles.xlsx", this); // differently styled cells on rows 2-5
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        deleteRow(spreadsheet, 2);
        deleteRow(spreadsheet, 2);
        deleteRow(spreadsheet, 2);
        deleteRow(spreadsheet, 2);

        undo();
        undo();
        undo();
        undo();

        assertCorrectCss(spreadsheet);
    }

    @Test
    public void undo_addRowWithDateAndUndone_dateIsVisible() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);

        String expectedDate = "11/11/11";
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue(expectedDate);
        deleteRow(spreadsheet, 1);

        undo();
        assertEquals(expectedDate, spreadsheet.getCellAt("A1").getValue());
    }


    @Test
    public void undo_theSecondRowWithMergedCellIsRemovedAndUndo_cellIsMerged() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        new Actions(driver).clickAndHold(spreadsheet.getCellAt("A2"))
                .release(spreadsheet.getCellAt("B2")).perform();
        spreadsheet.getCellAt("A2").contextClick();
        spreadsheet.getContextMenu().getItem("Merge cells").click();
        deleteRow(spreadsheet, 2);

        undo();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.findElement(By.cssSelector(".col1.row2.merged-cell")).isDisplayed();
            }
        });
    }

    @Ignore("This is a known issue which should be fixed.")
    @Test
    public void undo_theSecondRowWithInvalidFormulaIsRemovedAndUndo_formulaIndicatorIsPresent() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A2").setValue("=a");
        deleteRow(spreadsheet, 2);

        undo();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A2").hasInvalidFormulaIndicator();
            }
        });
    }

    @Ignore("This is a known issue which should be fixed.")
    @Test
    public void undo_conditionalFormattedCellsRemovedAndUndo_cellsAreStillConditionallyFormatted() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        headerPage.loadFile("conditional_formatting.xlsx", this);
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        deleteRow(spreadsheet, 1);

        undo();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "rgba(255, 199, 206, 1)".equals(spreadsheet.getCellAt("B1").getCssValue("background-color"));
            }
        });
    }

    @Test
    public void undo_pasteRegionThenUndo_cellsHaveInitialValues() {
        final SpreadsheetElement spreadsheet = setupSpreadSheetForRegionCopyPasteTest();
        copy();
        sheetController.clickCell("D1");

        paste();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("2");
            }
        });
        undo();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("4");
            }
        });

        String selectionValue = String.format("D1=%s, E1=%s", spreadsheet.getCellAt("D1").getValue(),
                spreadsheet.getCellAt("E1").getValue());
        assertEquals("D1=3, E1=4", selectionValue);
    }

    @Test
    public void undo_pasteRegionThenUndoAndRedo_cellsHavePastedValues() {
        final SpreadsheetElement spreadsheet = setupSpreadSheetForRegionCopyPasteTest();
        copy();
        sheetController.clickCell("D1");

        paste();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("2");
            }
        });
        undo();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("4");
            }
        });
        redo();
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("2");
            }
        });

        String selectionValue = String.format("D1=%s, E1=%s", spreadsheet.getCellAt("D1").getValue(),
                spreadsheet.getCellAt("E1").getValue());
        assertEquals("D1=1, E1=2", selectionValue);
    }

    private SpreadsheetElement setupSpreadSheetForRegionCopyPasteTest() {
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("1");
        spreadsheet.getCellAt("B1").setValue("2");
        spreadsheet.getCellAt("D1").setValue("3");
        spreadsheet.getCellAt("E1").setValue("4");
        sheetController.selectRegion("A1", "B1");
        return spreadsheet;
    }

    /**
     * Deletes the row from spreadsheet using 'Delete row' action in context
     * menu
     * <p>
     * Does not work with PhantomJS or Firefox
     *
     * @param spreadsheet
     * @param row
     */
    private void deleteRow(SpreadsheetElement spreadsheet, int row) {
        spreadsheet.getRowHeader(row).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();
    }

    private void deleteValueFromA1andA2(SpreadsheetElement spreadsheet) {
        spreadsheet.getCellAt("A1").click();
        new Actions(getDriver()).sendKeys(Keys.chord(Keys.SHIFT, Keys.DOWN),
                Keys.DELETE)
                .build().perform();
    }


    private void redo() {
        new Actions(getDriver())
                .sendKeys(Keys.chord(Keys.CONTROL, "y")).build().perform();
    }

    private void undo() {
        new Actions(getDriver())
                .sendKeys(Keys.chord(Keys.CONTROL, "z")).build().perform();
    }

    private void paste() {
        new Actions(getDriver())
                .sendKeys(Keys.chord(Keys.CONTROL, "v")).build().perform();
        getCommandExecutor().waitForVaadin();
    }

    private void copy() {
        new Actions(getDriver())
                .sendKeys(Keys.chord(Keys.CONTROL, "c")).build().perform();
    }

    private void assertCorrectCss(SpreadsheetElement c) {
        collector.checkThat(c.getCellAt("A2").getCssValue("text-align"),
                equalTo("center"));

        collector.checkThat(c.getCellAt("B2").getCssValue("text-align"),
                equalTo("right"));

        collector.checkThat(c.getCellAt("A3").getCssValue("border-bottom-color"),
                equalTo("rgba(0, 0, 255, 1)"));
        collector.checkThat(c.getCellAt("A3").getCssValue("border-bottom-style"),
                equalTo("solid"));
        collector.checkThat(c.getCellAt("A3").getCssValue("border-bottom-width"),
                equalTo("4px"));

        collector.checkThat(c.getCellAt("B3").getCssValue("background-color"),
                equalTo("rgba(0, 128, 0, 1)"));

        collector.checkThat(c.getCellAt("A4").getCssValue("color"),
                equalTo("rgba(255, 0, 0, 1)"));

        collector.checkThat(c.getCellAt("C4").getCssValue("font-style"),
                equalTo("italic"));

        collector.checkThat(
                (int) Math.ceil(parseSize(c.getCellAt("A5").getCssValue("font-size"))),
                equalTo(11));
        collector.checkThat(
                (int) Math.ceil(parseSize(c.getCellAt("B5").getCssValue("font-size"))),
                equalTo(14));
        collector.checkThat(
                (int) Math.ceil(parseSize(c.getCellAt("C5").getCssValue("font-size"))),
                equalTo(16));
        collector.checkThat(
                (int) Math.ceil(parseSize(c.getCellAt("D5").getCssValue("font-size"))),
                equalTo(19));

        if (getDesiredCapabilities().getBrowserName()
                .equalsIgnoreCase("chrome")) {
            collector.checkThat(c.getCellAt("B4").getCssValue("font-weight"), equalTo("bold"));
        } else {
            collector
                    .checkThat(c.getCellAt("B4").getCssValue("font-weight"), equalTo("700"));
        }
    }

    private double parseSize(String size) {
        return Double.parseDouble(size.replaceAll("[^.0-9]", ""));
    }
}
