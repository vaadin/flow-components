package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * Options for the hovered series
 */
public class Hover extends AbstractConfigurationObject {

    private Boolean animation;
    private Boolean enabled;
    private Halo halo;
    private Number lineWidth;
    private Number lineWidthPlus;
    private Color fillColor;
    private Color lineColor;
    private Number radius;
    private Number radiusPlus;
    private Color borderColor;
    private Number brightness;
    private Color color;
    private Number opacity;
    private Marker marker;

    public Hover() {
    }

    /**
     * @see #setAnimation(Boolean)
     */
    public Boolean getAnimation() {
        return animation;
    }

    /**
     * Animation setting for hovering the graph in line-type series.
     * <p>
     * Defaults to: { "duration": 50 }
     */
    public void setAnimation(Boolean animation) {
        this.animation = animation;
    }

    public Hover(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable separate styles for the hovered series to visualize that the user
     * hovers either the series itself or the legend. .
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setHalo(Halo)
     */
    public Halo getHalo() {
        if (halo == null) {
            halo = new Halo();
        }
        return halo;
    }

    /**
     * <p>
     * Options for the halo appearing around the hovered point in line-type
     * series as well as outside the hovered slice in pie charts. By default the
     * halo is filled by the current point or series color with an opacity of
     * 0.25. The halo can be disabled by setting the <code>halo</code> option to
     * <code>false</code>.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the halo is styled with the
     * <code>.highcharts-halo</code> class, with colors inherited from
     * <code>.highcharts-color-{n}</code>.
     * </p>
     */
    public void setHalo(Halo halo) {
        this.halo = halo;
    }

    /**
     * @see #setLineWidth(Number)
     */
    public Number getLineWidth() {
        return lineWidth;
    }

    /**
     * Pixel with of the graph line.
     * <p>
     * Defaults to: 2
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @see #setLineWidthPlus(Number)
     */
    public Number getLineWidthPlus() {
        return lineWidthPlus;
    }

    /**
     * The additional line width for the graph of a hovered series.
     * <p>
     * Defaults to: 1
     */
    public void setLineWidthPlus(Number lineWidthPlus) {
        this.lineWidthPlus = lineWidthPlus;
    }

    /**
     * @see #setFillColor(Color)
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * The fill color of the marker in hover state.
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * The color of the point marker's outline. When <code>null</code>, the
     * series' or point's color is used.
     * <p>
     * Defaults to: #ffffff
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    /**
     * @see #setRadius(Number)
     */
    public Number getRadius() {
        return radius;
    }

    /**
     * The radius of the point marker. In hover state, it defaults to the normal
     * state's radius + 2 as per the <a href=
     * "#plotOptions.series.marker.states.hover.radiusPlus">radiusPlus</a>
     * option.
     */
    public void setRadius(Number radius) {
        this.radius = radius;
    }

    /**
     * @see #setRadiusPlus(Number)
     */
    public Number getRadiusPlus() {
        return radiusPlus;
    }

    /**
     * The number of pixels to increase the radius of the hovered point.
     * <p>
     * Defaults to: 2
     */
    public void setRadiusPlus(Number radiusPlus) {
        this.radiusPlus = radiusPlus;
    }

    /**
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * A specific border color for the hovered point. Defaults to inherit the
     * normal state border color.
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBrightness(Number)
     */
    public Number getBrightness() {
        return brightness;
    }

    /**
     * <p>
     * How much to brighten the point on interaction. Requires the main color to
     * be defined in hex or rgb(a) format.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the hover brightening is by default replaced with a
     * fill-opacity set in the <code>.highcharts-point:hover</code> rule.
     * </p>
     * <p>
     * Defaults to: 0.1
     */
    public void setBrightness(Number brightness) {
        this.brightness = brightness;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * A specific color for the hovered point.
     * <p>
     * Defaults to: undefined
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setOpacity(Number)
     */
    public Number getOpacity() {
        return opacity;
    }

    /**
     * The opacity of a point in treemap. When a point has children, the
     * visibility of the children is determined by the opacity.
     * <p>
     * Defaults to: 0.75
     */
    public void setOpacity(Number opacity) {
        this.opacity = opacity;
    }

    /**
     * @see #setMarker(Marker)
     */
    public Marker getMarker() {
        if (marker == null) {
            marker = new Marker();
        }
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
