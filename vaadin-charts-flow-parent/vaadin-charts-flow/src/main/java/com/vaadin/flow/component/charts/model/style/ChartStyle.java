package com.vaadin.flow.component.charts.model.style;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;

/**
 * Styles of chart
 */
@SuppressWarnings("serial")
public class ChartStyle extends AbstractConfigurationObject {
    private String plotBackgroundImage;
    private Boolean plotShadow;
    private String className;
    private Number borderRadius;
    private Style style;

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
}
