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
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.renderer.ComponentTemplateRenderer;
import com.vaadin.flow.renderer.TemplateRenderer;
import com.vaadin.flow.renderer.TemplateRendererUtil;

/**
 * Helper class with utility methods used internally by {@link Grid} to support
 * {@link TemplateRenderer}s inside cells, headers, footers and detail rows.
 * <p>
 * This class is not meant to be used outside the scope of the Grid.
 *
 * @author Vaadin Ltd.
 */
class GridTemplateRendererUtil {

    private GridTemplateRendererUtil() {
    }

    static <T> CompositeDataGenerator<T> setupTemplateRenderer(
            TemplateRenderer<T> renderer, Element contentTemplate,
            Element templateDataHost, ValueProvider<String, ?> keyMapper) {

        CompositeDataGenerator<T> composite = new CompositeDataGenerator<>();

        renderer.getValueProviders()
                .forEach((key, provider) -> composite.addDataGenerator(
                        (item, jsonObject) -> jsonObject.put(key,
                                JsonSerializer.toJson(provider.apply(item)))));

        TemplateRendererUtil.registerEventHandlers(renderer, contentTemplate,
                templateDataHost, key -> (T) keyMapper.apply(key));

        return composite;
    }

    static <T> void setupHeaderOrFooterComponentRenderer(Component owner,
            ComponentTemplateRenderer<? extends Component, T> componentRenderer) {

        Component renderedComponent = componentRenderer.createComponent(null);
        owner.getElement().appendVirtualChild(renderedComponent.getElement());

        String appId = UI.getCurrent().getInternals().getAppId();

        componentRenderer.setTemplateAttribute("appid", appId);
        componentRenderer.setTemplateAttribute("nodeid", String
                .valueOf(renderedComponent.getElement().getNode().getId()));
    }

}
