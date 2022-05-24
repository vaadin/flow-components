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
import java.util.Date;
import java.time.Instant;
import com.vaadin.flow.component.charts.util.Util;

/**
 * An array of objects defining plot bands on the Y axis.
 */
public class PlotBand extends AbstractConfigurationObject {

    private Color borderColor;
    private Number borderWidth;
    private String className;
    private Color color;
    private Number from;
    private String id;
    private String innerRadius;
    private Label label;
    private String outerRadius;
    private String thickness;
    private Number to;
    private Number zIndex;

    public PlotBand() {
    }

    /**
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Border color for the plot band. Also requires <code>borderWidth</code> to
     * be set.
     * <p>
     * Defaults to: null
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderWidth(Number)
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * Border width for the plot band. Also requires <code>borderColor</code> to
     * be set.
     * <p>
     * Defaults to: 0
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * A custom class name, in addition to the default
     * <code>highcharts-plot-band</code>, to apply to each individual band.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * The color of the plot band.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setFrom(Number)
     */
    public Number getFrom() {
        return from;
    }

    /**
     * The start position of the plot band in axis units.
     */
    public void setFrom(Number from) {
        this.from = from;
    }

    /**
     * @see #setId(String)
     */
    public String getId() {
        return id;
    }

    /**
     * An id used for identifying the plot band in Axis.removePlotBand.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @see #setInnerRadius(String)
     */
    public String getInnerRadius() {
        return innerRadius;
    }

    /**
     * In a gauge chart, this option determines the inner radius of the plot
     * band that stretches along the perimeter. It can be given as a percentage
     * string, like <code>"100%"</code>, or as a pixel number, like
     * <code>100</code>. By default, the inner radius is controlled by the
     * <a href="#yAxis.plotBands.thickness">thickness</a> option.
     * <p>
     * Defaults to: null
     */
    public void setInnerRadius(String innerRadius) {
        this.innerRadius = innerRadius;
    }

    /**
     * @see #setLabel(Label)
     */
    public Label getLabel() {
        if (label == null) {
            label = new Label();
        }
        return label;
    }

    /**
     * Text labels for the plot bands
     */
    public void setLabel(Label label) {
        this.label = label;
    }

    /**
     * @see #setOuterRadius(String)
     */
    public String getOuterRadius() {
        return outerRadius;
    }

    /**
     * In a gauge chart, this option determines the outer radius of the plot
     * band that stretches along the perimeter. It can be given as a percentage
     * string, like <code>"100%"</code>, or as a pixel number, like
     * <code>100</code>.
     * <p>
     * Defaults to: 100%
     */
    public void setOuterRadius(String outerRadius) {
        this.outerRadius = outerRadius;
    }

    /**
     * @see #setThickness(String)
     */
    public String getThickness() {
        return thickness;
    }

    /**
     * In a gauge chart, this option sets the width of the plot band stretching
     * along the perimeter. It can be given as a percentage string, like
     * <code>"10%"</code>, or as a pixel number, like <code>10</code>. The
     * default value 10 is the same as the default
     * <a href="#yAxis.tickLength">tickLength</a>, thus making the plot band act
     * as a background for the tick markers.
     * <p>
     * Defaults to: 10
     */
    public void setThickness(String thickness) {
        this.thickness = thickness;
    }

    /**
     * @see #setTo(Number)
     */
    public Number getTo() {
        return to;
    }

    /**
     * The end position of the plot band in axis units.
     */
    public void setTo(Number to) {
        this.to = to;
    }

    /**
     * @see #setZIndex(Number)
     */
    public Number getZIndex() {
        return zIndex;
    }

    /**
     * The z index of the plot band within the chart, relative to other
     * elements. Using the same z index as another element may give
     * unpredictable results, as the last rendered element will be on top.
     * Values from 0 to 20 make sense.
     */
    public void setZIndex(Number zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * @deprecated as of 4.0. Use {@link #setPointStart(Instant)}
     */
    @Deprecated
    public void setFrom(Date date) {
        this.from = Util.toHighchartsTS(date);
    }

    /**
     * @see #setFrom(Number)
     */
    public void setFrom(Instant instant) {
        this.from = Util.toHighchartsTS(instant);
    }

    /**
     * @deprecated as of 4.0. Use {@link #setPointStart(Instant)}
     */
    @Deprecated
    public void setTo(Date date) {
        this.to = Util.toHighchartsTS(date);
    }

    /**
     * @see #setTo(Number)
     */
    public void setTo(Instant instant) {
        this.to = Util.toHighchartsTS(instant);
    }

    public PlotBand(Number from, Number to, Color color) {
        this.from = from;
        this.to = to;
        this.color = color;
    }
}
