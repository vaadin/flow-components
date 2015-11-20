package com.vaadin.addon.spreadsheet.test;


import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.testbench.By;
import com.vaadin.testbench.parallel.Browser;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class RemoveInsertRow extends AbstractSpreadsheetTestCase {


    @Test
    public void removeRow_theFirstCellHasInvalidFormula_formulaIndicatorIsRemoved() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("=a");

        deleteFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return !spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator();
            }
        });
    }

    @Test
    public void removeRow_theSecondRowCellHasInvalidFormula_formulaIndicatorIsRemoved() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A2").setValue("=a");

        deleteRow(spreadsheet, 2);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.findElements(By.className("cell-invalidformula-triangle")).isEmpty();
            }
        });
    }

    @Test
    public void insertRow_theFirstCellHasInvalidFormula_theInvalidFormulaIsMovedToNextRow() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("=a");

        insertNewFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A2").hasInvalidFormulaIndicator();
            }
        });
    }

    @Test
    public void removeRow_theSecondRowHasInvalidFormulaCell_formulaIndicatorIsMovedUp() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A2").setValue("=a");

        deleteFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator();
            }
        });
    }

    @Test
    public void removeRow_theSecondAndThirdRowHasInvalidFormulaCell_formulaIndicatorIsMovedUp() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A2").setValue("=a");
        spreadsheet.getCellAt("A3").setValue("=a");

        deleteFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator() &&
                        spreadsheet.getCellAt("A2").hasInvalidFormulaIndicator();
            }
        });
    }

    @Test
    public void insertRow_theFirstAndSecondRowHasInvalidFormulaCellAndTheRowIsAddedBetween_theFirstAndThirdRowHasErrorIndicator() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("=a");
        spreadsheet.getCellAt("A2").setValue("=a");

        insertRow(spreadsheet, 2);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator() &&
                        !spreadsheet.getCellAt("A2").hasInvalidFormulaIndicator() &&
                        spreadsheet.getCellAt("A3").hasInvalidFormulaIndicator();
            }
        });
    }

    @Test
    public void removeRow_theFirstRowHasInvalidFormulaCellAndTheSecondRowIsRemoved_theFirstRowHasErrorIndicator() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("=a");

        deleteRow(spreadsheet, 2);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A1").hasInvalidFormulaIndicator();
            }
        });
    }

    @Test
    public void removeRow_theFirstCellHasMergedCell_thereIsNoMergedCells() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        new Actions(driver).clickAndHold(spreadsheet.getCellAt("A1"))
                .release(spreadsheet.getCellAt("B1")).perform();
        spreadsheet.getCellAt("A1").contextClick();
        spreadsheet.getContextMenu().getItem("Merge cells").click();

        deleteFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.findElements(By.cssSelector(".merged-cell")).isEmpty();
            }
        });
    }

    @Test
    public void insertRow_theFirstCellHasMergedCell_theMergedCellIsMovedToNextRow() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        new Actions(driver).clickAndHold(spreadsheet.getCellAt("A1"))
                .release(spreadsheet.getCellAt("B1")).perform();
        spreadsheet.getCellAt("A1").contextClick();
        spreadsheet.getContextMenu().getItem("Merge cells").click();

        insertNewFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.findElement(By.cssSelector(".col1.row2.merged-cell")).isDisplayed();
            }
        });
    }

    @Test
    public void removeRow_theFirstCellHasPopupButton_thereIsNoPopupButtons() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("B1").click();
        headerPage.loadTestFixture(TestFixtures.PopupButton);
        spreadsheet.getCellAt("A1").click();

        deleteFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return !spreadsheet.getCellAt("A1").hasPopupButton();
            }
        });
    }

    @Test
    public void removeRow_theSecondRowCellHasPopupButton_thereIsNoPopupButtons() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("B1").click();
        headerPage.loadTestFixture(TestFixtures.PopupButton);
        spreadsheet.getCellAt("A2").click();

        deleteRow(spreadsheet, 2);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.findElements(By.className("popupbutton")).isEmpty();
            }
        });
    }

    @Test
    public void removeRow_theSecondRowCellHasPopupButton_popupButtonIsMovedUp() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("B1").click();
        headerPage.loadTestFixture(TestFixtures.PopupButton);
        spreadsheet.getCellAt("A2").click();

        deleteFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A1").hasPopupButton();
            }
        });
    }

    @Test
    public void insertRow_theFirstCellHasPopupButton_thePopupButtonIsMovedToNextRow() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("B1").click();
        headerPage.loadTestFixture(TestFixtures.PopupButton);
        spreadsheet.getCellAt("A1").click();

        insertNewFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("A2").hasPopupButton();
            }
        });
    }


    @Ignore("This is a known issue which should be fixed. The problem is most likely on client side.")
    @Test
    public void insertRow_theFirstAndSecondRowHasPopupbuttonsNewRowIsAddedToFirstRow_theSecondAndThirdRowHasPopupButton() {
        skipBrowser("Context click does not work with PhantomJS and Firefox", Browser.PHANTOMJS, Browser.FIREFOX);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("B1").click();
        headerPage.loadTestFixture(TestFixtures.PopupButton);
        spreadsheet.getCellAt("A1").click();
        spreadsheet.getCellAt("A2").click();

        insertNewFirstRow(spreadsheet);

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return !spreadsheet.getCellAt("A1").hasPopupButton() &&
                        spreadsheet.getCellAt("A2").hasPopupButton() &&
                        spreadsheet.getCellAt("A3").hasPopupButton();
            }
        });
    }


    private void insertNewFirstRow(SpreadsheetElement spreadsheet) {
        insertRow(spreadsheet,1);
    }


    private void insertRow(SpreadsheetElement spreadsheet, int row) {
        spreadsheet.getRowHeader(row).contextClick();
        spreadsheet.getContextMenu().getItem("Insert new row").click();
    }

    private void deleteFirstRow(SpreadsheetElement spreadsheet) {
        deleteRow(spreadsheet, 1);
    }

    private void deleteRow(SpreadsheetElement spreadsheet, int row) {
        spreadsheet.getRowHeader(row).contextClick();
        spreadsheet.getContextMenu().getItem("Delete row").click();
    }
}
