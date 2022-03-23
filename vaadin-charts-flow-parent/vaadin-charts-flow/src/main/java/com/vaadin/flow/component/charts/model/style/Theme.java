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
import com.vaadin.flow.component.charts.model.Credits;

/**
 * Theme class for Chart. This is empty theme, and only defines the structure of
 * Theme. Inherit own Theme class, or use Themes inherited from this class to
 * theme your Charts.
 *
 * @see VaadinTheme
 */
public class Theme extends AbstractConfigurationObject {
    private Color[] colors;
    private ChartStyle chart = new ChartStyle();
    private StyleWrapper title = new StyleWrapper();
    private StyleWrapper subtitle = new StyleWrapper();
    private AxisStyle xAxis = new AxisStyle();
    private AxisStyle yAxis = new AxisStyle();
    private StyleWrapper labels = new StyleWrapper();
    private LegendStyle legend = new LegendStyle();
    private TooltipStyle tooltip = new TooltipStyle();
    private PlotOptionsStyle plotOptions = new PlotOptionsStyle();
    private Credits credits = new Credits();

    /**
     * Set colors of items (bars, pie sectors ...) in chart
     *
     * @param colors
     *            Colors of items
     */
    public void setColors(Color... colors) {
        this.colors = colors;
    }

    /**
     * Get colors of items
     *
     * @return Colors of items, null if not defined
     */
    public Color[] getColors() {
        return colors;
    }

    /**
     * Get chart style
     *
     * @return Chart style
     */
    public ChartStyle getChart() {
        return chart;
    }

    /**
     * Set new chart style
     *
     * @param style
     *            Chart style
     */
    public void setChart(ChartStyle style) {
        chart = style;
    }

    /**
     * Get style of Chart title
     *
     * @return Style of title
     */
    public Style getTitle() {
        return title.getStyle();
    }

    /**
     * Set style of Chart title
     *
     * @param style
     *            New style of title
     */
    public void setTitle(Style style) {
        title.setStyle(style);
    }

    /**
     * Get style of Chart subtitle
     *
     * @return Style of subtitle
     */
    public Style getSubtitle() {
        return subtitle.getStyle();
    }

    /**
     * Set style of Chart subtitle
     *
     * @param style
     *            New style of subtitle
     */
    public void setSubtitle(Style style) {
        subtitle.setStyle(style);
    }

    /**
     * Get style of labels
     *
     * @return Labels style
     */
    public Style getLabels() {
        return labels.getStyle();
    }

    /**
     * Set new style of labels
     *
     * @param style
     *            Labels style
     */
    public void setLabels(Style style) {
        labels.setStyle(style);
    }

    /**
     * Get style of legend
     *
     * @return Legend style
     */
    public LegendStyle getLegend() {
        return legend;
    }

    /**
     * Set new style legend
     *
     * @param style
     *            Legend style
     */
    public void setLegend(LegendStyle style) {
        legend = style;
    }

    /**
     * Get style of X axes
     *
     * @return X axis style
     */
    public AxisStyle getxAxis() {
        return xAxis;
    }

    /**
     * Set new style of X axes
     *
     * @param style
     *            X axis style
     */
    public void setxAxis(AxisStyle style) {
        xAxis = style;
    }

    /**
     * Get style of Y axes
     *
     * @return Y axis style
     */
    public AxisStyle getyAxis() {
        return yAxis;
    }

    /**
     * Set new style of Y axes
     *
     * @param style
     *            Y axis style
     */
    public void setyAxis(AxisStyle style) {
        yAxis = style;
    }

    /**
     * Get style of tooltips
     *
     * @return Tooltip style
     */
    public TooltipStyle getTooltip() {
        return tooltip;
    }

    /**
     * Set new style of tooltips
     *
     * @param tooltip
     *            Tooltip style
     */
    public void setTooltip(TooltipStyle tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * Get style of plots
     *
     * @return Plot styles
     */
    public PlotOptionsStyle getPlotOptions() {
        return plotOptions;
    }

    /**
     * Set new style of plot
     *
     * @param plotOptions
     *            Plot styles
     */
    public void setPlotOptions(PlotOptionsStyle plotOptions) {
        this.plotOptions = plotOptions;
    }

    /**
     * Get credits style
     *
     * @return Credits style
     */
    public Credits getCredits() {
        return credits;
    }

    /**
     * Set new style for credits
     *
     * @param credits
     *            Credits style
     */
    public void setCredits(Credits credits) {
        this.credits = credits;
    }
}
