package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
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
        // Click on second marker
        map.clickAtCoordinates(2000000, 0);

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
        // Click on second marker
        map.clickAtCoordinates(2000000, 0);

        // Should have only one event, from the first marker
        assertEventLogHasNumberOfEvents(1);
        assertEventLogContainsEvent(
                "click: feature=first-marker-feature | layer=first-feature-layer | source=first-source");
    }

    private void assertEventLogHasNumberOfEvents(int expectedEvents) {
        waitUntil(driver -> {
            String[] eventLines = eventLog.getText()
                    .split(System.lineSeparator());
            return expectedEvents == eventLines.length;
        });
    }

    private void assertEventLogContainsEvent(String expectedEvent) {
        waitUntil(driver -> {
            String[] eventLines = eventLog.getText()
                    .split(System.lineSeparator());
            return Arrays.asList(eventLines).contains(expectedEvent);
        });
    }
}
