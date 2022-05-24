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
 * <p>
 * An optional scrollbar to display on the Y axis in response to limiting the
 * minimum an maximum of the axis values.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, all the presentational options for the scrollbar are
 * replaced by the classes <code>.highcharts-scrollbar-thumb</code>,
 * <code>.highcharts-scrollbar-arrow</code>,
 * <code>.highcharts-scrollbar-button</code>,
 * <code>.highcharts-scrollbar-rifles</code> and
 * <code>.highcharts-scrollbar-track</code>.
 * </p>
 */
public class Scrollbar extends AbstractConfigurationObject {

    private Color barBackgroundColor;
    private Color barBorderColor;
    private Number barBorderRadius;
    private Number barBorderWidth;
    private Color buttonArrowColor;
    private Color buttonBackgroundColor;
    private Color buttonBorderColor;
    private Number buttonBorderRadius;
    private Number buttonBorderWidth;
    private Boolean enabled;
    private Boolean liveRedraw;
    private Number margin;
    private Number minWidth;
    private Color rifleColor;
    private Boolean showFull;
    private Number size;
    private Color trackBackgroundColor;
    private Color trackBorderColor;
    private Number trackBorderRadius;
    private Number zIndex;
    private Number height;

    public Scrollbar() {
    }

    /**
     * @see #setBarBackgroundColor(Color)
     */
    public Color getBarBackgroundColor() {
        return barBackgroundColor;
    }

    /**
     * The background color of the scrollbar itself.
     * <p>
     * Defaults to: #cccccc
     */
    public void setBarBackgroundColor(Color barBackgroundColor) {
        this.barBackgroundColor = barBackgroundColor;
    }

    /**
     * @see #setBarBorderColor(Color)
     */
    public Color getBarBorderColor() {
        return barBorderColor;
    }

    /**
     * The color of the scrollbar's border.
     * <p>
     * Defaults to: #cccccc
     */
    public void setBarBorderColor(Color barBorderColor) {
        this.barBorderColor = barBorderColor;
    }

    /**
     * @see #setBarBorderRadius(Number)
     */
    public Number getBarBorderRadius() {
        return barBorderRadius;
    }

    /**
     * The border rounding radius of the bar.
     * <p>
     * Defaults to: 0
     */
    public void setBarBorderRadius(Number barBorderRadius) {
        this.barBorderRadius = barBorderRadius;
    }

    /**
     * @see #setBarBorderWidth(Number)
     */
    public Number getBarBorderWidth() {
        return barBorderWidth;
    }

    /**
     * The width of the bar's border.
     * <p>
     * Defaults to: 1
     */
    public void setBarBorderWidth(Number barBorderWidth) {
        this.barBorderWidth = barBorderWidth;
    }

    /**
     * @see #setButtonArrowColor(Color)
     */
    public Color getButtonArrowColor() {
        return buttonArrowColor;
    }

    /**
     * The color of the small arrow inside the scrollbar buttons.
     * <p>
     * Defaults to: #333333
     */
    public void setButtonArrowColor(Color buttonArrowColor) {
        this.buttonArrowColor = buttonArrowColor;
    }

    /**
     * @see #setButtonBackgroundColor(Color)
     */
    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    /**
     * The color of scrollbar buttons.
     * <p>
     * Defaults to: #e6e6e6
     */
    public void setButtonBackgroundColor(Color buttonBackgroundColor) {
        this.buttonBackgroundColor = buttonBackgroundColor;
    }

    /**
     * @see #setButtonBorderColor(Color)
     */
    public Color getButtonBorderColor() {
        return buttonBorderColor;
    }

    /**
     * The color of the border of the scrollbar buttons.
     * <p>
     * Defaults to: #cccccc
     */
    public void setButtonBorderColor(Color buttonBorderColor) {
        this.buttonBorderColor = buttonBorderColor;
    }

    /**
     * @see #setButtonBorderRadius(Number)
     */
    public Number getButtonBorderRadius() {
        return buttonBorderRadius;
    }

    /**
     * The corner radius of the scrollbar buttons.
     * <p>
     * Defaults to: 0
     */
    public void setButtonBorderRadius(Number buttonBorderRadius) {
        this.buttonBorderRadius = buttonBorderRadius;
    }

    /**
     * @see #setButtonBorderWidth(Number)
     */
    public Number getButtonBorderWidth() {
        return buttonBorderWidth;
    }

