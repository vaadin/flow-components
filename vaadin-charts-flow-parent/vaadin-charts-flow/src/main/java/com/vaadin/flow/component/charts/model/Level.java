/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * Set options on specific levels. Takes precedence over series options, but not
 * point options.
 * 
 * @since 6.0.1
 */
public class Level extends AbstractConfigurationObject {

    private Color borderColor;
    private DashStyle borderDashStyle;
    private Number borderWidth;
    private Color color;
    private DataLabels dataLabels;
    private TreeMapLayoutAlgorithm layoutAlgorithm;
    private TreeMapLayoutStartingDirection layoutStartingDirection;
    private Number level;

    public Level() {
    }

    /**
     * @see #setBorderColor(Color)
     * @since 18.0
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Can set a <code>borderColor</code> on all points which lies on the same
     * level.
     * 
     * @since 18.0
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderDashStyle(DashStyle)
     * @since 18.0
     */
    public DashStyle getBorderDashStyle() {
        return borderDashStyle;
    }

    /**
     * Set the dash style of the border of all the point which lies on the
     * level. See <a
     * href"#plotOptions.scatter.dashStyle">plotOptions.scatter.dashStyle</a>
     * for possible options.
     * 
     * @since 18.0
     */
    public void setBorderDashStyle(DashStyle borderDashStyle) {
        this.borderDashStyle = borderDashStyle;
    }

    /**
     * @see #setBorderWidth(Number)
     * @since 18.0
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * Can set the borderWidth on all points which lies on the same level.
     * 
     * @since 18.0
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setColor(Color)
     * @since 18.0
     */
    public Color getColor() {
        return color;
    }

    /**
     * Can set a color on all points which lies on the same level.
     * 
     * @since 18.0
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setDataLabels(DataLabels)
     */
    public DataLabels getDataLabels() {
        if (dataLabels == null) {
            dataLabels = new DataLabels();
        }
        return dataLabels;
    }

    /**
     * Can set the options of dataLabels on each point which lies on the level.
     * <a href=
     * "#plotOptions.treemap.dataLabels">plotOptions.treemap.dataLabels</a> for
     * possible values.
     * <p>
     * Defaults to: undefined
     */
    public void setDataLabels(DataLabels dataLabels) {
        this.dataLabels = dataLabels;
    }

    /**
     * @see #setLayoutAlgorithm(TreeMapLayoutAlgorithm)
     */
    public TreeMapLayoutAlgorithm getLayoutAlgorithm() {
        return layoutAlgorithm;
    }

    /**
     * Can set the layoutAlgorithm option on a specific level.
     */
    public void setLayoutAlgorithm(TreeMapLayoutAlgorithm layoutAlgorithm) {
        this.layoutAlgorithm = layoutAlgorithm;
    }

    /**
     * @see #setLayoutStartingDirection(TreeMapLayoutStartingDirection)
     */
    public TreeMapLayoutStartingDirection getLayoutStartingDirection() {
        return layoutStartingDirection;
    }

    /**
     * Can set the layoutStartingDirection option on a specific level.
     */
    public void setLayoutStartingDirection(
            TreeMapLayoutStartingDirection layoutStartingDirection) {
        this.layoutStartingDirection = layoutStartingDirection;
    }

    /**
     * @see #setLevel(Number)
     */
    public Number getLevel() {
        return level;
    }

    /**
     * Decides which level takes effect from the options set in the levels
     * object.
     */
    public void setLevel(Number level) {
        this.level = level;
    }

}
