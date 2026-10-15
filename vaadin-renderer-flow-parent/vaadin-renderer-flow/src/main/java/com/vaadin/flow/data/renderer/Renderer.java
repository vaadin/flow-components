/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.data.renderer;

import java.io.Serializable;

import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;

/**
 * Base class for all renderers - classes that take a given model object as
 * input and handles their rendering to the client side when requested.
 *
 * @author Vaadin Ltd
 *
 * @param <SOURCE>
 *            the type of the input object used inside the template
 *
 * @see ValueProvider
 * @see ComponentRenderer
 * @see LitRenderer
 */
public abstract class Renderer<SOURCE> implements Serializable {

    private final String DEFAULT_RENDERER_NAME = "renderer";

    /**
     * Registers a renderer function to the given container element. Creates the
     * setup to handle rendering of individual data items as requested by the
     * renderer function invocation. The renderer function name defaults to
     * "renderer".
     *
     * @param container
     *            the element which accepts the renderer function on the client.
     * @param keyMapper
     *            mapper used internally to fetch items by key and to provide
     *            keys for given items.
     * @return the context of the rendering, that can be used by the components
     *         to provide extra customization
     */
    public Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper) {
        return render(container, keyMapper, DEFAULT_RENDERER_NAME);
    }

    /**
     * Registers a renderer function with the given name to the given container
     * element. Creates the setup to handle rendering of individual data items
     * as requested by the renderer function invocation.
     *
     * @param container
     *            the element which accepts the renderer function on the client.
     * @param keyMapper
     *            mapper used internally to fetch items by key and to provide
     *            keys for given items.
     * @param rendererName
     *            name of the renderer function the container element accepts
     * @return the context of the rendering, that can be used by the components
     *         to provide extra customization
     */
    public abstract Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper, String rendererName);
}
