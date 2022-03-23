package com.vaadin.flow.component.charts.model.style;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class providing gradient colors
 */
public class GradientColor implements Color {

    public static class RadialGradient implements Serializable {
        private final Number cx;
        private final Number cy;
        private final Number r;

        public RadialGradient(double centerX, double centerY, double radius) {
            cx = centerX;
            cy = centerY;
            r = radius;
        }

        public Number getCx() {
            return cx;
        }

        public Number getCy() {
            return cy;
        }

        public Number getR() {
            return r;
        }
    }

    public static class LinearGradient implements Serializable {
        private final Number x1;
        private final Number y1;
        private final Number x2;
        private final Number y2;

        public LinearGradient(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public Number getX1() {
            return x1;
        }

        public Number getY1() {
            return y1;
        }

        public Number getX2() {
            return x2;
        }

        public Number getY2() {
            return y2;
        }
    }

    public static class Stop implements Serializable {

        private double position;
        private SolidColor color;

        public Stop(double position, SolidColor color) {
            this.position = position;
            this.color = color;
        }

        public double getPosition() {
            return position;
        }

        public SolidColor getColor() {
            return color;
        }

    }

    private List<Stop> stops;

    private RadialGradient radialGradient;

    private LinearGradient linearGradient;

    private GradientColor() {

    }

    /**
     * @return The linear gradient
     */
    public LinearGradient getLinearGradient() {
        return linearGradient;
    }

    /**
     * @return The radial gradient
     */
    public RadialGradient getRadialGradient() {
        return radialGradient;
    }

    /**
     * @return The stops of the gradient color
     */
    public List<Stop> getStops() {
        return stops;
    }

    /**
     * Adds a color stop to the gradient
     *
     * @param d
     *            The relative point of the color stop, between 0 and 1
     * @param color
     *            The color at the point defined by d
     */
    public void addColorStop(double d, SolidColor color) {
        if (stops == null) {
            stops = new ArrayList<Stop>();
        }
        stops.add(new Stop(d, color));
    }

    /**
     * Creates a new linear gradient between two given points. Use
     * {@link #addColorStop(double, SolidColor)} to define the colors.
     *
     * @param startX
     *            The relative start point on the X-axis, 0..1
     * @param startY
     *            The relative start point on the Y-axis, 0..1
     * @param endX
     *            The relative end point on the X-axis, 0..1
     * @param endY
     *            The relative end point on the Y-axis, 0..1
     * @return A new linear gradient color
     */
    public static GradientColor createLinear(double startX, double startY,
            double endX, double endY) {
        GradientColor ret = new GradientColor();
        ret.linearGradient = new LinearGradient(startX, startY, endX, endY);
        return ret;
    }

    /**
     * Creates a radial gradient color at a specified point with the given
     * radius. Use {@link #addColorStop(double, SolidColor)} to define the
     * colors.
     *
     * @param centerX
     *            The X coordinate of the center
     * @param centerY
     *            The Y coordinate of the center
     * @param radius
     *            The radius
     * @return A new radial gradient
     */
    public static GradientColor createRadial(double centerX, double centerY,
            double radius) {
        GradientColor ret = new GradientColor();
        ret.radialGradient = new RadialGradient(centerX, centerY, radius);
        return ret;
    }
}
