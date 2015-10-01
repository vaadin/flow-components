package com.vaadin.addon.spreadsheet.test;

import java.util.Locale;

import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

public class FormulaFormatTest extends AbstractSpreadsheetTestCase {

    @Test
    public void formulaLocaleFormatting_italianLocale_formulaHandledCorrectly()
            throws InterruptedException {
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

        a1.setValue("=1,123+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "2,123".equals(a1.getValue());
            }
        });
    }

    @Test
    public void formulaLocaleFormatting_englishLocale_formulaHandledCorrectly()
            throws InterruptedException {
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

        setLocale(Locale.ENGLISH);

        a1.setValue("=1.1+1");
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return "2.1".equals(a1.getValue());
            }
        });

    }
}
