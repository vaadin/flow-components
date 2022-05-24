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

public class Inactive extends AbstractConfigurationObject {

    private Boolean animation;
    private Boolean enabled;
    private Number lineWidth;
    private Color borderColor;
    private Color color;
    private Number opacity;

    public Inactive() {
    }

    /**
     * @see #setAnimation(Boolean)
     */
    public Boolean getAnimation() {
        return animation;
    }

    /**
     * Animation setting for hovering the graph in line-type series.
     * <p>
     * Defaults to: { "duration": 50 }
     */
    public void setAnimation(Boolean animation) {
        this.animation = animation;
    }

    public Inactive(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable separate styles for the hovered series to visualize that the user
     * hovers either the series itself or the legend. .
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setLineWidth(Number)
     */
    public Number getLineWidth() {
        return lineWidth;
    }

    /**
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * A specific border color for the hovered point. Defaults to inherit the
     * normal state border color.
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * A specific color for the hovered point.
     * <p>
     * Defaults to: undefined
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setOpacity(Number)
     */
    public Number getOpacity() {
        return opacity;
    }

    /**
     * The opacity of a point in treemap. When a point has children, the
     * visibility of the children is determined by the opacity.
     * <p>
     * Defaults to: 0.75
     */
    public void setOpacity(Number opacity) {
        this.opacity = opacity;
    }
}
