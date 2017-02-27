package com.vaadin.addon.spreadsheet.test;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.demoapps.SpreadsheetDemoUI;

public class ScrollTBTest extends AbstractSpreadsheetTestCase {

    @Test
    public void testHiddenColumnHeaderScrollingFix() throws IOException {
        headerPage.loadFile("hidden.xlsx",this);
        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();
        spreadsheetElement.scroll(128);
        spreadsheetElement.scroll(128);
        spreadsheetElement.scroll(512);
        spreadsheetElement.scroll(-256);
        spreadsheetElement.scroll(512);
        spreadsheetElement.scroll(-1024);
        compareScreen("hiddenColumnScroll");
    }

    @Override
    public Class<?> getUIClass() {
        return SpreadsheetDemoUI.class;
    }
}
