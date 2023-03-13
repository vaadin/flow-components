package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

@TestPath("vaadin-spreadsheet")
public class UndoRedoIT extends AbstractSpreadsheetIT {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void undo_cellValueIsSetAndUndone_cellHasNoValue() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");

        undo();

        Assert.assertEquals("", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void redo_cellValueIsSetAndUndoneAndRedone_cellHasValue() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");
        undo();

        redo();

        Assert.assertEquals("a", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void undo_cellValuesHasDeletedAndUndone_cellsHaveValue() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getCellAt("A2").setValue("b");
        deleteValueFromA1andA2(spreadsheet);

        undo();

        String selectionValue = String.format("A1=%s, A2=%s",
                spreadsheet.getCellAt("A1").getValue(),
                spreadsheet.getCellAt("A2").getValue());
        Assert.assertEquals("A1=a, A2=b", selectionValue);
    }

    @Test
    public void undo_cellValuesHasDeletedAndUndoneAndRedone_cellsHasNoValue() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getCellAt("A2").setValue("b");
        deleteValueFromA1andA2(spreadsheet);
        undo();

        redo();

        String selectionValue = String.format("A1=%s, A2=%s",
                spreadsheet.getCellAt("A1").getValue(),
                spreadsheet.getCellAt("A2").getValue());
        Assert.assertEquals("A1=, A2=", selectionValue);
    }

    @Test
    public void undo_cellValuesHasDeletedAndUndoneRedoneAndUndone_cellsHaveValues() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getCellAt("A2").setValue("b");
        deleteValueFromA1andA2(spreadsheet);
        undo();
        redo();

        undo();

