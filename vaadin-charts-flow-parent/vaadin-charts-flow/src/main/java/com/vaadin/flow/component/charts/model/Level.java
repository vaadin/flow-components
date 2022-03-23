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
 * Set options on specific levels. Takes precedence over series options, but not
 * point options.
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
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Can set a <code>borderColor</code> on all points which lies on the same
     * level.
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderDashStyle(DashStyle)
     */
    public DashStyle getBorderDashStyle() {
        return borderDashStyle;
    }

    /**
     * Set the dash style of the border of all the point which lies on the
     * level. See <a
     * href"#plotOptions.scatter.dashStyle">plotOptions.scatter.dashStyle</a>
     * for possible options.
     */
    public void setBorderDashStyle(DashStyle borderDashStyle) {
        this.borderDashStyle = borderDashStyle;
    }

    /**
     * @see #setBorderWidth(Number)
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * Can set the borderWidth on all points which lies on the same level.
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Can set a color on all points which lies on the same level.
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
