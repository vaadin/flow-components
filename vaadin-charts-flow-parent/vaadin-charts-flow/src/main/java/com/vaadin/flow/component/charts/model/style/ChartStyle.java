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

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;

/**
 * Styles of chart
 */
@SuppressWarnings("serial")
public class ChartStyle extends AbstractConfigurationObject {
    private Color backgroundColor;
    private Color plotBackgroundColor;
    private String plotBackgroundImage;
    private Boolean plotShadow;
    private Number plotBorderWidth;
    private Color plotBorderColor;
    private String className;
    private Number borderWidth;
    private Color borderColor;
    private Number borderRadius;
    private Style style;

    /**
     * @see #setBackgroundColor(Color)
     * @see #getPlotBackgroundColor()
     *
     * @return The background color of the chart, null if not defined
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the outer chart area. May be a gradient.
     * Defaults to "#FFFFFF".
     *
     * @see #setPlotBackgroundColor(Color)
     *
     * @param backgroundColor
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @see #setPlotBackgroundColor(Color)
     * @see #getBackgroundColor()
     *
     * @return The background color of the plot, null if not defined
     */
    public Color getPlotBackgroundColor() {
        return plotBackgroundColor;
    }

    /**
     * Sets the background color of the plot area. May be a gradient. Defaults
     * to null.
     *
     * @see #setBackgroundColor(Color)
     *
     * @param plotBackgroundColor
     */
    public void setPlotBackgroundColor(Color plotBackgroundColor) {
        this.plotBackgroundColor = plotBackgroundColor;
    }

    /**
     * @see #setPlotBackgroundImage(String)
     *
     * @return The background image of the plot, null if not defined
     */
    public String getPlotBackgroundImage() {
        return plotBackgroundImage;
    }

    /**
     * Sets the background of the plot to an image specified by the provided
     * URL. To set an image as the background for the entire chart, set a CSS
     * background image on the container element. Defaults to null.
     *
     * @param plotBackgroundImage
     *            The URL of the background image
     */
    public void setPlotBackgroundImage(String plotBackgroundImage) {
        this.plotBackgroundImage = plotBackgroundImage;
    }

    /**
     * @see #setPlotShadow(Boolean)
     * @return Whether a drop shadow is applied or null if not defined
     */
    public Boolean isPlotShadow() {
        return plotShadow;
    }

    /**
     * Sets whether to apply a drop shadow to the plot area. Requires that
     * plotBackgroundColor be set.
     *
     * @param plotShadow
     */
    public void setPlotShadow(Boolean plotShadow) {
        this.plotShadow = plotShadow;
    }

    /**
     * @see #setPlotBorderWidth(Number)
     *
     * @return The width of the plot border or null if not defined
     */
    public Number getPlotBorderWidth() {
        return plotBorderWidth;
    }

    /**
     * Sets the pixel width of the plot area border. Defaults to 0.
     *
     * @param plotBorderWidth
     *            Width of border
     */
    public void setPlotBorderWidth(Number plotBorderWidth) {
        this.plotBorderWidth = plotBorderWidth;
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the CSS class name to apply to the container DIV around the chart,
     * allowing unique CSS styling for each chart. Defaults to "".
     *
     * @param className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setBorderWidth(Number)
     *
     * @return The width of the chart border, null if not defined
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * Sets the pixel width of the outer chart border. The border is painted
     * using vector graphic techniques to allow rounded corners. Defaults to 0.
     *
     * @param borderWidth
     *            Border width
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setBorderRadius(Number)
     *
     * @return The corner radius of the border, null if not defined
     */
    public Number getBorderRadius() {
        return borderRadius;
    }

    /**
     * Sets the corner radius of the outer chart border. Defaults to 5.
     *
     * @param borderRadius
     *            Radius or border
     */
    public void setBorderRadius(Number borderRadius) {
        this.borderRadius = borderRadius;
    }

    /**
     * @see #setPlotBorderColor(Color)
     * @return The color of the plot border, null if not defined
     */
    public Color getPlotBorderColor() {
        return plotBorderColor;
    }

    /**
     * Sets the color of the outer chart border. The border is painted using
     * vector graphic techniques to allow rounded corners. Defaults to
     * "#4572A7".
     *
     * @param plotBorderColor
     */
    public void setPlotBorderColor(Color plotBorderColor) {
        this.plotBorderColor = plotBorderColor;
    }

    /**
     * Gets various style defaults used. This can be used to for example define
     * default font family.
     *
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Sets additional CSS styles to apply inline to the container div. Note
     * that since the default font styles are applied in the renderer, it is
     * ignorant of the individual chart options and must be set globally.
     * Defaults to:
     * <p>
     * <code>
     * style: {
     *  fontFamily: '"Lucida Grande", "Lucida Sans Unicode", Verdana, Arial, Helvetica, sans-serif', // default font fontSize: '12px'
     * }
     * </code>
     * </p>
     *
     * @param style
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see #setBorderColor(Color)
     * @return The color of the plot border, null if not defined
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the color of the outer chart border. Defaults to #4572A7.
     *
     * @param borderColor
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
}
