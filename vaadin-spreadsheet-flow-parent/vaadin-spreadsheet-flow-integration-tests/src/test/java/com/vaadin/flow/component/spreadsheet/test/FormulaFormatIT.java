package com.vaadin.flow.component.spreadsheet.test;

import java.util.Locale;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-spreadsheet")
public class FormulaFormatIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void formulaLocaleFormatting_italianLocale_formulaHandledCorrectly()
            throws InterruptedException {
        // TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        // When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.ITALY);
        createNewSpreadsheet();

        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=1,1+1");
        waitUntil(webDriver -> "2,1".equals(a1.getValue()));

        a1.setValue("=1.1+1");
        waitUntil(webDriver -> "#VALUE!".equals(a1.getValue()));

        a1.setValue("=a+1");
        waitUntil(webDriver -> "#VALUE!".equals(a1.getValue()));

        final SheetCellElement a2 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        a2.setValue("=1,123+1");
        waitUntil(webDriver -> "2,123".equals(a2.getValue()));
    }

    @Test
    public void formulaLocaleFormatting_englishLocale_formulaHandledCorrectly()
            throws InterruptedException {

        // TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        // setLocale(Locale.ENGLISH);
        // When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.ENGLISH);
        createNewSpreadsheet();

        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=1.1+1");
        waitUntil(webDriver -> "2.1".equals(a1.getValue()));

        a1.setValue("=1,1+1");
        waitUntil(webDriver -> "#VALUE!".equals(a1.getValue()));

        a1.setValue("=a+1");
        waitUntil(webDriver -> "#VALUE!".equals(a1.getValue()));

        a1.setValue("=1.123+1");
        waitUntil(webDriver -> "2.123".equals(a1.getValue()));
    }

    @Test
    public void formulaLocaleFormatting_changeLocale_formulaHandledCorrectly()
            throws InterruptedException {

        // TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        // setLocale(Locale.ITALY);
        // When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.ITALY);
        createNewSpreadsheet();

        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=1.1+1");
        waitUntil(webDriver -> "#VALUE!".equals(a1.getValue()));

        // TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        setLocale(Locale.ENGLISH);

        a1.setValue("=1.1+1");
        waitUntil(webDriver -> "2.1".equals(a1.getValue()));

    }

    @Test
    public void formulaFormatting_invalidFormula_cellHasInvalidFormulaIndicator()
            throws InterruptedException {
        createNewSpreadsheet();
        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        a1.setValue("=a");
        waitUntil(webDriver -> a1.hasInvalidFormulaIndicator());
        a1.setValue("=1");

        waitUntil(webDriver -> !a1.hasInvalidFormulaIndicator());
    }

    @Test
    public void formulaFormatting_setCommentToCellWithInvalidFormula_cellValueIsStillInvalidFormula()
            throws InterruptedException {
        createNewSpreadsheet();
        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        a1.setValue("=a");

        loadTestFixture(TestFixtures.AddOrRemoveComment);
        a1.click();

        waitUntil(webDriver -> "#VALUE!".equals(a1.getValue()));
    }

    @Test
    public void formulaFormatting_removeCommentFromCellWithInvalidFormula_cellValueIsStillInvalidFormula()
            throws InterruptedException {
        createNewSpreadsheet();
        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class)
                .first();
        final SheetCellElement a1 = spreadsheetElement.getCellAt("A1");
        loadTestFixture(TestFixtures.AddOrRemoveComment);
        selectCell("A1");
        a1.click();
        a1.setValue("=a");

        a1.contextClick();
        waitForContexMenu();
        spreadsheetElement.getContextMenu().getItem("Delete comment").click();

        waitUntil(webDriver -> "#VALUE!".equals(a1.getValue()));
    }

    @Test
    public void formulaFormatting_addFreezePaneWhileACellHasAnInvalidFormula_cellStillHasInvalidFormulaIndicator()
            throws InterruptedException {
        createNewSpreadsheet();
        SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        a1.setValue("=a");

        addFreezePane(); // Sheet content is reloaded

        final SheetCellElement a1_reloaded = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        waitUntil(webDriver -> a1_reloaded.hasInvalidFormulaIndicator());
    }

    private void waitForContexMenu() {
        waitUntil(webDriver -> webDriver
                .findElements(By.className("v-contextmenu")).size() > 0);
    }
}