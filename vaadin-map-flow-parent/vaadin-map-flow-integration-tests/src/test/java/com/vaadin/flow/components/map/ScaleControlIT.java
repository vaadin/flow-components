/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.components.map;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-map/scale-control")
public class ScaleControlIT extends AbstractComponentIT {
    private MapElement map;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
    }

    @Test
    public void lineMode_rendersScaleLine() {
        assertLineMode("km");
    }

    @Test
    public void lineMode_updatesUnits() {
        clickElementWithJs("set-imperial-units");
        assertLineMode("mi");
    }

    @Test
    public void barMode_rendersScaleBar() {
        clickElementWithJs("toggle-display-mode");
        assertBarMode("km", 4, false);
    }

    @Test
    public void barMode_updatesUnits() {
        clickElementWithJs("toggle-display-mode");
        clickElementWithJs("set-imperial-units");
        assertBarMode("mi", 4, false);
    }

    @Test
    public void barMode_updatesSteps() {
        clickElementWithJs("toggle-display-mode");
        clickElementWithJs("set-two-steps");
        assertBarMode("km", 2, false);
    }

    @Test
    public void barMode_togglesText() {
        clickElementWithJs("toggle-display-mode");
        assertBarMode("km", 4, false);
        clickElementWithJs("toggle-scale-bar-text");
        assertBarMode("km", 4, true);
    }

    private void assertLineMode(String expectedUnit) {
        TestBenchElement scaleLine = map.$(TestBenchElement.class)
                .withClassName("ol-scale-line").withoutClassName("ol-scale-bar")
                .first();
        TestBenchElement inner = scaleLine.$(TestBenchElement.class)
                .withClassName("ol-scale-line-inner")
                .withoutClassName("ol-scale-bar-inner").first();
        Assert.assertTrue("Scale line inner should have no child elements",
                inner.$(TestBenchElement.class).all().isEmpty());
        String text = inner.getText();
        Assert.assertTrue(
                "Scale line text should contain unit '" + expectedUnit
                        + "' but was '" + text + "'",
                text.contains(expectedUnit));
    }

    private void assertBarMode(String expectedUnit, int expectedSteps,
            boolean textEnabled) {
        TestBenchElement scaleBar = map.$(TestBenchElement.class)
                .withClassName("ol-scale-bar").withoutClassName("ol-scale-line")
                .first();
        TestBenchElement inner = scaleBar.$(TestBenchElement.class)
                .withClassName("ol-scale-bar-inner")
                .withoutClassName("ol-scale-line-inner").first();
        String text = inner.getText();
        Assert.assertTrue(
                "Scale bar text should contain unit '" + expectedUnit
                        + "' but was '" + text + "'",
                text.contains(expectedUnit));
        List<TestBenchElement> steps = inner.$(TestBenchElement.class)
                .withClassName("ol-scale-singlebar").all();
        Assert.assertEquals("Scale bar should have correct number of steps",
                expectedSteps, steps.size());
        for (TestBenchElement step : steps) {
            int width = step.getSize().getWidth();
            Assert.assertTrue(
                    "Each scale bar step should have a positive width",
                    width > 0);
        }
        boolean hasScaleText = scaleBar.$(TestBenchElement.class)
                .withClassName("ol-scale-text").exists();
        if (textEnabled) {
            Assert.assertTrue("Scale text should be present", hasScaleText);
            TestBenchElement scaleText = scaleBar.$(TestBenchElement.class)
                    .withClassName("ol-scale-text").first();
            Assert.assertFalse("Scale text should not be empty",
                    scaleText.getText().isEmpty());
        } else {
            Assert.assertFalse("Scale text should not be present",
                    hasScaleText);
        }
    }
}
