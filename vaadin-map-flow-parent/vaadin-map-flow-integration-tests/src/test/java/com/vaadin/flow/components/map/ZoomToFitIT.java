/**
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.map.ZoomToFitPage;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-map/zoom-to-fit")
public class ZoomToFitIT extends AbstractComponentIT {

    private MapElement map;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
    }

    @Test
    public void initialZoomToFit_showsFirstSet() {
        Assert.assertTrue(isFeatureVisible(map, ZoomToFitPage.FEATURE_1));
        Assert.assertTrue(isFeatureVisible(map, ZoomToFitPage.FEATURE_2));
        Assert.assertTrue(isFeatureVisible(map, ZoomToFitPage.FEATURE_3));

        Assert.assertFalse(isFeatureVisible(map, ZoomToFitPage.FEATURE_4));
        Assert.assertFalse(isFeatureVisible(map, ZoomToFitPage.FEATURE_5));
        Assert.assertFalse(isFeatureVisible(map, ZoomToFitPage.FEATURE_6));
    }

    @Test
    public void zoomToFitSecondSet_showsSecondSet() {
        clickElementWithJs("zoom-to-second-set");

        Assert.assertFalse(isFeatureVisible(map, ZoomToFitPage.FEATURE_1));
        Assert.assertFalse(isFeatureVisible(map, ZoomToFitPage.FEATURE_2));
        Assert.assertFalse(isFeatureVisible(map, ZoomToFitPage.FEATURE_3));

        Assert.assertTrue(isFeatureVisible(map, ZoomToFitPage.FEATURE_4));
        Assert.assertTrue(isFeatureVisible(map, ZoomToFitPage.FEATURE_5));
        Assert.assertTrue(isFeatureVisible(map, ZoomToFitPage.FEATURE_6));
    }

    private boolean isFeatureVisible(MapElement map, MarkerFeature feature) {
        Coordinate coordinates = feature.getGeometry().getCoordinates();
        List<Double> extent = map.getMapReference().getView().calculateExtent();
        return coordinates.getX() >= extent.get(0)
                && coordinates.getX() <= extent.get(2)
                && coordinates.getY() >= extent.get(1)
                && coordinates.getY() <= extent.get(3);
    }
}
