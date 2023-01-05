/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.data.renderer;

import java.io.Serializable;
import com.vaadin.flow.data.provider.DataGenerator;
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
