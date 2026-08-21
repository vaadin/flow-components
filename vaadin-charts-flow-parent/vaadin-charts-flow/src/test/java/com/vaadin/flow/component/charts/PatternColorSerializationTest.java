/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static com.vaadin.flow.component.charts.util.ChartSerialization.toJSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.style.PatternColor;
import com.vaadin.flow.component.charts.model.style.SolidColor;

/**
 * Tests for the serialization of {@link PatternColor}.
 */
class PatternColorSerializationTest {

    // Sets the pattern as a DataSeriesItem color and asserts the exact JSON.
    private static void assertItemColorJson(PatternColor pattern,
            String expectedJson) {
        var item = new DataSeriesItem();
        item.setColor(pattern);
        assertEquals(expectedJson, toJSON(item));
    }

    @Test
    void patternColor_pathWithColorWidthHeight_toJSON() {
        assertItemColorJson(
                PatternColor.createPath("M 0 0 L 10 10",
                        new SolidColor("#ff0000"), 10, 10),
                "{\"color\":{\"pattern\":{\"color\":\"#ff0000\",\"height\":10,\"path\":\"M 0 0 L 10 10\",\"width\":10}}}");
    }

    @Test
    void patternColor_image_toJSON() {
        assertItemColorJson(
                PatternColor.createImage("https://example.com/p.png"),
                "{\"color\":{\"pattern\":{\"image\":\"https://example.com/p.png\"}}}");
    }

    @Test
    void patternColor_imageWithAspectRatioAndSize_toJSON() {
        var pattern = PatternColor.createImage("https://example.com/p.png");
        pattern.getPattern().setAspectRatio(1.5);
        pattern.getPattern().setWidth(32);
        pattern.getPattern().setHeight(24);
        assertItemColorJson(pattern,
                "{\"color\":{\"pattern\":{\"aspectRatio\":1.5,\"height\":24,\"image\":\"https://example.com/p.png\",\"width\":32}}}");
    }

    @Test
    void patternColor_extrasSet_toJSON() {
        var pattern = PatternColor.createPath("M 0 0 L 10 10");
        pattern.getPattern().setWidth(8);
        pattern.getPattern().setHeight(8);
        pattern.getPattern().setX(1);
        pattern.getPattern().setY(2);
        pattern.getPattern().setColor(new SolidColor("#00ff00"));
        pattern.getPattern().setOpacity(0.5);
        pattern.getPattern().setBackgroundColor(new SolidColor("#ffffff"));
        pattern.getPattern().setAspectRatio(1.5);
        pattern.getPattern().setPatternTransform("rotate(45)");
        pattern.getPattern().setId("my-pattern");
        assertItemColorJson(pattern,
                "{\"color\":{\"pattern\":{\"aspectRatio\":1.5,\"backgroundColor\":\"#ffffff\",\"color\":\"#00ff00\",\"height\":8,\"id\":\"my-pattern\",\"opacity\":0.5,\"path\":\"M 0 0 L 10 10\",\"patternTransform\":\"rotate(45)\",\"width\":8,\"x\":1,\"y\":2}}}");
    }

    @Test
    void patternColor_unsetFieldsAbsent_toJSON() {
        // Only the path is present; no null fields are serialized.
        assertItemColorJson(PatternColor.createPath("M 0 0 L 10 10"),
                "{\"color\":{\"pattern\":{\"path\":\"M 0 0 L 10 10\"}}}");
    }

    @Test
    void patternColor_onPlotOptions_toJSON() {
        var plotOptions = new PlotOptionsColumn();
        plotOptions.setColor(PatternColor.createPath("M 0 0 L 10 10",
                new SolidColor("#ff0000"), 10, 10));
        assertEquals(
                "{\"color\":{\"pattern\":{\"color\":\"#ff0000\",\"height\":10,\"path\":\"M 0 0 L 10 10\",\"width\":10}}}",
                toJSON(plotOptions));
    }
}
