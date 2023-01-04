/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.chartdata;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ChartData {
    public String title;

    public List<AbstractSeriesData> plotData;

    public View3dData view3dData;

    public boolean blanksAsZeros;

    public BorderStyle borderStyle;

    public TitleProperties titleStyle;

    public LegendProperties legendProperties;

    public AxisProperties xAxisProperties;

    /**
     * Items index is its id in AbstractSeriesData
     */
    public List<AxisProperties> yAxesProperties;

    public BackgroundProperties background;

    public static class BackgroundProperties {
        // either gradient or solid color, check which object is set
        public GradientProperties gradient;
        public ColorProperties color;
    }

    public static class GradientProperties {
        /**
         * Start point in 0..1 range and color
         */
        public SortedMap<Double, ColorProperties> colorStops = new TreeMap<Double, ColorProperties>();

        /**
         * Angle in 0..1 range: 0 meaning 0 degrees (starting vertically), 1
         * meaning 360 degrees.
         */
        public double angle;
    }

    public static class ColorProperties {
        public ColorProperties(int[] rgbUnsignedWithLum, double opacity) {
            this.opacity = opacity;
            red = rgbUnsignedWithLum[0];
            green = rgbUnsignedWithLum[1];
            blue = rgbUnsignedWithLum[2];
        }

        public int red, green, blue;
        public double opacity;
    }

    public static class AxisProperties {
        public String title = "";
        public TextProperties textProperties;
        public Double minVal; // null means use HighCharts default scaling logic
        public Double maxVal; // null means use HighCharts default scaling logic
        // Note: tried adding horizontal title for y-axis support, doesn't
        // really work in Highchart
    }

    public static class View3dData {
        public int rotation3dAngleA = 30;
        public int rotation3dAngleB = 30;
    }

    public static class BorderStyle {
        public double width = 0;
        public int radius = 0;
        public ColorProperties color;

        // Note: dash style is not supported by Highcharts
    }

    public static class TextProperties {
        public String fontFamily;
        public double size;
        public boolean bold;

        // note that italics is not supported by Vaadin Charts.
        // One can use <i> tag in some cases, that's how it works for the title.
        public boolean italics;

        public ColorProperties color;
    }

    public static class TitleProperties {
        public boolean isFloating;
        public TextProperties textProperties;
    }

    public enum LegendPosition {
        NONE, BOTTOM, TOP_RIGHT, LEFT, RIGHT, TOP;
    }

    public static class LegendProperties {
        public TextProperties textProperties;
        public LegendPosition position;
    }
}
