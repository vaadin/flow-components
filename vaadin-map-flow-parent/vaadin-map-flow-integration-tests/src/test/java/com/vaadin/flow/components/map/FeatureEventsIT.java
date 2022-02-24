package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

@TestPath("vaadin-map/feature-events")
public class FeatureEventsIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement addGlobalFeatureClickListener;
    private TestBenchElement addFirstLayerFeatureClickListener;
    private TestBenchElement eventLog;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        map.disableInteractions();
        addGlobalFeatureClickListener = $("button")
                .id("add-global-feature-click-listener");
        addFirstLayerFeatureClickListener = $("button")
                .id("add-first-layer-feature-click-listener");
        eventLog = $("div").id("event-log");
    }

    @Test
    public void globalClickListener_capturesEventsFromAllLayers() {
        addGlobalFeatureClickListener.click();

        // Click on first marker
        map.clickAtCoordinates(0, 0);
        // To prevent double-clicking, wait before we trigger the next event
        waitSeconds(1);
        // Click on second marker
        map.clickAtCoordinates(2000000, 0);
        // Click events are delayed by around 250ms, wait for last event
        waitSeconds(1);

        // Should have two events, one from each marker
        assertEventLogHasNumberOfEvents(2);
        assertEventLogContainsEvent(
                "click: feature=first-marker-feature | layer=first-feature-layer | source=first-source");
        assertEventLogContainsEvent(
                "click: feature=second-marker-feature | layer=second-feature-layer | source=second-source");
    }

    @Test
    public void layerClickListener_capturesEventsFromSingleLayer() {
        addFirstLayerFeatureClickListener.click();

        // Click on first marker
        map.clickAtCoordinates(0, 0);
        // To prevent double-clicking, wait before we trigger the next event
        waitSeconds(1);
        // Click on second marker
        map.clickAtCoordinates(2000000, 0);
        // Click events are delayed by around 250ms, wait for last event
        waitSeconds(1);

        // Should have only one event, from the first marker
        assertEventLogHasNumberOfEvents(1);
        assertEventLogContainsEvent(
                "click: feature=first-marker-feature | layer=first-feature-layer | source=first-source");
    }

    @Test
    public void overlappingFeatures_singleEventFromTopLevelFeature() {
        addFirstLayerFeatureClickListener.click();

        // Click on overlapping markers
        map.clickAtCoordinates(4000000, 0);
        // Click events are delayed by around 250ms, wait for event
        waitSeconds(1);

        // Should have only one event, from the marker that was last added
        assertEventLogHasNumberOfEvents(1);
        assertEventLogContainsEvent(
                "click: feature=overlapping-marker-feature-3 | layer=first-feature-layer | source=first-source");
    }

    private void assertEventLogHasNumberOfEvents(int expectedEvents) {
        String[] eventLines = eventLog.getText().split(System.lineSeparator());
        Assert.assertEquals(expectedEvents, eventLines.length);
    }

    private void assertEventLogContainsEvent(String expectedEvent) {
        String[] eventLines = eventLog.getText().split(System.lineSeparator());
        Assert.assertTrue("Event was not triggered: " + expectedEvent,
                Arrays.asList(eventLines).contains(expectedEvent));
    }

    private void waitSeconds(int seconds) {
        long start = System.currentTimeMillis();
        waitUntil(driver -> {
            long now = System.currentTimeMillis();
            double delta = Math.floor((now - start) / 1000f);
            return delta > seconds;
        });
    }
}
