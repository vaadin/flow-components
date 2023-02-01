package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/feature-label")
public class FeatureLabelIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement updateLabelText;
    private TestBenchElement removeLabelText;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        updateLabelText = $(TestBenchElement.class).id("update-label-text");
        removeLabelText = $(TestBenchElement.class).id("remove-label-text");
    }

    @Test
    public void defaultLabelStyle() {
        MapElement.TextReference text = getMarkerTextStyle();

        Assert.assertEquals("Marker label", text.getText());
        Assert.assertEquals("13px sans-serif", text.getFont());
        Assert.assertEquals("#333", text.getFill().getColor());
        Assert.assertEquals("#fff", text.getStroke().getColor());
        Assert.assertEquals(3, text.getStroke().getWidth());
        Assert.assertEquals(0, text.getOffsetX());
        Assert.assertEquals(10, text.getOffsetY());
    }

    @Test
    public void updateLabel() {
        updateLabelText.click();

        MapElement.TextReference text = getMarkerTextStyle();
        Assert.assertEquals("Updated label", text.getText());
    }

    @Test
    public void removeLabel() {
        removeLabelText.click();

        MapElement.TextReference text = getMarkerTextStyle();
        Assert.assertNull(text.getText());
    }

    private MapElement.TextReference getMarkerTextStyle() {
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.VectorSourceReference vectorSource = mapReference.getLayers()
                .getLayer("feature-layer").getSource().asVectorSource();
        MapElement.FeatureReference feature = vectorSource.getFeatures()
                .getFeature(0);
        return feature.getStyle().getText();
    }
}
