package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/feature-text")
public class FeatureTextIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement updateTextButton;
    private TestBenchElement removeTextButton;
    private TestBenchElement setTextStyleButton;
    private TestBenchElement updateTextStyleButton;
    private TestBenchElement removeTextStyleButton;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        updateTextButton = $(TestBenchElement.class).id("update-marker-text");
        removeTextButton = $(TestBenchElement.class).id("remove-marker-text");
        setTextStyleButton = $(TestBenchElement.class).id("set-text-style");
        updateTextStyleButton = $(TestBenchElement.class)
                .id("update-text-style");
        removeTextStyleButton = $(TestBenchElement.class)
                .id("remove-text-style");

        trackRenderCount();
    }

    @Test
    public void defaultTextStyle() {
        MapElement.TextReference text = getMarkerTextStyle("marker1");

        Assert.assertEquals("13px sans-serif", text.getFont());
        Assert.assertEquals("#333", text.getFill().getColor());
        Assert.assertEquals("#fff", text.getStroke().getColor());
        Assert.assertEquals(3, text.getStroke().getWidth());
        Assert.assertEquals(0, text.getOffsetX());
        Assert.assertEquals(10, text.getOffsetY());
    }

    @Test
    public void initialTexts() {
        Assert.assertEquals("Marker text 1",
                getMarkerTextStyle("marker1").getText());
        Assert.assertEquals("Marker text 2",
                getMarkerTextStyle("marker2").getText());
        Assert.assertEquals("Marker text 3",
                getMarkerTextStyle("marker3").getText());
    }

    @Test
    public void updateTexts() {
        updateTextButton.click();

        Assert.assertEquals("Updated text 1",
                getMarkerTextStyle("marker1").getText());
        Assert.assertEquals("Updated text 2",
                getMarkerTextStyle("marker2").getText());
        Assert.assertEquals("Updated text 3",
                getMarkerTextStyle("marker3").getText());
    }

    @Test
    public void removeTexts() {
        removeTextButton.click();

        Assert.assertNull(getMarkerTextStyle("marker1").getText());
        Assert.assertNull(getMarkerTextStyle("marker2").getText());
        Assert.assertNull(getMarkerTextStyle("marker3").getText());
    }

    @Test
    public void setCustomTextStyle() {
        setTextStyleButton.click();

        MapElement.TextReference text = getMarkerTextStyle("marker1");
        Assert.assertEquals("Marker text 1", text.getText());
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
    public void updateCustomTextStyle() {
        setTextStyleButton.click();

        waitUntil(driver -> getRenderCount() == 1);

        updateTextStyleButton.click();

        MapElement.TextReference text = getMarkerTextStyle("marker1");
        Assert.assertEquals("Marker text 1", text.getText());
        Assert.assertEquals("15px sans-serif", text.getFont());

        waitUntil(driver -> getRenderCount() == 2);
    }

    @Test
    public void removeCustomTextStyle() {
        setTextStyleButton.click();

        waitUntil(driver -> getRenderCount() == 1);

        removeTextStyleButton.click();

        MapElement.TextReference text = getMarkerTextStyle("marker1");
        Assert.assertEquals("Marker text 1", text.getText());
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
