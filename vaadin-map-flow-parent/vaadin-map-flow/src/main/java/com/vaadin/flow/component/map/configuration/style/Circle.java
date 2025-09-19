/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.style;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Feature;

/**
 * Displays a circle shape to represent a {@link Feature}
 */
public class Circle extends ImageStyle {
    private Fill fill;
    private Stroke stroke;
    private Double radius;

    public Circle(Options options) {
        super(options);
        radius = options.radius;
        setFill(options.fill);
        setStroke(options.stroke);
    }

    @Override
    public String getType() {
        return Constants.OL_STYLE_CIRCLE;
    }

    /**
     * The fill style to use for the circle.
     *
     * @return the fill style
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Fill getFill() {
        return fill;
    }

    /**
     * Sets the fill style to use for the circle.
     *
     * @param fill
     *            the fill style
     */
    public void setFill(Fill fill) {
        removeChild(this.fill);
        this.fill = fill;
        addNullableChild(fill);
    }

    /**
     * The stroke style to use for the circle.
     *
     * @return the stroke style
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the stroke style to use for the circle.
     *
     * @param stroke
     *            the stroke style
     */
    public void setStroke(Stroke stroke) {
        removeChild(this.stroke);
        this.stroke = stroke;
        addNullableChild(stroke);
    }

    /**
     * The radius of the circle in pixels.
     *
     * @return the radius
     */
    public Double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the circle in pixels.
     *
     * @param radius
     *            the radius
     */
    public void setRadius(Double radius) {
        this.radius = radius;
        markAsDirty();
    }

    public static class Options extends ImageStyle.Options {
        private Fill fill;
        private Stroke stroke;
        private Double radius;

        /**
         * @see Circle#setFill(Fill)
         */
        public void setFill(Fill fill) {
            this.fill = fill;
        }

        /**
         * @see Circle#setStroke(Stroke)
         */
        public void setStroke(Stroke stroke) {
            this.stroke = stroke;
        }

        /**
         * @see Circle#setRadius(Double)
         */
        public void setRadius(Double radius) {
            this.radius = radius;
        }
    }
}
