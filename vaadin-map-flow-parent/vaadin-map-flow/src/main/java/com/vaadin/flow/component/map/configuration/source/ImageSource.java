package com.vaadin.flow.component.map.configuration.source;

/**
 * Abstract base class for all sources providing a single image
 */
public abstract class ImageSource extends Source {
    public ImageSource(BaseOptions<?> options) {
        super(options);
    }

    public static abstract class BaseOptions<T extends BaseOptions<T>>
            extends Source.BaseOptions<T> {

    }
}
