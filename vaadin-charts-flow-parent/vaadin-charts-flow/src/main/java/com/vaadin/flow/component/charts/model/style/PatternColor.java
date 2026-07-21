/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.style;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class providing pattern fills, an accessibility feature that renders series or
 * points with an SVG pattern (defined by an SVG path or an image) instead of a
 * plain color. This is useful to distinguish data for color-blind users.
 * <p>
 * The produced JSON matches the Highcharts pattern option shape, for example:
 *
 * <pre>
 * {"pattern":{"path":"M 0 0 L 10 10","width":10,"height":10,"color":"#ff0000"}}
 * </pre>
 *
 * @since 25.3
 */
@SuppressWarnings("serial")
public class PatternColor implements Color {

    /**
     * The pattern definition holding the individual Highcharts pattern options.
     */
    public static class Pattern implements Serializable {

        private String path;
        private String image;
        private Number width;
        private Number height;
        private Number x;
        private Number y;
        private Color color;
        private Number opacity;
        private Color backgroundColor;
        private Number aspectRatio;
        private String patternTransform;
        private String id;

        /**
         * @return the SVG path used to draw the pattern
         */
        public String getPath() {
            return path;
        }

        /**
         * Sets the SVG path data used to draw the pattern (for example
         * {@code "M 0 0 L 10 10"}).
         * <p>
         * Highcharts also accepts an object form for the path (with
         * {@code d}/{@code strokeWidth}); only the string form is exposed here.
         * A pattern should define either a path or an {@link #setImage(String)
         * image}, not both.
         *
         * @param path
         *            the SVG path data
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * @return the URL of the image used as the pattern
         */
        public String getImage() {
            return image;
        }

        /**
         * Sets the URL of an image to use as the pattern. A pattern should
         * define either an image or a {@link #setPath(String) path}, not both.
         *
         * @param image
         *            the image URL
         */
        public void setImage(String image) {
            this.image = image;
        }

        /**
         * @return the width of the pattern
         */
        public Number getWidth() {
            return width;
        }

        /**
         * Sets the width of the pattern.
         *
         * @param width
         *            the pattern width
         */
        public void setWidth(Number width) {
            this.width = width;
        }

        /**
         * @return the height of the pattern
         */
        public Number getHeight() {
            return height;
        }

        /**
         * Sets the height of the pattern.
         *
         * @param height
         *            the pattern height
         */
        public void setHeight(Number height) {
            this.height = height;
        }

        /**
         * @return the horizontal offset of the pattern
         */
        public Number getX() {
            return x;
        }

        /**
         * Sets the horizontal offset of the pattern.
         *
         * @param x
         *            the horizontal offset
         */
        public void setX(Number x) {
            this.x = x;
        }

        /**
         * @return the vertical offset of the pattern
         */
        public Number getY() {
            return y;
        }

        /**
         * Sets the vertical offset of the pattern.
         *
         * @param y
         *            the vertical offset
         */
        public void setY(Number y) {
            this.y = y;
        }

        /**
         * @return the color of the pattern path
         */
        public Color getColor() {
            return color;
        }

        /**
         * Sets the color of the pattern path. Use a {@link SolidColor} so that
         * it serializes to a bare color string.
         *
         * @param color
         *            the pattern color
         */
        public void setColor(Color color) {
            this.color = color;
        }

        /**
         * @return the opacity of the pattern
         */
        public Number getOpacity() {
            return opacity;
        }

        /**
         * Sets the opacity of the pattern, between 0 and 1.
         *
         * @param opacity
         *            the pattern opacity
         */
        public void setOpacity(Number opacity) {
            this.opacity = opacity;
        }

        /**
         * @return the background color of the pattern
         */
        public Color getBackgroundColor() {
            return backgroundColor;
        }

        /**
         * Sets the background color rendered behind the pattern. Use a
         * {@link SolidColor} so that it serializes to a bare color string.
         *
         * @param backgroundColor
         *            the background color
         */
        public void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        /**
         * @return the aspect ratio of an image pattern
         */
        public Number getAspectRatio() {
            return aspectRatio;
        }

        /**
         * Sets the aspect ratio used to fit an image pattern.
         *
         * @param aspectRatio
         *            the aspect ratio
         */
        public void setAspectRatio(Number aspectRatio) {
            this.aspectRatio = aspectRatio;
        }

        /**
         * @return the SVG transform applied to the pattern
         */
        public String getPatternTransform() {
            return patternTransform;
        }

        /**
         * Sets the SVG transform applied to the pattern (for example
         * {@code "rotate(45)"}).
         *
         * @param patternTransform
         *            the SVG transform
         */
        public void setPatternTransform(String patternTransform) {
            this.patternTransform = patternTransform;
        }

        /**
         * @return the id of the pattern
         */
        public String getId() {
            return id;
        }

        /**
         * Sets an explicit id for the pattern definition. When omitted, an id is
         * generated on the client side based on the pattern content.
         *
         * @param id
         *            the pattern id
         */
        public void setId(String id) {
            this.id = id;
        }
    }

    private final Pattern pattern = new Pattern();

    private PatternColor() {
    }

    /**
     * @return the pattern definition, used to configure the remaining pattern
     *         options
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Creates a pattern fill drawn with the given SVG path.
     *
     * @param path
     *            the SVG path data, for example {@code "M 0 0 L 10 10"}
     * @return a new path-based pattern color
     */
    public static PatternColor createPath(String path) {
        Objects.requireNonNull(path, "path");
        PatternColor ret = new PatternColor();
        ret.pattern.setPath(path);
        return ret;
    }

    /**
     * Creates a pattern fill drawn with the given SVG path, color and size.
     *
     * @param path
     *            the SVG path data, for example {@code "M 0 0 L 10 10"}
     * @param color
     *            the color of the pattern path; use a {@link SolidColor} so it
     *            serializes to a bare color string
     * @param width
     *            the width of the pattern
     * @param height
     *            the height of the pattern
     * @return a new path-based pattern color
     */
    public static PatternColor createPath(String path, Color color, int width,
            int height) {
        Objects.requireNonNull(path, "path");
        PatternColor ret = new PatternColor();
        ret.pattern.setPath(path);
        ret.pattern.setColor(color);
        ret.pattern.setWidth(width);
        ret.pattern.setHeight(height);
        return ret;
    }

    /**
     * Creates a pattern fill using the image at the given URL.
     *
     * @param image
     *            the URL of the image to use as the pattern
     * @return a new image-based pattern color
     */
    public static PatternColor createImage(String image) {
        Objects.requireNonNull(image, "image");
        PatternColor ret = new PatternColor();
        ret.pattern.setImage(image);
        return ret;
    }
}
