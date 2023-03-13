package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-spreadsheet")
public class RemoveInsertRowIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void removeRow_theFirstCellHasInvalidFormula_formulaIndicatorIsRemoved() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("=a");

        deleteFirstRow(spreadsheet);

        Assert.assertFalse(
                spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator());
    }

    @Test
    public void removeRow_theSecondRowCellHasInvalidFormula_formulaIndicatorIsRemoved() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A2").setValue("=a");

        deleteRow(spreadsheet, 2);

        Assert.assertTrue(findElementsInShadowRoot(
                By.className("cell-invalidformula-triangle")).isEmpty());
    }

    @Test
    public void insertRow_theFirstCellHasInvalidFormula_theInvalidFormulaIsMovedToNextRow() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("=a");

        insertNewFirstRow(spreadsheet);

        Assert.assertTrue(
                spreadsheet.getCellAt("A2").hasInvalidFormulaIndicator());
    }

    @Test
    public void removeRow_theSecondRowHasInvalidFormulaCell_formulaIndicatorIsMovedUp() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A2").setValue("=a");

        deleteFirstRow(spreadsheet);

        Assert.assertTrue(
                spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator());
    }

    @Test
    public void removeRow_theSecondAndThirdRowHasInvalidFormulaCell_formulaIndicatorIsMovedUp() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A2").setValue("=a");
        spreadsheet.getCellAt("A3").setValue("=a");

        deleteFirstRow(spreadsheet);

        Assert.assertTrue(
                spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator());
        Assert.assertTrue(
                spreadsheet.getCellAt("A2").hasInvalidFormulaIndicator());
    }

    @Test
    public void insertRow_theFirstAndSecondRowHasInvalidFormulaCellAndTheRowIsAddedBetween_theFirstAndThirdRowHasErrorIndicator() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("=a");
        spreadsheet.getCellAt("A2").setValue("=a");

        insertRow(spreadsheet, 2);

        Assert.assertTrue(
                spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator());
        Assert.assertFalse(
                spreadsheet.getCellAt("A2").hasInvalidFormulaIndicator());
        Assert.assertTrue(
                spreadsheet.getCellAt("A3").hasInvalidFormulaIndicator());
    }

    @Test
    public void removeRow_theFirstRowHasInvalidFormulaCellAndTheSecondRowIsRemoved_theFirstRowHasErrorIndicator() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("=a");

        deleteRow(spreadsheet, 2);

        Assert.assertTrue(
                spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator());
    }

    @Test
    public void removeRow_theFirstCellHasMergedCell_thereIsNoMergedCells() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        new Actions(driver).clickAndHold(spreadsheet.getCellAt("A1"))
                .release(spreadsheet.getCellAt("B1")).perform();
        spreadsheet.getCellAt("A1").contextClick();
        spreadsheet.getContextMenu().getItem("Merge cells").click();

        deleteFirstRow(spreadsheet);

        Assert.assertTrue(
                findElementsInShadowRoot(By.cssSelector(".merged-cell"))
                        .isEmpty());
    }

    @Test
    public void insertRow_theFirstCellHasMergedCell_theMergedCellIsMovedToNextRow() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        new Actions(driver).clickAndHold(spreadsheet.getCellAt("A1"))
                .release(spreadsheet.getCellAt("B1")).perform();
        spreadsheet.getCellAt("A1").contextClick();
        spreadsheet.getContextMenu().getItem("Merge cells").click();

        insertNewFirstRow(spreadsheet);

        Assert.assertTrue(findElementInShadowRoot(
                By.cssSelector(".col1.row2.merged-cell")).isDisplayed());
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3223 is fixed")
    @Test
    public void removeRow_theFirstCellHasPopupButton_thereIsNoPopupButtons() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        clickCell("B1");
        loadTestFixture(TestFixtures.PopupButton);
        clickCell("A1");

        deleteFirstRow(spreadsheet);

        Assert.assertFalse(spreadsheet.getCellAt("A1").hasPopupButton());
    }

    @Test
    public void removeRow_theSecondRowCellHasPopupButton_thereIsNoPopupButtons() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        clickCell("B1");
        loadTestFixture(TestFixtures.PopupButton);
        clickCell("A2");

        deleteRow(spreadsheet, 2);

        Assert.assertTrue(findElementsInShadowRoot(By.className("popupbutton"))
                .isEmpty());
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3223 is fixed")
    @Test
    public void removeRow_theSecondRowCellHasPopupButton_popupButtonIsMovedUp() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        clickCell("B1");
        loadTestFixture(TestFixtures.PopupButton);
        clickCell("A2");

        deleteFirstRow(spreadsheet);

        Assert.assertTrue(spreadsheet.getCellAt("A1").hasPopupButton());
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3223 is fixed")
    @Test
    public void insertRow_theFirstCellHasPopupButton_thePopupButtonIsMovedToNextRow() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        clickCell("B1");
        loadTestFixture(TestFixtures.PopupButton);
        clickCell("A1");

        insertNewFirstRow(spreadsheet);

        Assert.assertTrue(spreadsheet.getCellAt("A2").hasPopupButton());
    }

    @Ignore("This is a known issue which should be fixed. The problem is most likely on client side.")
    @Test
    public void insertRow_theFirstAndSecondRowHasPopupbuttonsNewRowIsAddedToFirstRow_theSecondAndThirdRowHasPopupButton() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        clickCell("B1");
        loadTestFixture(TestFixtures.PopupButton);
        clickCell("A1");
        clickCell("A2");

        insertNewFirstRow(spreadsheet);

        Assert.assertFalse(spreadsheet.getCellAt("A1").hasPopupButton());
        Assert.assertTrue(spreadsheet.getCellAt("A2").hasPopupButton());
        Assert.assertTrue(spreadsheet.getCellAt("A3").hasPopupButton());
    }

    private void insertNewFirstRow(SpreadsheetElement spreadsheet) {
        insertRow(spreadsheet, 1);
    }

    private void insertRow(SpreadsheetElement spreadsheet, int row) {
        clickCell("A" + row);
        loadTestFixture(TestFixtures.InsertRow);
    }

    private void deleteFirstRow(SpreadsheetElement spreadsheet) {
        deleteRow(spreadsheet, 1);
    }

    private void deleteRow(SpreadsheetElement spreadsheet, int row) {
        clickCell("A" + row);
        loadTestFixture(TestFixtures.DeleteRow);
    }
}
