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
import com.vaadin.testbench.TestBenchElement;
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
        assertIconSize("width-and-height", 40, 40);
    }

    @Test
    public void widthOnly_heightPreservesAspectRatio() {
        assertIconSize("width-only", 40, 52);
    }

    @Test
    public void heightOnly_widthPreservesAspectRatio() {
        assertIconSize("height-only", 40, 52);
    }

    @Test
    public void scale_iconScaledFromNaturalSize() {
        assertIconSize("scale", 40, 52);
    }

    @Test
    public void changeScale_iconResized() {
        $(TestBenchElement.class).id("change-scale").click();
        MapElement.IconReference icon = getIcon("scale");
        waitUntil(driver -> icon.getScale() == 0.25);

        assertIconSize("scale", 20, 26);
    }

    private MapElement.IconReference getIcon(String featureId) {
        return features.getFeature(featureId).getStyle().getImage();
    }

    private void assertIconSize(String featureId, double width, double height) {
        MapElement.IconReference icon = getIcon(featureId);
        // Rendered size is only available once the image has loaded
        waitUntil(driver -> icon.getWidth() != null);
        Assert.assertEquals(width, icon.getWidth(), 0.001);
        Assert.assertEquals(height, icon.getHeight(), 0.001);
    }
}
