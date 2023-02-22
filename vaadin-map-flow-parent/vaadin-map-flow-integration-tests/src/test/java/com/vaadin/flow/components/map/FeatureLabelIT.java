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
    private TestBenchElement updateLabelButton;
    private TestBenchElement removeLabelButton;
    private TestBenchElement setLabelStyleButton;
    private TestBenchElement updateLabelStyleButton;
    private TestBenchElement removeLabelStyleButton;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        updateLabelButton = $(TestBenchElement.class).id("update-label-text");
        removeLabelButton = $(TestBenchElement.class).id("remove-label-text");
        setLabelStyleButton = $(TestBenchElement.class).id("set-label-style");
        updateLabelStyleButton = $(TestBenchElement.class)
                .id("update-label-style");
        removeLabelStyleButton = $(TestBenchElement.class)
                .id("remove-label-style");

        trackRenderCount();
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
        updateLabelButton.click();

        Assert.assertEquals("Updated label 1",
                getMarkerTextStyle("marker1").getText());
        Assert.assertEquals("Updated label 2",
                getMarkerTextStyle("marker2").getText());
        Assert.assertEquals("Updated label 3",
                getMarkerTextStyle("marker3").getText());
    }

    @Test
    public void removeLabels() {
        removeLabelButton.click();

        Assert.assertNull(getMarkerTextStyle("marker1").getText());
        Assert.assertNull(getMarkerTextStyle("marker2").getText());
        Assert.assertNull(getMarkerTextStyle("marker3").getText());
    }

    @Test
    public void setSetLabelStyle() {
        setLabelStyleButton.click();

        MapElement.TextReference text = getMarkerTextStyle("marker1");
        Assert.assertEquals("Marker label 1", text.getText());
        Assert.assertEquals("bold 13px monospace", text.getFont());
        Assert.assertEquals(30, text.getOffsetX());
        Assert.assertEquals(0, text.getOffsetY());
        Assert.assertEquals("left", text.getTextAlign());
        Assert.assertEquals("bottom", text.getTextBaseline());
        Assert.assertEquals("#fff", text.getFill().getColor());
        Assert.assertEquals("#000", text.getStroke().getColor());
        Assert.assertEquals(5, text.getStroke().getWidth());
        Assert.assertEquals("#1F6B75", text.getBackgroundFill().getColor());
        Assert.assertEquals("#fff", text.getBackgroundStroke().getColor());
        Assert.assertEquals(2, text.getBackgroundStroke().getWidth());

        waitUntil(driver -> getRenderCount() == 1);
    }

    @Test
    public void updateLabelStyle() {
        setLabelStyleButton.click();

        waitUntil(driver -> getRenderCount() == 1);

        updateLabelStyleButton.click();

        MapElement.TextReference text = getMarkerTextStyle("marker1");
        Assert.assertEquals("Marker label 1", text.getText());
        Assert.assertEquals("15px sans-serif", text.getFont());

        waitUntil(driver -> getRenderCount() == 2);
    }

    @Test
    public void removeLabelStyle() {
        setLabelStyleButton.click();

        waitUntil(driver -> getRenderCount() == 1);

        removeLabelStyleButton.click();

        MapElement.TextReference text = getMarkerTextStyle("marker1");
        Assert.assertEquals("Marker label 1", text.getText());
        Assert.assertEquals("13px sans-serif", text.getFont());

        waitUntil(driver -> getRenderCount() == 2);
    }

    private MapElement.TextReference getMarkerTextStyle(String markerId) {
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.VectorSourceReference vectorSource = mapReference.getLayers()
                .getLayer("feature-layer").getSource().asVectorSource();
        MapElement.FeatureReference feature = vectorSource.getFeatures()
                .getFeature(markerId);
        return feature.getStyle().getText();
    }

    private long getRenderCount() {
        return (long) getCommandExecutor().executeScript(
                "const map = arguments[0];" + "return map.__renderCount;", map);
    }

    private void trackRenderCount() {
        getCommandExecutor().executeScript("const map = arguments[0];"
                + "map.__renderCount = 0;"
                + "map.configuration.on('rendercomplete', () => { map.__renderCount = map.__renderCount + 1 });",
                map);
    }
}
