package com.vaadin.addon.spreadsheet.test;

import java.util.Locale;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;

public class FormulaFormatTest extends AbstractSpreadsheetTestCase {

    @Test
    public void formulaLocaleFormatting_italianLocale_formulaHandledCorrectly()
            throws InterruptedException {
        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.ITALY);
        headerPage.createNewSpreadsheet();

        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=1,1+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "2,1".equals(a1.getValue());
            }
        });

        a1.setValue("=1.1+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "#VALUE!".equals(a1.getValue());
            }
        });

        a1.setValue("=a+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "#VALUE!".equals(a1.getValue());
            }
        });

        final SheetCellElement a2 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        a2.setValue("=1,123+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "2,123".equals(a2.getValue());
            }
        });
    }

    @Test
    public void formulaLocaleFormatting_englishLocale_formulaHandledCorrectly()
            throws InterruptedException {

        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //setLocale(Locale.ENGLISH);
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.ENGLISH);
        headerPage.createNewSpreadsheet();

        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=1.1+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "2.1".equals(a1.getValue());
            }
        });

        a1.setValue("=1,1+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "#VALUE!".equals(a1.getValue());
            }
        });

        a1.setValue("=a+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "#VALUE!".equals(a1.getValue());
            }
        });

        a1.setValue("=1.123+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "2.123".equals(a1.getValue());
            }
        });
    }

    @Test
    public void formulaLocaleFormatting_changeLocale_formulaHandledCorrectly()
            throws InterruptedException {

        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //setLocale(Locale.ITALY);
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.ITALY);
        headerPage.createNewSpreadsheet();

        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=1.1+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "#VALUE!".equals(a1.getValue());
            }
        });

        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        setLocale(Locale.ENGLISH);

        a1.setValue("=1.1+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "2.1".equals(a1.getValue());
            }
        });

    }

    @Test
    public void formulaFormatting_invalidFormula_cellHasInvalidFormulaIndicator()
            throws InterruptedException {
        headerPage.createNewSpreadsheet();
        reduceFontSizeAtCellA1(); // Otherwise, #VALUE! would overflow in PhantomJS
        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=a");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return a1.hasInvalidFormulaIndicator();
            }
        });
        a1.setValue("=1");

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return !a1.hasInvalidFormulaIndicator();
            }
        });
    }

    @Test
    public void formulaFormatting_setCommentToCellWithInvalidFormula_cellValueIsStillInvalidFormula()
            throws InterruptedException {
        headerPage.createNewSpreadsheet();
        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        a1.setValue("=a");

        headerPage.loadTestFixture(TestFixtures.AddOrRemoveComment);
        a1.click();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "#VALUE!".equals(a1.getValue());
            }
        });
    }

    @Test
    public void formulaFormatting_removeCommentFromCellWithInvalidFormula_cellValueIsStillInvalidFormula()
            throws InterruptedException {
        headerPage.createNewSpreadsheet();
        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();
        final SheetCellElement a1 = spreadsheetElement.getCellAt("A1");
        headerPage.loadTestFixture(TestFixtures.AddOrRemoveComment);
        a1.click();
        a1.setValue("=a");

        a1.contextClick();
        waitForContexMenu();
        spreadsheetElement.getContextMenu().getItem("Delete comment").click();

        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "#VALUE!".equals(a1.getValue());
            }
        });
    }

    @Test
    public void formulaFormatting_addFreezePaneWhileACellHasAnInvalidFormula_cellStillHasInvalidFormulaIndicator()
            throws InterruptedException {
        headerPage.createNewSpreadsheet();
        reduceFontSizeAtCellA1(); // Otherwise, #VALUE! would overflow in PhantomJS
        SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        a1.setValue("=a");

        headerPage.addFreezePane(); // Sheet content is reloaded

        final SheetCellElement a1_reloaded = $(SpreadsheetElement.class).first().getCellAt("A1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return a1_reloaded.hasInvalidFormulaIndicator();
            }
        });
    }

    private void reduceFontSizeAtCellA1() {
        String script = "var css = '.v-spreadsheet .col1.row1.cell { font-size: 8pt }'; " +
                        "var style = document.createElement('style'); " +
                        "style.appendChild(document.createTextNode(css)); " +
                        "document.head.appendChild(style);";
        executeScript(script);
    }

    private void waitForContexMenu() {
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return webDriver.findElements(By.className("v-contextmenu")).size() > 0;
            }
        });
    }

}
