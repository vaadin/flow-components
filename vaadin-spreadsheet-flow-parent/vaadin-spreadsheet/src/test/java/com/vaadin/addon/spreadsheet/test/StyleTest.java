package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.Matchers.equalTo;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.parallel.Browser;

public class StyleTest extends AbstractSpreadsheetTestCase {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    @Ignore("Fails in all browsers, Are POI CellStyles even supported?")
    public void testCssStyleFromFixture() {
        headerPage.createNewSpreadsheet();
        headerPage.loadTestFixture(TestFixtures.Styles);
        testBench(driver).waitForVaadin();
        assertCorrectCss(sheetController);
        testBench(driver).waitForVaadin();
    }

    @Test
    public void testCssFromUpload() {
        skipBrowser("Fails in Phantom", Browser.PHANTOMJS);
        headerPage.createNewSpreadsheet();
        headerPage.loadFile("spreadsheet_styles.xlsx",this);
        assertCorrectCss(sheetController); // TODO - Fails with rev 18
        testBench(driver).waitForVaadin();
    }

    private void assertCorrectCss(SheetController c) {
        collector.checkThat(c.getCellStyle("A2", "text-align"),
                equalTo("center"));

        collector.checkThat(c.getCellStyle("B2", "text-align"),
                equalTo("right"));

        collector.checkThat(c.getCellStyle("A3", "border-bottom-color"),
                equalTo("rgba(0, 0, 255, 1)"));
        collector.checkThat(c.getCellStyle("A3", "border-bottom-style"),
                equalTo("solid"));
        collector.checkThat(c.getCellStyle("A3", "border-bottom-width"),
                equalTo("4px"));

        collector.checkThat(c.getCellStyle("B3", "background-color"),
                equalTo("rgba(0, 128, 0, 1)"));

        collector.checkThat(c.getCellStyle("A4", "color"),
                equalTo("rgba(255, 0, 0, 1)"));

        collector.checkThat(c.getCellStyle("C4", "font-style"),
                equalTo("italic"));

        collector.checkThat(
                (int) Math.ceil(getSize(c.getCellStyle("A5", "font-size"))),
                equalTo(11));
        collector.checkThat(
                (int) Math.ceil(getSize(c.getCellStyle("B5", "font-size"))),
                equalTo(14));
        collector.checkThat(
                (int) Math.ceil(getSize(c.getCellStyle("C5", "font-size"))),
                equalTo(16));
        collector.checkThat(
                (int) Math.ceil(getSize(c.getCellStyle("D5", "font-size"))),
                equalTo(19));

         if (getDesiredCapabilities().getBrowserName()
         .equalsIgnoreCase("chrome")) {
             collector.checkThat(c.getCellStyle("B4", "font-weight"), equalTo("bold"));
         } else {
            collector
                    .checkThat(c.getCellStyle("B4", "font-weight"), equalTo("700"));
         }
    }
}
