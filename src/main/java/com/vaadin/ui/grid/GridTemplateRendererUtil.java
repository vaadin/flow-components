/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.ui.grid;

import java.io.Serializable;
import java.util.Map;

import com.vaadin.flow.dom.Element;
import com.vaadin.function.ValueProvider;
import com.vaadin.ui.Component;
import com.vaadin.ui.grid.Grid.GridDataGenerator;
import com.vaadin.ui.renderers.ComponentRendererUtil;
import com.vaadin.ui.renderers.ComponentTemplateRenderer;
import com.vaadin.ui.renderers.TemplateRenderer;
import com.vaadin.ui.renderers.TemplateRendererUtil;
import com.vaadin.util.JsonSerializer;

/**
 * Helper class with utility methods used internally by {@link Grid} to support
 * {@link TemplateRenderer}s inside cells, headers, footers and detail rows.
 * <p>
 * This class is not meant to be used outside the scope of the Grid.
 * 
 * @author Vaadin Ltd.
 */
class GridTemplateRendererUtil {

    /**
     * Internal object to hold {@link ComponentTemplateRenderer}s and their
     * generated {@link Component}s together.
     * 
     * @param <T>
     *            the model item attached to the component
     */
    static final class RendereredComponent<T> implements Serializable {
        private Component component;
        private ComponentTemplateRenderer<? extends Component, T> componentRenderer;

        /**
         * Default constructor.
         * 
         * @param component
         *            the generated component
         * @param componentRenderer
         *            the renderer that generated the component
         */
        public RendereredComponent(Component component,
                ComponentTemplateRenderer<? extends Component, T> componentRenderer) {
            this.component = component;
            this.componentRenderer = componentRenderer;
        }

        /**
         * Gets the current generated component.
         * 
         * @return the generated component by the renderer
         */
        public Component getComponent() {
            return component;
        }

        /**
         * Recreates the component by calling
         * {@link ComponentTemplateRenderer#createComponent(Object)}, and sets
         * the internal component returned by {@link #getComponent()}.
         * 
         * @param item
         *            the model item to be attached to the component instance
         * @return the new generated component returned by the renderer
         */
        public Component recreateComponent(T item) {
            component = componentRenderer.createComponent(item);
            return component;
        }
    }

    private GridTemplateRendererUtil() {
    }

    static <T> void setupTemplateRenderer(TemplateRenderer<T> renderer,
            Element contentTemplate, Element templateDataHost,
            GridDataGenerator<T> dataGenerator,
            ValueProvider<String, ?> keyMapper) {

        renderer.getValueProviders()
                .forEach((key, provider) -> dataGenerator.addDataGenerator(
                        (item, jsonObject) -> jsonObject.put(key,
                                JsonSerializer.toJson(provider.apply(item)))));

        TemplateRendererUtil.registerEventHandlers(renderer, contentTemplate,
                templateDataHost, key -> (T) keyMapper.apply(key));
    }

    static <T> void setupHeaderOrFooterComponentRenderer(Component owner,
            ComponentTemplateRenderer<? extends Component, T> componentRenderer) {
        Element container = ComponentRendererUtil
                .createContainerForRenderers(owner);

        componentRenderer.setTemplateAttribute("key", "0");
        componentRenderer.setTemplateAttribute("keyname",
                "data-flow-renderer-item-key");
        componentRenderer.setTemplateAttribute("containerid",
                container.getAttribute("id"));

        Component renderedComponent = componentRenderer.createComponent(null);
        GridTemplateRendererUtil.registerRenderedComponent(componentRenderer,
                null, container, "0", renderedComponent);
    }

    static <T> void registerRenderedComponent(
            ComponentTemplateRenderer<? extends Component, T> componentRenderer,
            Map<String, RendereredComponent<T>> renderedComponents,
            Element container, String key, Component component) {
        component.getElement().setAttribute("data-flow-renderer-item-key", key);
        container.appendChild(component.getElement());

        if (renderedComponents != null) {
            renderedComponents.put(key,
                    new RendereredComponent<>(component, componentRenderer));
        }
    }

}