        String selectionValue = String.format("A1=%s, A2=%s",
                spreadsheet.getCellAt("A1").getValue(),
                spreadsheet.getCellAt("A2").getValue());
        Assert.assertEquals("A1=a, A2=b", selectionValue);
    }

    @Test
    public void undo_addRowAndUndone_addedRowIsRemoved() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Insert new row").click();

        undo();

        Assert.assertEquals("a", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void redo_addRowAndUndoneAndRedo_rowIsAdded() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Insert new row").click();
        undo();

        redo();

        Assert.assertEquals("", spreadsheet.getCellAt("A1").getValue());
        Assert.assertEquals("a", spreadsheet.getCellAt("A2").getValue());
    }

    @Test
    public void undo_removeRowAndUndone_removedRowIsAdded() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();

        undo();

        Assert.assertEquals("a", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void redo_removeRowAndUndoneAndRedo_rowIsRemoved() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("a");
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();
        undo();

        redo();

        Assert.assertEquals("", spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void undo_removeRowWithCommentAndUndo_cellStillHasComment() {
        loadFile("cell_comments.xlsx"); // A1 has a comment
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();

        undo();

        waitUntil(
                webDriver -> spreadsheet.getCellAt("A1").hasCommentIndicator());
    }

    @Test
    public void undo_userAddsCommentAndRemovesTheRowAndUndo_cellStillHasComment() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").contextClick();
        spreadsheet.getContextMenu().getItem("Insert comment").click();
        spreadsheet.getRowHeader(1).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();

        undo();

        waitUntil(
                webDriver -> spreadsheet.getCellAt("A1").hasCommentIndicator());
    }

    @Test
    public void undo_removeRowsWithStyledCellsAndUndo_cellsHaveStyles() {
        loadFile("spreadsheet_styles.xlsx"); // differently styled cells on rows
                                             // 2-5
        final SpreadsheetElement spreadsheet = getSpreadsheet();
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

        String expectedDate = "11/11/11";
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue(expectedDate);
        deleteRow(spreadsheet, 1);

        undo();
        Assert.assertEquals(expectedDate,
                spreadsheet.getCellAt("A1").getValue());
    }

    @Test
    public void undo_theSecondRowWithMergedCellIsRemovedAndUndo_cellIsMerged() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        new Actions(driver).clickAndHold(spreadsheet.getCellAt("A2"))
                .release(spreadsheet.getCellAt("B2")).perform();
        spreadsheet.getCellAt("A2").contextClick();
        spreadsheet.getContextMenu().getItem("Merge cells").click();
        deleteRow(spreadsheet, 2);

        undo();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return findElementInShadowRoot(
                        By.cssSelector(".col1.row2.merged-cell")).isDisplayed();
            }
        });
    }

    @Ignore("This is a known issue which should be fixed: https://github.com/vaadin/spreadsheet/issues/326")
    @Test
    public void undo_theSecondRowWithInvalidFormulaIsRemovedAndUndo_formulaIndicatorIsPresent() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
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

    @Ignore("This is a known issue which should be fixed: https://github.com/vaadin/spreadsheet/issues/331")
    @Test
    public void undo_conditionalFormattedCellsRemovedAndUndo_cellsAreStillConditionallyFormatted() {
        loadFile("conditional_formatting.xlsx");
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        deleteRow(spreadsheet, 1);

        undo();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "rgba(255, 199, 206, 1)".equals(spreadsheet
                        .getCellAt("B1").getCssValue("background-color"));
            }
        });
    }

    @Test
    public void undo_pasteRegionThenUndo_cellsHaveInitialValues() {
        final SpreadsheetElement spreadsheet = setupSpreadSheetForRegionCopyPasteTest();
        copy();
        clickCell("D1");

        paste();
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("2");
            }
        });
        undo();
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("4");
            }
        });

        String selectionValue = String.format("D1=%s, E1=%s",
                spreadsheet.getCellAt("D1").getValue(),
                spreadsheet.getCellAt("E1").getValue());
        Assert.assertEquals("D1=3, E1=4", selectionValue);
    }

    @Test
    public void undo_pasteRegionThenUndoAndRedo_cellsHavePastedValues() {
        final SpreadsheetElement spreadsheet = setupSpreadSheetForRegionCopyPasteTest();
        copy();
        clickCell("D1");

        paste();
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("2");
            }
        });
        undo();
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("4");
            }
        });
        redo();
        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E1").getValue().equals("2");
            }
        });

        String selectionValue = String.format("D1=%s, E1=%s",
                spreadsheet.getCellAt("D1").getValue(),
                spreadsheet.getCellAt("E1").getValue());
        Assert.assertEquals("D1=1, E1=2", selectionValue);
    }

    private SpreadsheetElement setupSpreadSheetForRegionCopyPasteTest() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("1");
        spreadsheet.getCellAt("B1").setValue("2");
        spreadsheet.getCellAt("D1").setValue("3");
        spreadsheet.getCellAt("E1").setValue("4");
        selectRegion("A1", "B1");
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
        dragFromCellToCell("A1", "A2");
        new Actions(getDriver()).sendKeys(Keys.DELETE).build().perform();
    }

    private void assertCorrectCss(SpreadsheetElement c) {
        Assert.assertEquals(c.getCellAt("A2").getCssValue("text-align"),
                "center");

        Assert.assertEquals(c.getCellAt("B2").getCssValue("text-align"),
                "right");

        Assert.assertEquals(
                c.getCellAt("A3").getCssValue("border-bottom-color"),
                "rgba(0, 0, 255, 1)");
        Assert.assertEquals(
                c.getCellAt("A3").getCssValue("border-bottom-style"), "solid");
        Assert.assertEquals(
                c.getCellAt("A3").getCssValue("border-bottom-width"), "4px");

        Assert.assertEquals(c.getCellAt("B3").getCssValue("background-color"),
                "rgba(0, 128, 0, 1)");

        Assert.assertEquals(c.getCellAt("A4").getCssValue("color"),
                "rgba(255, 0, 0, 1)");

        Assert.assertEquals(c.getCellAt("C4").getCssValue("font-style"),
                "italic");

        Assert.assertEquals(
                (int) Math.ceil(
                        parseSize(c.getCellAt("A5").getCssValue("font-size"))),
                11);
        Assert.assertEquals(
                (int) Math.ceil(
                        parseSize(c.getCellAt("B5").getCssValue("font-size"))),
                14);
        Assert.assertEquals(
                (int) Math.ceil(
                        parseSize(c.getCellAt("C5").getCssValue("font-size"))),
                16);
        Assert.assertEquals(
                (int) Math.ceil(
                        parseSize(c.getCellAt("D5").getCssValue("font-size"))),
                19);

        Assert.assertEquals(c.getCellAt("B4").getCssValue("font-weight"),
                "700");
    }

    private double parseSize(String size) {
        return Double.parseDouble(size.replaceAll("[^.0-9]", ""));
    }
}
