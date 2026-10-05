/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.components.map;

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
        MapElement map = $(MapElement.class).waitForFirst();
        features = map.getMapReference().getLayers().getLayer(1).getSource()
                .asVectorSource().getFeatures();
    }

    @Test
    public void widthAndHeight_iconStretchedToSize() {
        waitUntilIconSize(0, 40, 40);
    }

    @Test
    public void widthOnly_heightPreservesAspectRatio() {
        waitUntilIconSize(1, 40, 52);
    }

    @Test
    public void heightOnly_widthPreservesAspectRatio() {
        waitUntilIconSize(2, 40, 52);
    }

    @Test
    public void sizeAndScale_scaleAppliedOnTopOfSize() {
        waitUntilIconSize(3, 80, 80);

        $(TestBenchElement.class).id("set-scale").click();

        waitUntilIconSize(3, 120, 120);
    }

    private void waitUntilIconSize(int featureIndex, double width,
            double height) {
        MapElement.IconReference icon = features.getFeature(featureIndex)
                .getStyle().getImage();
        waitUntil(driver -> {
            Double iconWidth = icon.getWidth();
            Double iconHeight = icon.getHeight();
            return iconWidth != null && iconHeight != null
                    && Math.abs(iconWidth - width) < 0.001
                    && Math.abs(iconHeight - height) < 0.001;
        });
    }
}
