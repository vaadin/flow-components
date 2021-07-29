package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

public class Inactive extends AbstractConfigurationObject {

    private Boolean animation;
    private Boolean enabled;
    private Number lineWidth;
    private Color borderColor;
    private Number brightness;
    private Color color;
    private Number opacity;

    public Inactive() {
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

    public Inactive(Boolean enabled) {
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
     * @see #setLineWidth(Number)
     */
    public Number getLineWidth() {
        return lineWidth;
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
}
