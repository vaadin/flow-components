package com.vaadin.flow.component.map.configuration.style;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;

/**
 * Abstract base class for drawing image-based features
 */
public abstract class ImageStyle extends AbstractConfigurationObject {

    private float opacity;
    private boolean rotateWithView;
    private float rotation;
    private float scale;

    /**
     * The opacity of the image. Value values range from {@code 0} to {@code 1}.
     * Defaults to {@code 1}.
     *
     * @return the current opacity
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Sets the opacity for the image.
     *
     * @param opacity
     *            the new opacity
     */
    public void setOpacity(float opacity) {
        this.opacity = opacity;
        markAsDirty();
    }

    /**
     * Whether to rotate the image together with the view. Defaults to
     * {@code false}.
     */
    public boolean isRotateWithView() {
        return rotateWithView;
    }

    /**
     * Sets whether to rotate the image together with the view.
     */
    public void setRotateWithView(boolean rotateWithView) {
        this.rotateWithView = rotateWithView;
        markAsDirty();
    }

    /**
     * The rotation of the image in radians. Defaults to {@code 0}.
     *
     * @return the current rotation
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation of the image in radians.
     *
     * @param rotation
     *            the new rotation
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
        markAsDirty();
    }

    /**
     * The scaling of the image's size. Defaults to {@code 1}.
     *
     * @return the current scaling
     */
    public float getScale() {
        return scale;
    }

    /**
     * Sets the scaling of the image's size.
     *
     * @param scale
     *            the new scaling
     */
    public void setScale(float scale) {
        this.scale = scale;
        markAsDirty();
    }

    public ImageStyle(Options options) {
        opacity = options.opacity;
        rotateWithView = options.rotateWithView;
        rotation = options.rotation;
        scale = options.scale;
    }

    protected static abstract class Options {
        private float opacity = 1;
        private boolean rotateWithView = false;
        private float rotation = 0;
        private float scale = 1;

        /**
         * @see ImageStyle#getOpacity()
         */
        public void setOpacity(float opacity) {
            this.opacity = opacity;
        }

        /**
         * @see ImageStyle#isRotateWithView()
         */
        public void setRotateWithView(boolean rotateWithView) {
            this.rotateWithView = rotateWithView;
        }

        /**
         * @see ImageStyle#getRotation()
         */
        public void setRotation(float rotation) {
            this.rotation = rotation;
        }

        /**
         * @see ImageStyle#getScale()
         */
        public void setScale(float scale) {
            this.scale = scale;
        }
    }
}
