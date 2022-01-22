package com.vaadin.flow.component.map.configuration.style;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright (C) 2022 - 2022 Vaadin Ltd
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

public abstract class ImageStyle extends AbstractConfigurationObject {

    private float opacity;
    private boolean rotateWithView;
    private float rotation;
    private float scale;

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
        notifyChange();
    }

    public boolean isRotateWithView() {
        return rotateWithView;
    }

    public void setRotateWithView(boolean rotateWithView) {
        this.rotateWithView = rotateWithView;
        notifyChange();
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        notifyChange();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        notifyChange();
    }

    public ImageStyle(BaseOptions<?> options) {
        opacity = options.opacity;
        rotateWithView = options.rotateWithView;
        rotation = options.rotation;
        scale = options.scale;
    }

    protected static class BaseOptions<T extends BaseOptions<T>> {
        private float opacity = 1;
        private boolean rotateWithView = false;
        private float rotation = 0;
        private float scale = 1;

        protected T getThis() {
            return (T) this;
        }

        public T setOpacity(float opacity) {
            this.opacity = opacity;
            return getThis();
        }

        public T setRotateWithView(boolean rotateWithView) {
            this.rotateWithView = rotateWithView;
            return getThis();
        }

        public T setRotation(float rotation) {
            this.rotation = rotation;
            return getThis();
        }

        public T setScale(float scale) {
            this.scale = scale;
            return getThis();
        }
    }
}
