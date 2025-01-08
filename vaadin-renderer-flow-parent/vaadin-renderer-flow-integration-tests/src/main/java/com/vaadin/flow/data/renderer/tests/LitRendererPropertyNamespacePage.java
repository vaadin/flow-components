/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.data.renderer.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/lit-renderer-property-namespace")
public class LitRendererPropertyNamespacePage extends Div {
    public LitRendererPropertyNamespacePage() {
        // Create a wrapper component with two LitRendererTestComponents
        LitRendererTestComponentWrapper wrapper = new LitRendererTestComponentWrapper(
                2);
        wrapper.setItems("Item 0");

        NativeButton setLitRenderersButton = new NativeButton(
                "Set LitRenderers", e -> {
                    for (int i = 0; i < wrapper.getComponentCount(); i++) {
                        wrapper.setRenderer(i, createLitRenderer(
                                "Component " + i + " : Default Renderer"));
                        wrapper.setDetailsRenderer(i, createLitRenderer(
                                "Component " + i + " : Details Renderer"));
                    }
                });
        setLitRenderersButton.setId("set-lit-renderers");

        NativeButton setComponentRenderersButton = new NativeButton(
                "Set ComponentRenderers", e -> {
                    for (int i = 0; i < wrapper.getComponentCount(); i++) {
                        wrapper.setRenderer(i, createComponentRenderer(
                                "Component " + i + " : Default Renderer"));
                        wrapper.setDetailsRenderer(i, createComponentRenderer(
                                "Component " + i + " : Details Renderer"));
                    }
                });
        setComponentRenderersButton.setId("set-component-renderers");

        NativeButton attachButton = new NativeButton("Attach", e -> {
            add(wrapper);
        });
        attachButton.setId("attach");

        add(setLitRenderersButton, setComponentRenderersButton, attachButton);
    }

    private LitRenderer<String> createLitRenderer(String prefix) {
        LitRenderer<String> renderer = LitRenderer
                .of("<span>${item.content}</span>");
        renderer.withProperty("content", item -> prefix + " : " + item);
        return renderer;
    }

    private ComponentRenderer<Span, String> createComponentRenderer(
            String prefix) {
        return new ComponentRenderer<>(item -> new Span(prefix + " : " + item));
    }
}