    /**
     * The border width of the scrollbar buttons.
     * <p>
     * Defaults to: 1
     */
    public void setButtonBorderWidth(Number buttonBorderWidth) {
        this.buttonBorderWidth = buttonBorderWidth;
    }

    public Scrollbar(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable the scrollbar on the Y axis.
     * <p>
     * Defaults to: false
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setLiveRedraw(Boolean)
     */
    public Boolean getLiveRedraw() {
        return liveRedraw;
    }

    /**
     * Whether to redraw the main chart as the scrollbar or the navigator zoomed
     * window is moved. Defaults to <code>true</code> for modern browsers and
     * <code>false</code> for legacy IE browsers as well as mobile devices.
     */
    public void setLiveRedraw(Boolean liveRedraw) {
        this.liveRedraw = liveRedraw;
    }

    /**
     * @see #setMargin(Number)
     */
    public Number getMargin() {
        return margin;
    }

    /**
     * Pixel margin between the scrollbar and the axis elements.
     * <p>
     * Defaults to: 10
     */
    public void setMargin(Number margin) {
        this.margin = margin;
    }

    /**
     * @see #setMinWidth(Number)
     */
    public Number getMinWidth() {
        return minWidth;
    }

    /**
     * The minimum width of the scrollbar.
     * <p>
     * Defaults to: 6
     */
    public void setMinWidth(Number minWidth) {
        this.minWidth = minWidth;
    }

    /**
     * @see #setRifleColor(Color)
     */
    public Color getRifleColor() {
        return rifleColor;
    }

    /**
     * The color of the small rifles in the middle of the scrollbar.
     * <p>
     * Defaults to: #333333
     */
    public void setRifleColor(Color rifleColor) {
        this.rifleColor = rifleColor;
    }

    /**
     * @see #setShowFull(Boolean)
     */
    public Boolean getShowFull() {
        return showFull;
    }

    /**
     * Whether to show the scrollbar when it is fully zoomed out at max range.
     * Setting it to <code>false</code> on the Y axis makes the scrollbar stay
     * hidden until the user zooms in, like common in browsers.
     * <p>
     * Defaults to: true
     */
    public void setShowFull(Boolean showFull) {
        this.showFull = showFull;
    }

    /**
     * @see #setSize(Number)
     */
    public Number getSize() {
        return size;
    }

    /**
     * The width of a vertical scrollbar or height of a horizontal scrollbar.
     * Defaults to 20 on touch devices.
     * <p>
     * Defaults to: 14
     */
    public void setSize(Number size) {
        this.size = size;
    }

    /**
     * @see #setTrackBackgroundColor(Color)
     */
    public Color getTrackBackgroundColor() {
        return trackBackgroundColor;
    }

    /**
     * The color of the track background.
     * <p>
     * Defaults to: #f2f2f2
     */
    public void setTrackBackgroundColor(Color trackBackgroundColor) {
        this.trackBackgroundColor = trackBackgroundColor;
    }

    /**
     * @see #setTrackBorderColor(Color)
     */
    public Color getTrackBorderColor() {
        return trackBorderColor;
    }

    /**
     * The color of the border of the scrollbar track.
     * <p>
     * Defaults to: #f2f2f2
     */
    public void setTrackBorderColor(Color trackBorderColor) {
        this.trackBorderColor = trackBorderColor;
    }

    /**
     * @see #setTrackBorderRadius(Number)
     */
    public Number getTrackBorderRadius() {
        return trackBorderRadius;
    }

    /**
     * The corner radius of the border of the scrollbar track.
     * <p>
     * Defaults to: 0
     */
    public void setTrackBorderRadius(Number trackBorderRadius) {
        this.trackBorderRadius = trackBorderRadius;
    }

    /**
     * @see #setZIndex(Number)
     */
    public Number getZIndex() {
        return zIndex;
    }

    /**
     * Z index of the scrollbar elements.
     * <p>
     * Defaults to: 3
     */
    public void setZIndex(Number zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * @see #setHeight(Number)
     */
    public Number getHeight() {
        return height;
    }

    /**
     * The height of the scrollbar. The height also applies to the width of the
     * scroll arrows so that they are always squares. Defaults to 20 for touch
     * devices and 14 for mouse devices.
     */
    public void setHeight(Number height) {
        this.height = height;
    }
}
