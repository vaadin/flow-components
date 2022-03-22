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
 * The AxisStyle class contains options for customizing the style of an axis
 */
@SuppressWarnings("serial")
public class AxisStyle extends AbstractConfigurationObject {

    private TickIntervalStyle minorTickInterval;
    private Color lineColor;
    private Number lineWidth;
    private Number tickWidth;
    private Color tickColor;
    private Color gridLineColor;
    private Number gridLineWidth;
    private Color alternateGridColor;

    private final StyleWrapper title = new StyleWrapper();
    private final StyleWrapper subtitle = new StyleWrapper();
    private final StyleWrapper labels = new StyleWrapper();

    /**
     * @see #setMinorTickInterval(TickIntervalStyle)
     */
    public TickIntervalStyle getMinorTickInterval() {
        return minorTickInterval;
    }

    /**
     * Sets the tick interval in scale units for the minor ticks. On a linear
     * axis, if {@link TickIntervalStyle#AUTO}, the minor tick interval is
     * calculated as a fifth of the tickInterval. If null, minor ticks are not
     * shown.
     *
     * On logarithmic axes, the unit is the power of the value. For example,
     * setting the minor tick interval to 1 puts one tick on each of 0.1, 1, 10,
     * 100 etc. Setting the minor tick interval to 0.1 produces 9 ticks between
     * 1 and 10, 10 and 100 etc. A minor tick interval of
     * {@link TickIntervalStyle#AUTO} on a log axis results in a best guess,
     * attempting to enter approximately 5 minor ticks between each major tick.
     * Defaults to null.
     *
     * @param minorTickInterval
     */
    public void setMinorTickInterval(TickIntervalStyle minorTickInterval) {
        this.minorTickInterval = minorTickInterval;
    }

    /**
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Sets the color of the line marking the axis itself. Defaults to
     * "#C0D0E0".
     *
     * @param lineColor
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    /**
     * @see #setLineWidth(Number)
     */
    public Number getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the width of the line marking the axis itself. Defaults to 1.
     *
     * @param lineWidth
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @see #setTickWidth(Number)
     */
    public Number getTickWidth() {
        return tickWidth;
    }

    /**
     * Sets the pixel width of the major tick marks. Defaults to 1.
     *
     * @param tickWidth
     */
    public void setTickWidth(Number tickWidth) {
        this.tickWidth = tickWidth;
    }

    /**
     * @see #setTickColor(Color)
     */
    public Color getTickColor() {
        return tickColor;
    }

    /**
     * Sets the color for the main tick marks. Defaults to #C0D0E0.
     *
     * @param tickColor
     */
    public void setTickColor(Color tickColor) {
        this.tickColor = tickColor;
    }

    /**
     * @return The title style
     */
    public Style getTitle() {
        return title.getStyle();
    }

    /**
     * Sets the title style
     *
     * @param style
     */
    public void setTitle(Style style) {
        title.setStyle(style);
    }

    /**
     * @return The subtitle style
     */
    public Style getSubtitle() {
        return subtitle.getStyle();
    }

    /**
     * Sets the subtitle style
     *
     * @param style
     *            Subtitle style
     */
    public void setSubtitle(Style style) {
        subtitle.setStyle(style);
    }

    /**
     * @return The style for labels
     */
    public Style getLabels() {
        return labels.getStyle();
    }

    /**
     * Sets the style for labels
     *
     * @param style
     *            Labels style
     */
    public void setLabels(Style style) {
        labels.setStyle(style);
    }

    /**
     * @see #setGridLineWidth(Number)
     *
     * @return The width of grid lines or null if not defined
     */
    public Number getGridLineWidth() {
        return gridLineWidth;
    }

    /**
     * Sets the width of the grid lines extending the ticks across the plot
     * area. Defaults to 0.
     *
     * @param gridLineWidth
     */
    public void setGridLineWidth(Number gridLineWidth) {
        this.gridLineWidth = gridLineWidth;
    }

    /**
     * @see #setAlternateGridColor(Color)
     *
     * @return The alternate grid color, null if not defined
     */
    public Color getAlternateGridColor() {
        return alternateGridColor;
    }

    /**
     * Sets the alternate grid color. When using an alternate grid color, a band
     * is painted across the plot area between every other grid line. Defaults
     * to null.
     *
     * @param alternateGridColor
     */
    public void setAlternateGridColor(Color alternateGridColor) {
        this.alternateGridColor = alternateGridColor;
    }

    /**
     * @see #setGridLineColor(Color)
     *
     * @return The color of grid lines, null if not defined
     */
    public Color getGridLineColor() {
        return gridLineColor;
    }

    /**
     * Sets the color of the grid lines extending the ticks across the plot
     * area. Defaults to "#C0C0C0".
     *
     * @param gridLineColor
     *            Color of grid lines
     */
    public void setGridLineColor(Color gridLineColor) {
        this.gridLineColor = gridLineColor;
    }
}
