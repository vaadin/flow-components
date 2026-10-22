/**
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-map/icon-size")
public class IconSizeIT extends AbstractComponentIT {
    private MapElement.FeatureCollectionReference features;

    @Before
    public void init() {
        open();
        MapElement map = $(MapElement.class).single();
        features = map.getMapReference().getLayers().getLayer(1).getSource()
                .asVectorSource().getFeatures();
    }

    @Test
    public void widthAndHeight_iconStretchedToSize() {
        assertIconSize(0, 40, 40);
    }

    @Test
    public void widthOnly_heightPreservesAspectRatio() {
        assertIconSize(1, 40, 52);
    }

    @Test
    public void heightOnly_widthPreservesAspectRatio() {
        assertIconSize(2, 40, 52);
    }

    private void assertIconSize(int featureIndex, double width, double height) {
        MapElement.IconReference icon = features.getFeature(featureIndex)
                .getStyle().getImage();
        // Rendered size is only available once the image has loaded
        waitUntil(driver -> icon.getWidth() != null);
        Assert.assertEquals(width, icon.getWidth(), 0.001);
        Assert.assertEquals(height, icon.getHeight(), 0.001);
    }
}
