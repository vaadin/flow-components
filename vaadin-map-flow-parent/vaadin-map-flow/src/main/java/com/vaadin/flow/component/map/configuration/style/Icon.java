/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.style;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.server.StreamResource;

import java.io.Serializable;

/**
 * An icon or image that can be used to visually represent a {@link Feature}, by
 * using it as the feature's {@link Style#setImage(ImageStyle)}
 */
public class Icon extends ImageStyle {

    private Anchor anchor;
    private final AnchorOrigin anchorOrigin;
    private final String color;
    private final String crossOrigin;
    private final String src;
    private final StreamResource img;
    private final ImageSize imgSize;

    @Override
    public String getType() {
        return Constants.OL_STYLE_ICON;
    }

    /**
     * The anchor position of the image. This defines how the image should be
     * aligned from the {@link #getAnchorOrigin()}. The anchor position is
     * specified in relative units, based on the size of the image. Valid values
     * range from {@code 0} to {@code 1}, where {@code 1} moves the image by its
     * full width or height from the anchor origin. Default value is {@code {x:
     * 0.5, y: 0.5}}, which centers the image.
     *
     * @return the current anchor
     */
    public Anchor getAnchor() {
        return anchor;
    }

    /**
     * Sets the anchor position of the icon's image
     *
     * @param anchor
     *            the new anchor
     */
    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
        markAsDirty();
    }

    /**
     * The origin of the {@link #getAnchor()} position. Defaults to
     * {@link AnchorOrigin#TOP_LEFT}.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the current anchor origin
     */
    public AnchorOrigin getAnchorOrigin() {
        return anchorOrigin;
    }

    /**
     * Color to tint the icon's image with. If not specified, the image will not
     * be modified.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the current color tint
     */
    public String getColor() {
        return color;
    }

    /**
     * The cross-origin attribute value for loaded images.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the cross-origin attribute value
     */
    public String getCrossOrigin() {
        return crossOrigin;
    }

    /**
     * The source URL from which the icon's image should be loaded. Either this
     * or {@link #getImg()} must be specified in the options for the icon, and
     * only one of the two options must be provided.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the source URL for the icon's image
     */
    public String getSrc() {
        return src;
    }

    /**
     * The stream resource from which the icon's image should be loaded. Either
     * this or {@link #getSrc()} must be specified in the options for the icon,
     * and only one of the two options must be provided.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the stream resource containing the icon's image
     */
    public StreamResource getImg() {
        return img;
    }

    /**
     * The size of the image in pixels.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the size of the image
     */
    public ImageSize getImgSize() {
        return imgSize;
    }

    public Icon(Options options) {
        super(options);
        if (options.src == null && options.img == null) {
            throw new NullPointerException(
                    "Either a source URL or an image must be specified in the options");
        }
        if (options.src != null && options.img != null) {
            throw new IllegalStateException(
                    "Both a source URL or an image were specified in the options. You must only specify one of these options.");
        }

        anchor = options.anchor;
        anchorOrigin = options.anchorOrigin;
        color = options.color;
        crossOrigin = options.crossOrigin;
        src = options.src;
        img = options.img;
        imgSize = options.imgSize;
    }

    public static class Options extends ImageStyle.Options {
        private Anchor anchor = new Anchor();
        private AnchorOrigin anchorOrigin = AnchorOrigin.TOP_LEFT;
        private String color;
        private String crossOrigin;
        private String src;
        private StreamResource img;
        private ImageSize imgSize;

        /**
         * @see Icon#getAnchor()
         */
        public void setAnchor(Anchor anchor) {
            this.anchor = anchor;
        }

        /**
         * @see Icon#getAnchorOrigin()
         */
        public void setAnchorOrigin(AnchorOrigin anchorOrigin) {
            this.anchorOrigin = anchorOrigin;
        }

        /**
         * @see Icon#getColor()
         */
        public void setColor(String color) {
            this.color = color;
        }

        /**
         * @see Icon#getCrossOrigin()
         */
        public void setCrossOrigin(String crossOrigin) {
            this.crossOrigin = crossOrigin;
        }

        /**
         * @see Icon#getSrc()
         */
        public void setSrc(String src) {
            this.src = src;
        }

        /**
         * @see Icon#getImg()
         */
        public void setImg(StreamResource img) {
            this.img = img;
        }

        /**
         * @see Icon#getImgSize()
         */
        public void setImgSize(ImageSize imgSize) {
            this.imgSize = imgSize;
        }
    }

    public static class Anchor implements Serializable {
        private double x = 0.5;
        private double y = 0.5;

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public Anchor() {
        }

        public Anchor(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public enum AnchorOrigin {
        BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT,
    }

    public static class ImageSize implements Serializable {
        int width;
        int height;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public ImageSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
