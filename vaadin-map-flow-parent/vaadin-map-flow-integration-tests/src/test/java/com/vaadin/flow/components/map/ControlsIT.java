/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.components.map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-map/controls")
public class ControlsIT extends AbstractComponentIT {
    private MapElement map;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
    }

    @Test
    public void attributions_toggleVisibility() {
        // Attributions control should be visible initially
        Assert.assertTrue(queryAttributionsControl().exists());

        clickElementWithJs("toggle-attributions");

        Assert.assertFalse(queryAttributionsControl().exists());

        clickElementWithJs("toggle-attributions");

        Assert.assertTrue(queryAttributionsControl().exists());
    }

    @Test
    public void zoom_toggleVisibility() {
        // Zoom control should be visible initially
        Assert.assertTrue(queryZoomControl().exists());

        clickElementWithJs("toggle-zoom");

        Assert.assertFalse(queryZoomControl().exists());

        clickElementWithJs("toggle-zoom");

        Assert.assertTrue(queryZoomControl().exists());
    }

    @Test
    public void scaleLine_toggleVisibility() {
        // Scale lint should *not* be visible initially
        Assert.assertFalse(queryScaleLineControl().exists());

        clickElementWithJs("toggle-scale-line");

        Assert.assertTrue(queryScaleLineControl().exists());

        clickElementWithJs("toggle-scale-line");

        Assert.assertFalse(queryScaleLineControl().exists());
    }

    private ElementQuery<TestBenchElement> queryAttributionsControl() {
        return map.$(TestBenchElement.class).withClassName("ol-attribution")
                .withClassName("ol-control");
    }

    private ElementQuery<TestBenchElement> queryZoomControl() {
        return map.$(TestBenchElement.class).withClassName("ol-zoom")
                .withClassName("ol-control");
    }

    private ElementQuery<TestBenchElement> queryScaleLineControl() {
        return map.$(TestBenchElement.class).withClassName("ol-scale-line");
    }
}
