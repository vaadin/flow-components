package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.testutil.OverlayHelper;

public class ChartTests extends AbstractSpreadsheetTestCase {

    private static final String CHART1_CELL = "C2";
    private static final String CHART1_PADDING_LEFT = "28px";
    private static final int CHART1_WIDTH = 425;
    private static final int CHART1_HEIGHT = 304;
    private static final int CHART_MINIMIZED_WIDTH = 28;
    private static final int CHART_MINIMIZED_HIGHT = 16;

    private OverlayHelper overlayHelper = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        overlayHelper = new OverlayHelper(driver);
    }

    @Test
    public void sampleWith3overlays_loadFile_overlaysPresentAndHaveCorrectSize()
            throws IOException {
        headerPage.loadFile("charts.xlsx", this);

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_PADDING_LEFT);

        assertOverlayProperties("A5", 425, 293, "29px");
        assertOverlayProperties("C10", 352, 307, "69px");
    }

    @Test
    public void sampleWith1overlay_disableOverlay_overlayIsNotPresent()
            throws IOException {
        String cell = "A5";

        headerPage.loadFile("chart.xlsx", this);

        assertTrue("Overlay should be visible",
                overlayHelper.isOverlayPresent(cell));

        headerPage.loadTestFixture(TestFixtures.DisableChartOverlays);

        assertFalse("Overlay shouldn't be visible",
                overlayHelper.isOverlayPresent(cell));
    }

    @Test
    public void sampleWith3overlays_minimizeAndRestore_success()
            throws IOException {
        headerPage.loadFile("charts.xlsx", this);

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_PADDING_LEFT);

        WebElement chartWrapperElement = overlayHelper
                .getOverlayElement(CHART1_CELL);
        WebElement minimizeButton = chartWrapperElement.findElement(By
                .className("minimize-button"));

        minimizeButton.click();

        assertOverlayProperties(CHART1_CELL, CHART_MINIMIZED_WIDTH,
                CHART_MINIMIZED_HIGHT, CHART1_PADDING_LEFT);

        minimizeButton.click();

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_PADDING_LEFT);
    }

    private void assertOverlayProperties(String cell, double width,
            double height, String paddingLeft) {
        WebElement elementC10 = overlayHelper.getOverlayElement(cell);
        assertEquals(width, elementC10.getSize().width, 1);
        assertEquals(height, elementC10.getSize().height, 1);
        assertEquals(paddingLeft, elementC10.getCssValue("padding-left"));
        // could not compare padding top as it is set in pt and the browsers
        // report it in px
    }

}
