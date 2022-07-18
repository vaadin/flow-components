package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/modify-style")
public class ModifyStyleIT extends AbstractComponentIT {

    MapElement map;
    TestBenchElement setStyleImage;
    TestBenchElement setImageScale;
    TestBenchElement renderCount;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        setStyleImage = $(TestBenchElement.class).id("set-style-image");
        setImageScale = $(TestBenchElement.class).id("set-image-scale");
        renderCount = $(TestBenchElement.class).id("render-count");
    }

    @Test
    public void setStyleImage_triggersRender() {
        MapElement.LayerReference featureLayer = map.getMapReference()
                .getLayers().getLayer("feature-layer");

        // Should start with initial render
        waitUntilRenderCount(1);
        long initialRevision = featureLayer.getRevision();

        setStyleImage.click();

        // Should trigger another render
        waitUntilRenderCount(2);
        // Feature layer revision should have increased, indicating that the
        // layer has been redrawn as part of the latest rendering
        Assert.assertEquals(initialRevision + 1, featureLayer.getRevision());
    }

    @Test
    public void setImageScale_triggersRender() {
        MapElement.LayerReference featureLayer = map.getMapReference()
                .getLayers().getLayer("feature-layer");

        // Should start with initial render
        waitUntilRenderCount(1);
        long initialRevision = featureLayer.getRevision();

        setImageScale.click();

        // Should trigger another render
        waitUntilRenderCount(2);
        // Feature layer revision should have increased, indicating that the
        // layer has been redrawn as part of the latest rendering
        Assert.assertEquals(initialRevision + 1, featureLayer.getRevision());
    }

    private void waitUntilRenderCount(int count) {
        waitUntil(driver -> getRenderCount() == count);
    }

    private int getRenderCount() {
        return Integer.parseInt(renderCount.getText());
    }
}
