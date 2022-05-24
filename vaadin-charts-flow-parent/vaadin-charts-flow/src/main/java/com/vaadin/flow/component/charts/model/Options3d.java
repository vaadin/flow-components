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

/**
 * Options to render charts in 3 dimensions. This feature requires
 * <code>highcharts-3d.js</code>, found in the download package or online at
 * <a href="http://code.highcharts.com/highcharts-3d.js">code.highcharts.com/
 * highcharts-3d.js</a>.
 */
public class Options3d extends AbstractConfigurationObject {

    public static final String AXIS_LABEL_POSITION_AUTO = "auto";
    private Number alpha;
    private String axisLabelPosition;
    private Number beta;
    private Number depth;
    private Boolean enabled;
    private Boolean fitToPlot;
    private Frame frame;
    private Number viewDistance;

    public Options3d() {
    }

    /**
     * @see #setAlpha(Number)
     */
    public Number getAlpha() {
        return alpha;
    }

    /**
     * One of the two rotation angles for the chart.
     * <p>
     * Defaults to: 0
     */
    public void setAlpha(Number alpha) {
        this.alpha = alpha;
    }

    /**
     * @see #setAxisLabelPosition(String)
     */
    public String getAxisLabelPosition() {
        return axisLabelPosition;
    }

    /**
     * Set it to <code>"auto"</code> to automatically move the labels to the
     * best edge.
     * <p>
     * Defaults to: null
     */
    public void setAxisLabelPosition(String axisLabelPosition) {
        this.axisLabelPosition = axisLabelPosition;
    }

    /**
     * @see #setBeta(Number)
     */
    public Number getBeta() {
        return beta;
    }

    /**
     * One of the two rotation angles for the chart.
     * <p>
     * Defaults to: 0
     */
    public void setBeta(Number beta) {
        this.beta = beta;
    }

    /**
     * @see #setDepth(Number)
     */
    public Number getDepth() {
        return depth;
    }

    /**
     * The total depth of the chart.
     * <p>
     * Defaults to: 100
     */
    public void setDepth(Number depth) {
        this.depth = depth;
    }

    public Options3d(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Wether to render the chart using the 3D functionality.
     * <p>
     * Defaults to: false
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setFitToPlot(Boolean)
     */
    public Boolean getFitToPlot() {
        return fitToPlot;
    }

    /**
     * Whether the 3d box should automatically adjust to the chart plot area.
     * <p>
     * Defaults to: true
     */
    public void setFitToPlot(Boolean fitToPlot) {
        this.fitToPlot = fitToPlot;
    }

    /**
     * @see #setFrame(Frame)
     */
    public Frame getFrame() {
        if (frame == null) {
            frame = new Frame();
        }
        return frame;
    }

    /**
     * Provides the option to draw a frame around the charts by defining a
     * bottom, front and back panel.
     */
    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    /**
     * @see #setViewDistance(Number)
     */
    public Number getViewDistance() {
        return viewDistance;
    }

    /**
     * Defines the distance the viewer is standing in front of the chart, this
     * setting is important to calculate the perspective effect in column and
     * scatter charts. It is not used for 3D pie charts.
     * <p>
     * Defaults to: 100
     */
    public void setViewDistance(Number viewDistance) {
        this.viewDistance = viewDistance;
    }
}
