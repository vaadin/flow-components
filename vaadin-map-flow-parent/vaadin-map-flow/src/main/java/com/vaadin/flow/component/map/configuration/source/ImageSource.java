/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

/**
 * Abstract base class for all sources providing a single image
 * 
 * @since 23.0
 */
public abstract class ImageSource extends Source {
    public ImageSource(Options options) {
        super(options);
    }

    /**
     * @since 23.1
     */
    protected static abstract class Options extends Source.Options {
    }
}
