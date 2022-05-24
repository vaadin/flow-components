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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Applies only to polar charts and angular gauges. This configuration object
 * holds general options for the combined X and Y axes set. Each xAxis or yAxis
 * can reference the pane by index.
 */
public class Pane extends AbstractConfigurationObject {

    private Integer paneIndex;
    private ArrayList<Background> background;
    private String[] center;
    private Number endAngle;
    private String size;
    private Number startAngle;

    public Pane() {
    }

    /**
     * @see #setPaneIndex(Integer)
     */
    Integer getPaneIndex() {
        return paneIndex;
    }

    void setPaneIndex(Integer paneIndex) {
        this.paneIndex = paneIndex;
    }

    /**
     * @see #setBackground(Background...)
     */
    public Background[] getBackground() {
        if (background == null) {
            return new Background[] {};
        }
        Background[] arr = new Background[background.size()];
        background.toArray(arr);
        return arr;
    }

    /**
     * An object, or array of objects, for backgrounds.
     */
    public void setBackground(Background... background) {
        this.background = new ArrayList<Background>(Arrays.asList(background));
    }

    /**
     * Adds background to the background array
     *
     * @param background
     *            to add
     * @see #setBackground(Background...)
     */
    public void addBackground(Background background) {
        if (this.background == null) {
            this.background = new ArrayList<Background>();
        }
        this.background.add(background);
    }

    /**
     * Removes first occurrence of background in background array
     *
     * @param background
     *            to remove
     * @see #setBackground(Background...)
     */
    public void removeBackground(Background background) {
        this.background.remove(background);
    }

    /**
     * The center of a polar chart or angular gauge, given as an array of [x, y]
     * positions. Positions can be given as integers that transform to pixels,
     * or as percentages of the plot area size.
     * <p>
     * Defaults to: ["50%", "50%"]
     */
    public void setCenter(String[] center) {
        this.center = center;
    }

    /**
     * @see #setEndAngle(Number)
     */
    public Number getEndAngle() {
        return endAngle;
    }

    /**
     * The end angle of the polar X axis or gauge value axis, given in degrees
     * where 0 is north. Defaults to <a href="#pane.startAngle">startAngle</a> +
     * 360.
     */
    public void setEndAngle(Number endAngle) {
        this.endAngle = endAngle;
    }

    /**
     * @see #setSize(String)
     */
    public String getSize() {
        return size;
    }

    /**
     * The size of the pane, either as a number defining pixels, or a percentage
     * defining a percentage of the plot are.
     * <p>
     * Defaults to: 85%
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * @see #setStartAngle(Number)
     */
    public Number getStartAngle() {
        return startAngle;
    }

    /**
     * The start angle of the polar X axis or gauge axis, given in degrees where
     * 0 is north. Defaults to 0.
     */
    public void setStartAngle(Number startAngle) {
        this.startAngle = startAngle;
    }

    public Pane(Number startAngle, Number endAngle) {
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    public void setCenter(String x, String y) {
        this.center = new String[] { x, y };
    }

    public String[] getCenter() {
        return this.center;
    }
}
