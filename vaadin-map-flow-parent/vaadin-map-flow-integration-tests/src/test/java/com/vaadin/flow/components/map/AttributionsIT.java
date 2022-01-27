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
    private TestBenchElement setupCustomAttributions;
    private TestBenchElement changeAttributions;
    private TestBenchElement clearAttributions;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).first();
        setupCustomAttributions = $("button").id("setup-custom-attributions");
        changeAttributions = $("button").id("change-attributions");
        clearAttributions = $("button").id("clear-attributions");
    }

    @Test
    public void defaultAttributions() {
        List<TestBenchElement> attributionItems = map.getAttributionItems();

        Assert.assertEquals(1, attributionItems.size());
        Assert.assertEquals(
                "© <a href=\"https://www.openstreetmap.org/copyright\" target=\"_blank\">OpenStreetMap</a> contributors.",
                attributionItems.get(0).getPropertyString("innerHTML"));
    }

    @Test
    public void setupCustomAttributions() {
        setupCustomAttributions.click();

        // Updating attributions might be async in OpenLayers, wait until we
        // have the correct number
        waitUntilNumberOfAttributions(2);
        List<TestBenchElement> attributionItems = map.getAttributionItems();

        Assert.assertEquals(
                "© <a href=\"https://map-service-1.com\">Map service 1</a>",
                attributionItems.get(0).getPropertyString("innerHTML"));
        Assert.assertEquals(
                "© <a href=\"https://map-service-2.com\">Map service 2</a>",
                attributionItems.get(1).getPropertyString("innerHTML"));
    }

    @Test
    public void changeAttributions() {
        changeAttributions.click();

        // Updating attributions might be async in OpenLayers, wait until we
        // have the correct number
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
    public void clearAttributions() {
        clearAttributions.click();

        // Updating attributions might be async in OpenLayers, wait until we
        // have the correct number
        waitUntilNumberOfAttributions(0);
    }

    public void waitUntilNumberOfAttributions(int expectedNumber) {
        waitUntil(driver -> {
            List<TestBenchElement> attributionItems = map.getAttributionItems();
            return attributionItems.size() == expectedNumber;
        });
    }
}
