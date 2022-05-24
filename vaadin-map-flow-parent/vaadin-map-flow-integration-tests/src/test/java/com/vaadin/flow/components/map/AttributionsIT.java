package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@TestPath("vaadin-map/attributions")
public class AttributionsIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement setupOSMSource;
    private TestBenchElement setupXYZSource;
    private TestBenchElement setCustomAttributions;
    private TestBenchElement clearAttributions;
    private TestBenchElement setupCollapsibleEnabled;
    private TestBenchElement setupCollapsibleDisabled;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        setupOSMSource = $("button").id("setup-osm-source");
        setupXYZSource = $("button").id("setup-xyz-source");
        setCustomAttributions = $("button").id("set-custom-attributions");
        clearAttributions = $("button").id("clear-attributions");
        setupCollapsibleEnabled = $("button").id("setup-collapsible-enabled");
        setupCollapsibleDisabled = $("button").id("setup-collapsible-disabled");
    }

    @Test
    public void osmSourceDefaultAttributions() {
        setupOSMSource.click();
        waitUntilNumberOfAttributions(1);

        List<TestBenchElement> attributionItems = map.getAttributionItems();
        Assert.assertEquals(1, attributionItems.size());
        Assert.assertEquals(
                "© <a href=\"https://www.openstreetmap.org/copyright\" target=\"_blank\">OpenStreetMap</a> contributors.",
                attributionItems.get(0).getPropertyString("innerHTML"));
    }

    @Test
    public void customAttributions() {
        setupOSMSource.click();
        setCustomAttributions.click();
        waitUntilNumberOfAttributions(2);

        List<TestBenchElement> attributionItems = map.getAttributionItems();
        Assert.assertEquals(2, attributionItems.size());
        Assert.assertEquals(
                "© <a href=\"https://map-service-1.com\">Map service 1</a>",
                attributionItems.get(0).getPropertyString("innerHTML"));
        Assert.assertEquals(
                "© <a href=\"https://map-service-2.com\">Map service 2</a>",
                attributionItems.get(1).getPropertyString("innerHTML"));
    }

    @Test
    public void resetToDefaultAttributions() {
        // OSM source has default attributions, clearing the attributions should
        // result in resetting to default attributions
        setupOSMSource.click();
        setCustomAttributions.click();
        waitUntilNumberOfAttributions(2);

        clearAttributions.click();
        waitUntilNumberOfAttributions(1);

        List<TestBenchElement> attributionItems = map.getAttributionItems();
        Assert.assertEquals(1, attributionItems.size());
        Assert.assertEquals(
                "© <a href=\"https://www.openstreetmap.org/copyright\" target=\"_blank\">OpenStreetMap</a> contributors.",
                attributionItems.get(0).getPropertyString("innerHTML"));
    }

    @Test
    public void clearAttributions() {
        // XYZ source has no default attributions, clearing the attributions
        // should result in empty attributions container
        setupXYZSource.click();
        setCustomAttributions.click();
        waitUntilNumberOfAttributions(2);

        clearAttributions.click();
        waitUntilNumberOfAttributions(0);
    }

    @Test
    public void collapsibleEnabled() {
        setupCollapsibleEnabled.click();
        waitUntilNumberOfAttributions(2);

        // Collapsed by default
        TestBenchElement attributionContainer = map.getAttributionContainer();
        Assert.assertTrue("Attributions should have collapsed state",
                attributionContainer.getClassNames().contains("ol-collapsed"));
        // Has collapse button to toggle collapsed state (no need to test button
        // clicks, that is OpenLayers internal)
        TestBenchElement collapseButton = attributionContainer.$("button")
                .first();
        Assert.assertTrue("Collapse button should be displayed",
                collapseButton.isDisplayed());
    }

    @Test
    public void collapsibleDisabled() {
        setupCollapsibleDisabled.click();
        waitUntilNumberOfAttributions(2);

        // Not collapsed
        TestBenchElement attributionContainer = map.getAttributionContainer();
        Assert.assertFalse("Attributions should not have collapsed state",
                attributionContainer.getClassNames().contains("ol-collapsed"));
        // No collapse button to toggle collapsed state
        TestBenchElement collapseButton = attributionContainer.$("button")
                .first();
        Assert.assertFalse("Collapse button should not be displayed",
                collapseButton.isDisplayed());
    }

    /**
     * Updating attributions might be async in OpenLayers, wait until we have
     * the correct number
     *
     * @param expectedNumber
     *            number of expected attribution items
     */
    public void waitUntilNumberOfAttributions(int expectedNumber) {
        waitUntil(driver -> {
            List<TestBenchElement> attributionItems = map.getAttributionItems();
            return attributionItems.size() == expectedNumber;
        });
    }
}
