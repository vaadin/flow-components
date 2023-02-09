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
        MapElement.TextReference text = getMarkerTextStyle("marker1");

        Assert.assertEquals("13px sans-serif", text.getFont());
        Assert.assertEquals("#333", text.getFill().getColor());
        Assert.assertEquals("#fff", text.getStroke().getColor());
        Assert.assertEquals(3, text.getStroke().getWidth());
        Assert.assertEquals(0, text.getOffsetX());
        Assert.assertEquals(10, text.getOffsetY());
    }

    @Test
    public void initialLabels() {
        Assert.assertEquals("Marker label 1",
                getMarkerTextStyle("marker1").getText());
        Assert.assertEquals("Marker label 2",
                getMarkerTextStyle("marker2").getText());
        Assert.assertEquals("Marker label 3",
                getMarkerTextStyle("marker3").getText());
    }

    @Test
    public void updateLabels() {
        updateLabelText.click();

        Assert.assertEquals("Updated label 1",
                getMarkerTextStyle("marker1").getText());
        Assert.assertEquals("Updated label 2",
                getMarkerTextStyle("marker2").getText());
        Assert.assertEquals("Updated label 3",
                getMarkerTextStyle("marker3").getText());
    }

    @Test
    public void removeLabels() {
        removeLabelText.click();

        Assert.assertNull(getMarkerTextStyle("marker1").getText());
        Assert.assertNull(getMarkerTextStyle("marker2").getText());
        Assert.assertNull(getMarkerTextStyle("marker3").getText());
    }

    private MapElement.TextReference getMarkerTextStyle(String markerId) {
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.VectorSourceReference vectorSource = mapReference.getLayers()
                .getLayer("feature-layer").getSource().asVectorSource();
        MapElement.FeatureReference feature = vectorSource.getFeatures()
                .getFeature(markerId);
        return feature.getStyle().getText();
    }
}
