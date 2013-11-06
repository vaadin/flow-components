package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.addon.spreadsheet.test.testutil.SheetController;

public class StyleTest extends Test1 {

    @Test
    public void testCssStyleFromFixture() {
        SheetController c = keyboardSetup();
        newSheetAndLoadServerFixture("STYLES");

        testBench(driver).waitForVaadin();
        assertCorrectCss(c);
        testBench(driver).waitForVaadin();
    }

    @Test
    public void testCssFromUpload() {
        SheetController c = keyboardSetup();
        loadSheetFile("spreadsheet_styles.xlsx");
        assertCorrectCss(c); // TODO - Fails with rev 18
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

        if (getDesiredCapabilities().getBrowserName()
                .equalsIgnoreCase("chrome")) {
            collector.checkThat(c.getCellStyle("B4", "font-weight"),
                    equalTo("bold"));
            collector.checkThat(c.getCellStyle("A5", "font-size"),
                    equalTo("11px"));
            collector.checkThat(c.getCellStyle("B5", "font-size"),
                    equalTo("13px"));
            collector.checkThat(c.getCellStyle("C5", "font-size"),
                    equalTo("16px"));
            collector.checkThat(c.getCellStyle("D5", "font-size"),
                    equalTo("19px"));
        } else if (getDesiredCapabilities().getBrowserName().equalsIgnoreCase(
                DesiredCapabilities.internetExplorer().getBrowserName())) {
            collector.checkThat(c.getCellStyle("B4", "font-weight"),
                    equalTo("700"));
            collector.checkThat(c.getCellStyle("A5", "font-size"),
                    equalTo("10.66px"));
            collector.checkThat(c.getCellStyle("B5", "font-size"),
                    equalTo("13.33px"));
            collector.checkThat(c.getCellStyle("C5", "font-size"),
                    equalTo("16px"));
            collector.checkThat(c.getCellStyle("D5", "font-size"),
                    equalTo("18.66px"));
        } else {
            collector.checkThat(c.getCellStyle("B4", "font-weight"),
                    equalTo("700"));
            // Pixel to point conversion pixel = points * (16/12), firefox
            // rounds to
            // the 4th decimal digit
            collector.checkThat(c.getCellStyle("A5", "font-size"),
                    equalTo("10.6667px"));
            collector.checkThat(c.getCellStyle("B5", "font-size"),
                    equalTo("13.3333px"));
            // Assert.assertTrue(Pattern.matches("(13.3333|12)px",
            // c.getCellStyle("B5", "font-size")));
            collector.checkThat(c.getCellStyle("C5", "font-size"),
                    equalTo("16px"));
            collector.checkThat(c.getCellStyle("D5", "font-size"),
                    equalTo("18.6667px"));
        }
    }
}
